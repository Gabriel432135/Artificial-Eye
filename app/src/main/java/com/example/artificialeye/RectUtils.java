/*
    Autor: Gabriel Alves
*/

package com.example.artificialeye;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Size;

import org.opencv.core.Rect;

public class RectUtils {
    private RectUtils(){}

    private static Matrix matrix = new Matrix();
    private static RectF rectF = new RectF();

    public static android.graphics.Rect toAndroidRect(Rect openCvRect) {
        return new android.graphics.Rect(
                openCvRect.x,
                openCvRect.y,
                openCvRect.x + openCvRect.width,
                openCvRect.y + openCvRect.height
        );
    }

    public static Rect toOpenCvRect(android.graphics.Rect androidRect) {
        return new Rect(
                androidRect.left,
                androidRect.top,
                androidRect.width(),
                androidRect.height()
        );
    }

    public static android.graphics.Rect[] toAndroidRects(Rect[]openCvRects){
        android.graphics.Rect[] androidRects = new android.graphics.Rect[openCvRects.length];
        for(int i = 0; i<openCvRects.length; i++){
            androidRects[i] = toAndroidRect(openCvRects[i]);
        }
        return androidRects;
    }

    /*
    A forma como o cameraX deixa a imagem em 16:9 é escalonando enquanto o buffer
    do surfaceTexture faz isso esticando. Aqui ajusto as coordenadas dos rects calculados
    no imageProxy para serem exibidos no preview corretamente
     */


    //Esse método escalona as coordenadas do rect fornecido. Ele não corrige o quão esticadas estão, apenas amplia.
    public static void scaleRect(android.graphics.Rect rect, Size origin, Size staggered) {
        short x = (short) staggered.getWidth();
        short y = (short) staggered.getHeight();
        short a = (short) origin.getWidth();
        short b = (short) origin.getHeight();

        //coeficiente de multiplicação
        float coef = (float) x/a;
        if (coef*b < y) coef = (float) y/b;

        rect.set(Math.round(rect.left*coef), Math.round(rect.top*coef), Math.round(rect.right*coef), Math.round(rect.bottom*coef));
    }

    //Esse método estica as coordenadas do rect fornecido. Ele estica e contrai suas dimensões para ficar proporcional ao Size stretched
    public static void stretchRect(android.graphics.Rect rect, Size origin, Size stretched){
        short x = (short) stretched.getWidth();
        short y = (short) stretched.getHeight();
        short a = (short) origin.getWidth();
        short b = (short) origin.getHeight();

        //coeficientes de multiplicação
        float coefWidth = (float) x/a;
        float coefHeight = (float) y/b;

        rect.set(Math.round(rect.left*coefWidth), Math.round(rect.top*coefHeight), Math.round(rect.right*coefWidth), Math.round(rect.bottom*coefHeight));
    }

    //Determina um Size escalonado em uma superfície de tamanho diferente.
    public static Size scaleSize(Size origin, Size staggered) {
        short x = (short) staggered.getWidth();
        short y = (short) staggered.getHeight();
        short a = (short) origin.getWidth();
        short b = (short) origin.getHeight();

        //coeficiente de multiplicação
        float coef = (float) x/a;
        if (coef*b < y) coef = (float) y/b;

        return new Size((int) (a*coef), (int) (b*coef));
    }

    //Como as coordenadas dos rects começam a contar do canto superior esquerdo, este método alinha
    // ele com a view após ser ajustado, com base no tamanho onde rect foi calculado e no tamanho da view.
    public static void alignRectWithSize(android.graphics.Rect rect, Size origin, Size screen){
        short a = (short) origin.getWidth();
        short b = (short) origin.getHeight();

        short Xoffset = (short) Math.round((float)((a-screen.getWidth())/2));
        short Yoffset = (short) Math.round((float)((b-screen.getHeight())/2));

        rect.set(rect.left-Xoffset, rect.top-Yoffset, rect.right-Xoffset, rect.bottom-Yoffset);
    }

    //Rotaciona o rect em torno de um ponto pivô
    public static void rotateAndroidRect(android.graphics.Rect rect, float degrees, Point pivot){
        matrix.setRotate(degrees, pivot.x, pivot.y);
        rectF.set(rect);
        matrix.mapRect(rectF);
        rectF.roundOut(rect);
    }

    public static Rect getRotatedOpencvRect(Rect rect, float degrees, Point pivot){
        android.graphics.Rect androidRect = toAndroidRect(rect);
        rotateAndroidRect(androidRect, degrees, pivot);
        return toOpenCvRect(androidRect);
    }



}

