/*
    Autor: Gabriel Alves
*/

package com.example.artificialeye;

import org.opencv.android.OpenCVLoader;

public class MyOpenCVLoader {
    private boolean loaded = false;

    private LoaderCallback loaderCallback;

    public boolean isLoaded() {
        return loaded;
    }

    public void load(){
        if(loaded) return;
        new Thread(() -> {
            if (OpenCVLoader.initDebug()) {
                if (loaderCallback != null) loaderCallback.onLoaded();
                loaded = true;
            }
        }).start();
    }

    public void registerCallback(LoaderCallback loaderCallback){
        this.loaderCallback = loaderCallback;
    }

    public interface LoaderCallback{
        public void onLoaded();
    }
}
