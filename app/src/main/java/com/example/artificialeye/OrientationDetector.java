package com.example.artificialeye;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationDetector{
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;
    private float[] accelerometerReading;

    public static final int PORTATIL = 0;
    public static final int LANDSCAPE = 1;
    public static final int PORTATIL_UPSIDE_DOWN = 2;
    public static final int LANDSCAPE_UPSIDE_DOWN = 3;

    public OrientationDetector(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerReading = new float[3];


        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor == accelerometer){
                    System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //Nada
            }
        };
    }

    public OrientationDetector start(){
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        return this;
    }

    public void stop(){
        sensorManager.unregisterListener(sensorListener);
    }
    public int getBestOrientation(){
        //Se o vetor no eixo y é maior que no eixo x
        if(Math.abs(accelerometerReading[1])>Math.abs(accelerometerReading[0])){
            //Se o módulo do vetor é negativo
            if(accelerometerReading[1]<0){
                return OrientationDetector.PORTATIL_UPSIDE_DOWN;
            }else{
                return OrientationDetector.PORTATIL;
            }
        }else{
            //Se o módulo do vetor é negativo
            if(accelerometerReading[0]<0){
                return OrientationDetector.LANDSCAPE_UPSIDE_DOWN;
            }else{
                //Padrão
                return OrientationDetector.LANDSCAPE;
            }
        }
    }
}
