/*
    Autor: Gabriel Alves
*/

package com.example.artificialeye;

import android.content.Context;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


public class CameraX {
    private static final String TAG = "CameraX";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private Context context;
    private Preview.SurfaceProvider surfaceProvider;
    private ExecutorService cameraExecutor;
    private ImageAnalysis.Analyzer analyzer;
    private Preview preview;
    private Size previewSize;
    private CameraListener cameraListener;

    public CameraX(@NonNull Context context, @NonNull Preview.SurfaceProvider surfaceProvider, @NonNull ExecutorService executorsService, @NonNull ImageAnalysis.Analyzer analyzer) {
        this.context = context;
        this.surfaceProvider = surfaceProvider;
        this.cameraExecutor = executorsService;
        this.analyzer = analyzer;
    }

    public void startCamera() {
        //CameraX
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderFuture.get();


                // Preview
                preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();

                preview.setSurfaceProvider(surfaceProvider);


                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                //Cria o image Analyzer
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, imageAnalysis, preview);

                //Depois que a câmera for iniciada:
                previewSize = new Size(preview.getResolutionInfo().getResolution().getWidth(), preview.getResolutionInfo().getResolution().getHeight());
                if(cameraListener!=null) cameraListener.onBindToLifecycle();

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error getting camera provider", e);
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void attachCameraListener(CameraListener listener){
        cameraListener = listener;
    }

    public Size getSize(){
        return previewSize;
    }

    public interface CameraListener{
        public void onBindToLifecycle();
    }

}
