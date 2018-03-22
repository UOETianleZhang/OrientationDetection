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
 * Created by tianlezhang on 2017/11/4.
 */

public class GetOrientation extends GetData implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetic;
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] values = new float[3];

    private int accuracy = -1;
    private long counter = 0;
    private long currentTime;
    private long lastIntervalTime;
    private long intervalError;
    long zeroTime = 0;
    private long SENSOR_DELAY = 50000000; //in nanoseconds

    private FileOutputStream out;
    private BufferedWriter writer;
    private final static String TAG = "Orientation";

    public long getZerotime(){ return zeroTime;}
    public float[] getValues(){return values;}

    public GetOrientation(Service context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Log.d(TAG, "GetOrientation: 构造函数被调用了");
        try {
            out = context.openFileOutput(TAG+"data.txt",Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write("accuracy\tinterval(ns)\tAzimuth\tPitch\tRoll\n");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            writer.write("Current time:" + df.format(new Date()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displaySensor(Activity context) {
        TextView rotation_Azimuth = (TextView) context.findViewById(R.id.rotation_vector_x);
        TextView rotation_Pitch = (TextView) context.findViewById(R.id.rotation_vector_y);
        TextView rotation_Roll = (TextView) context.findViewById(R.id.rotation_vector_z);
        if(rotation_Azimuth!=null&&rotation_Pitch!=null&&rotation_Roll!=null) {
            rotation_Azimuth.setText(new Formatter().format("%.1f", Math.toDegrees(values[0])).toString()+"°");
            rotation_Pitch.setText(new Formatter().format("%.1f", Math.toDegrees(values[1])).toString()+"°");
            rotation_Roll.setText(new Formatter().format("%.1f", Math.toDegrees(values[2])).toString()+"°");
        }
    }

    @Override
    public void initSensor() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, magnetic,30000);
//        mSensorManager.registerListener(this,accelerometer, 50000);
//        mSensorManager.registerListener(this,magnetic,50000);
        Log.d(TAG, "GetOrientation: sensor初始化成功");
        zeroTime = System.nanoTime();
    }

    public void refresh() {
//        mSensorManager.registerListener(this,accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.unregisterListener(this);
//        mSensorManager.registerListener(this,accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//        //in microseconds
//        mSensorManager.registerListener(this,magnetic,SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void destroySensor() {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//set the format of displaying date
        try {
            writer.write("Current time:" + df.format(new Date()) + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //mSensorManager.unregisterListener(this);
    }

    private void calculateOrientation() {
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //To measure the minimum sampling interval
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
//                currentTime = System.nanoTime();
//                if(currentTime-zeroTime-counter*SENSOR_DELAY > SENSOR_DELAY) {
//                    intervalError = currentTime-zeroTime-(counter+1)*SENSOR_DELAY;
//                    counter = (currentTime-zeroTime)/SENSOR_DELAY;
//                    long inteval = currentTime - lastIntervalTime;
//                    lastIntervalTime = currentTime;
//                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//                    try {
//                        writer.write(df.format(new Date())+"\t"+accuracy+"\t"+inteval+"\t"+Math.toDegrees(values[0])+"\t"+Math.toDegrees(values[1])+"\t"+Math.toDegrees(values[2])+"\n");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Log.i("Accelerometer", df.format(new Date())+"\t"+accuracy+"\t"+inteval+"\t"+Math.toDegrees(values[0])+"\t"+Math.toDegrees(values[1])+"\t"+Math.toDegrees(values[2]));
//                }



            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;

                //To measure the minimum sampling interval
                long inteval = System.nanoTime() - currentTime;
                currentTime = System.nanoTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                try {
                    writer.write(df.format(new Date())+"\t"+accuracy+"\t"+inteval+"\t"+Math.toDegrees(values[0])+"\t"+Math.toDegrees(values[1])+"\t"+Math.toDegrees(values[2])+"\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            calculateOrientation();
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        GetOrientation.this.accuracy = accuracy;
        Log.d(TAG, "onAccuracyChanged: Orientation");
    }

}
