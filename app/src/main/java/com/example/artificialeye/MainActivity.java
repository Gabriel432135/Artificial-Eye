/*
    Autor: Gabriel Alves
*/

package com.example.artificialeye;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;


import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.artificialeye.databinding.ActivityMainBinding;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding viewBinding;
    private CameraX cameraX;
    private ExecutorService cameraExecutor;
    private MyOpenCVLoader openCvLoader;
    private CascadeClassifier faceCascade;
    private OrientationDetector orientationDetector;
    private Paint paint;
    private Size screenSize, imageProxySize;
    private static final String TAG = "CameraXApp";
    private int orientation;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        this.fullScreen();

        cameraExecutor = Executors.newSingleThreadExecutor();

        viewBinding.viewFinder.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        viewBinding.overlayTexture.setOpaque(false);

        paint = new Paint();

        orientationDetector = new OrientationDetector(this).start();

        openCvLoader = new MyOpenCVLoader();

        openCvLoader.registerCallback(() -> {
            Log.i("Fluxo 1", "O OpenCV foi carregado com sucesso");
            faceCascade = new CascadeClassifier(loadXML().getAbsolutePath());
        });

        openCvLoader.load();

        screenSize = getFullScreenSize();

        cameraX = new CameraX(this, viewBinding.viewFinder.getSurfaceProvider(), cameraExecutor, image -> {
            //Processar imagem aqui
            if(openCvLoader.isLoaded()){
                processImage(image);
            }
            image.close();
        });
        //Quando a câmera estiver funcionando:
        cameraX.attachCameraListener(() -> {
            //Obtém o tamanho escalonado da câmera no preview
        });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    private void startCamera() {
        cameraX.startCamera();
    }


    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.fullScreen();
    }

    private void fullScreen(){
        //Ocultar action bar
        if(this.getSupportActionBar()!=null){
            this.getSupportActionBar().hide();
        }

        // Ocultar barra de status
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Usar área do recorte da câmera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        // Ocultar barra de navegação
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    private void processImage(ImageProxy image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) return;

        // Converta a imagem para Mat (formato do OpenCV)
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        Mat yMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        yMat.put(0, 0, data);

        MatOfRect faces = new MatOfRect();

        orientation = orientationDetector.getBestOrientation();

        Mat rotatedMat = getRotatedMat(yMat, orientation);

        faceCascade.detectMultiScale(rotatedMat, faces, 1.5, 4,
                0 | Objdetect.CASCADE_SCALE_IMAGE, new org.opencv.core.Size(40, 40), new org.opencv.core.Size());

        drawRects(image, resetRectRotation(faces.toArray(), orientation, rotatedMat.size()));
    }

    private File loadXML(){

        File mCascadeFile = null;
        try {
            File cascadeDir = new File(getCacheDir(), "/cascade/"); // Usar o diretório de cache

            if(!cascadeDir.exists()){
                if(!cascadeDir.mkdirs()){
                    Log.e("Fluxo 2", "Erro ao criar diretório /cascade no cache");
                }
            }

            mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

            if(mCascadeFile.exists()) return mCascadeFile;

            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096]; //4kb
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            Log.i("Fluxo 1", "XML copiado com sucesso");

        }catch (Exception e){
            Log.e("Fluxo 2", "Erro: "+e);
        }

        return mCascadeFile;

    }

    private void drawRects(ImageProxy image, org.opencv.core.Rect[] rectOpencv){
        //Cria e prepara os objetos usados para desenhar no canvas
        if(!viewBinding.overlayTexture.isAvailable()) return;
        SurfaceTexture surfaceTexture = viewBinding.overlayTexture.getSurfaceTexture();
        if(surfaceTexture == null) return;
        // Configura o tamanho do buffer do SurfaceTexture
        surfaceTexture.setDefaultBufferSize(screenSize.getWidth(), screenSize.getHeight());

        if(imageProxySize==null) imageProxySize = new Size(image.getWidth(), image.getHeight());

        //Convertendo o rect do openCv no rect do android
        Rect[]retangles = RectUtils.toAndroidRects(rectOpencv);

        // Obtém o Canvas do SurfaceTexture
        Canvas canvas = viewBinding.overlayTexture.lockCanvas();
        if(canvas == null) return;

        //Limpando a superfície
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        //Configura o pincel
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for(Rect retangle: retangles){
            RectUtils.scaleRect(retangle, imageProxySize, screenSize);
            RectUtils.alignRectWithSize(retangle, RectUtils.scaleSize(imageProxySize, screenSize), screenSize);
            canvas.drawRect(retangle, paint);
        }
        viewBinding.overlayTexture.unlockCanvasAndPost(canvas);
    }

    public Size getFullScreenSize() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        Point size = new Point();
        display.getRealSize(size);

        return new Size(size.x, size.y);
    }

    public Mat getRotatedMat(Mat mat, int rotation){

        //Se a posição já está certa, não faça nada
        if(rotation == OrientationDetector.LANDSCAPE) return mat;

        Mat rotatedMat = new Mat();

        switch (rotation){
            case OrientationDetector.PORTATIL:
                Core.rotate(mat, rotatedMat, Core.ROTATE_90_CLOCKWISE);
                break;
            case OrientationDetector.PORTATIL_UPSIDE_DOWN:
                Core.rotate(mat, rotatedMat, Core.ROTATE_90_COUNTERCLOCKWISE);
                break;
            case OrientationDetector.LANDSCAPE_UPSIDE_DOWN:
                Core.rotate(mat, rotatedMat, Core.ROTATE_180);
                break;
        }
        return rotatedMat;
    }

    //Esse método é usado para ressetar a rotação dos rects gerados pelo processamento com o Mat rotacionado
    public org.opencv.core.Rect[] resetRectRotation(org.opencv.core.Rect[]rects, int orientation, org.opencv.core.Size matSize){
        float angleReset = 0;
        Point pivot = null;

        switch (orientation){
            case OrientationDetector.PORTATIL:
                angleReset = 270f;
                pivot = new Point((int) matSize.width/2, (int) matSize.width/2);
                break;
            case OrientationDetector.PORTATIL_UPSIDE_DOWN:
                angleReset = 90f;
                pivot = new Point((int) matSize.height/2, (int) matSize.height/2);
                break;
            case OrientationDetector.LANDSCAPE_UPSIDE_DOWN:
                angleReset = 180f;
                pivot = new Point((int) matSize.width/2, (int) matSize.height/2);
                break;
            case OrientationDetector.LANDSCAPE:
                return rects;
        }
        org.opencv.core.Rect rectsRotated[] = new org.opencv.core.Rect[rects.length];

        for(short i = 0; i<rects.length; i++){
            rectsRotated[i] = RectUtils.getRotatedOpencvRect(rects[i], angleReset, pivot);
        }

        return rectsRotated;
    }




}