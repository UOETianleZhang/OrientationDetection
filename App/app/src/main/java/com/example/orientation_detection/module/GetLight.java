package com.example.orientation_detection.module;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.example.orientation_detection.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

/**
 * Created by tianlezhang on 2017/11/14.
 * This class implements the interaction with the light sensor.
 */

public class GetLight implements GetData, SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mLight;
    private float lightValue;     //The real time illuminance value
    private long zeroTime;
    private int accuracy = -1;
    private final static String TAG = "Light";
    private BufferedWriter writeLight;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public float getLightValue() {return lightValue;}

    public GetLight(Service context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }
    @Override
    public void initSensor() {
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_FASTEST);
        zeroTime = System.nanoTime();

        File fileLight = new File("/sdcard/", "DataLight.txt");
        try {
            writeLight = new BufferedWriter(new FileWriter(fileLight));
            writeLight.write("data\taccuracy\tinterval(ns)\tIlluminance\n");
            writeLight.write("Start time:" + df.format(new Date()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroySensor() {
        mSensorManager.unregisterListener(this);
        try {
            writeLight.write("Over time:" + df.format(new Date()) + "\n");
            writeLight.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displaySensor(Activity context) {
        TextView lightText =(TextView) context.findViewById(R.id.light_value);
        if(lightText!=null)
            lightText.setText(new Formatter().format("%.1f",lightValue).toString()+"\t\tlx");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        lightValue = event.values[0];

        long absoluteTime = System.nanoTime()-zeroTime;
        try {
            writeLight.write(df.format(new Date())+"\t"+accuracy+"\t"+absoluteTime+"\t"+lightValue+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        GetLight.this.accuracy = accuracy;
        Log.d(TAG, "onAccuracyChanged: Light");

    }
}
