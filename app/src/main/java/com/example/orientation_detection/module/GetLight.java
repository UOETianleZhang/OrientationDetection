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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

/**
 * Created by tianlezhang on 2017/11/14.
 */

public class GetLight extends GetData implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mLight;
    private float lightValue;
    private FileOutputStream out;
    private BufferedWriter writer;
    private long zeroTime=0;
    private int accuracy = -1;
    private final static String TAG = "Light";
    private long currentTime = System.nanoTime();

    public float getLightValue() {return lightValue;}

    public GetLight(Service context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

//        try {
//            out = context.openFileOutput(TAG+"data.txt",Context.MODE_PRIVATE);
//            writer = new BufferedWriter(new OutputStreamWriter(out));
//            writer.write("accuracy\tinterval(ns)\tAzimuth\tPitch\tRoll\n");
//
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//            writer.write("Current time:" + df.format(new Date()) + "\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    @Override
    public void initSensor() {
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void destroySensor() {

//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//set the format of displaying date
//        try {
//            writer.write("Current time:" + df.format(new Date()) + "\n");
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        mSensorManager.unregisterListener(this);
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

//        //To measure the minimum sampling interval
//        long inteval = System.nanoTime() - currentTime;
//        currentTime = System.nanoTime();
//
//
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//        try {
//            writer.write(df.format(new Date())+"\t"+accuracy+"\t"+inteval+"\t"+lightValue+"\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        GetLight.this.accuracy = accuracy;
        Log.d(TAG, "onAccuracyChanged: Light");

    }
}
