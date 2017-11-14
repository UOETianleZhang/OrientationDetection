package com.example.orientation_detection.module;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.example.orientation_detection.R;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Formatter;

/**
 * Created by tianlezhang on 2017/11/4.
 */

public class GetOrientation extends GetData implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetic;
    private Activity context;
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] values = new float[3];

    private int accuracy = -1;
    private long currentTime = System.nanoTime();

    private FileOutputStream out;
    private BufferedWriter writer;
    public final static String TAG = "Orientation";

    public GetOrientation(Activity context){
        this.context = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        try {
            out = context.openFileOutput(TAG+"data.txt",Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write("accuracy\tinterval(ns)\tAzimuth\tPitch\tRoll\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displaySensor() {
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
        mSensorManager.registerListener(this, accelerometer, Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, magnetic,Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void destroySensor() {
        mSensorManager.unregisterListener(this);
    }

    private void calculateOrientation() {
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values;
        }
        calculateOrientation();

        //测试的是实际的间隔时间，不是最小
        //是最小
        long inteval = System.nanoTime() - currentTime;
        currentTime = System.nanoTime();
        try {
            writer.write(""+accuracy+"\t"+inteval+"\t"+Math.toDegrees(values[0])+"\t"+Math.toDegrees(values[1])+"\t"+Math.toDegrees(values[2])+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        GetOrientation.this.accuracy = accuracy;
    }
}
