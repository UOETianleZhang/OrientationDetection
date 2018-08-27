package com.example.orientation_detection.module;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.example.orientation_detection.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by tianlezhang on 2017/11/4.
 */

public class GetOrientation implements GetData, SensorEventListener{
    /**This class implements the interaction with the two orientation
     * sensors: accelerometer and magnetic
     */

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetic;
    PowerManager.WakeLock wakeLock;

    //all sensor values are stored as global variables in this class
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] values = new float[3];  //3 orientation angles

    private int accuracy = -1;
    long zeroTime = 0;

    private BufferedWriter writerAccelerometer;
    private BufferedWriter writerMagnetometer;
    private BufferedWriter writerOrientation;
    private final static String TAG = "Orientation";
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public long getZerotime(){ return zeroTime;}
    public float[] getValues(){return values;}

    //Initiate the class
    public GetOrientation(Service context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    //Display the orientation angles on the phone screen
    public void displaySensor(Activity context) {
        TextView rotation_Azimuth = (TextView) context.findViewById(R.id.rotation_vector_x);
        TextView rotation_Pitch = (TextView) context.findViewById(R.id.rotation_vector_y);
        TextView rotation_Roll = (TextView) context.findViewById(R.id.rotation_vector_z);
        if(rotation_Azimuth!=null&&rotation_Pitch!=null&&rotation_Roll!=null) {
            rotation_Azimuth.setText(new Formatter().format("%.1f", Math.toDegrees(values[0])).toString()+"°");
            rotation_Pitch.setText(new Formatter().format("%.1f", Math.toDegrees(values[1])).toString()+"°");
            rotation_Roll.setText(new Formatter().format("%.1f", Math.toDegrees(values[2])).toString()+"°");
        }

        displaySensorState(context);
    }

    @Override
    /**Initiate the sensors. The sampling intervals can be set by two ways:
     * 1. Use given parameters such as SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI, SENSOR_DELAY_GAME and SENSOR_DELAY_FASTEST
     * 2. Use specific delay time in microseconds.
     * Attention: This is only a hint to the system. Events may be received faster or slower than the specified rate.
     * Usually events are received faster.
     */
    public void initSensor() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, magnetic,SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(this,accelerometer, 50000);
//        mSensorManager.registerListener(this,magnetic,50000);
        zeroTime = System.nanoTime();

        //Create the file. The file will be rewritten every time the methond initSensor() is called
        File fileMag = new File("/sdcard/", "DataMagnetometer.txt");
        File fileAcc = new File("/sdcard/", "DataAccelerometer.txt");
        File fileOri = new File("/sdcard/", "DataOrientation.txt");

        if (!fileMag.exists()) {
            Log.e(TAG, "onCreate: Directory not created");
        } else {Log.d(TAG, "onCreate: Directory created"); }
        try {
            writerMagnetometer = new BufferedWriter(new FileWriter(fileMag));
            writerMagnetometer.write("data\taccuracy\ttime(ns)\tMag1\tMag2\tMag3\n");
            writerMagnetometer.write("Start time:" + df.format(new Date()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writerAccelerometer = new BufferedWriter(new FileWriter(fileAcc));
            writerAccelerometer.write("data\taccuracy\ttime(ns)\tAcc1\tAcc2\tAcc3\n");
            writerAccelerometer.write("Start time:" + df.format(new Date()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writerOrientation = new BufferedWriter(new FileWriter(fileOri));
            writerOrientation.write("data\taccuracy\tinterval(ns)\tAzimuth\tPitch\tRoll\n");
            writerOrientation.write("Start time:" + df.format(new Date()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    /**Destroy sensor objects when finish using them
     */
    public void destroySensor() {
        try {
            writerOrientation.write("Over time:" + df.format(new Date()) + "\n");
            writerOrientation.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writerMagnetometer.write("Over time:" + df.format(new Date()) + "\n");
            writerMagnetometer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writerAccelerometer.write("Over time:" + df.format(new Date()) + "\n");
            writerAccelerometer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSensorManager.unregisterListener(this);
    }

    //transfer 3 magnetic field values (3 axes) and 3 accelerometer values (3 axes) into
    //3 orientation values (A)
    private void calculateOrientation() {
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values); //"values" here are orientation angles, it is a global attribute
    }

    @Override
    /**This method is the most important method in the class. It is called by the system automatically
     * when the sensor value changes (acceleration and magnetic field here)
     */
    public void onSensorChanged(SensorEvent event) {
        //To measure sampling intervals
        synchronized (this) {
            //When the accelerometer detects the change of acceleration, record the new value to the file
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
                long absoluteTime = System.nanoTime()-zeroTime;
                try {
                    writerAccelerometer.write(df.format(new Date())+"\t"+accuracy+"\t"+absoluteTime+"\t"+accelerometerValues[0]+"\t"+accelerometerValues[1]+"\t"+accelerometerValues[2]+"\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            //When the magnetic field sensor detects the change of acceleration, record the new value to the file
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;

                long absoluteTime = System.nanoTime()-zeroTime;
                try {
                    if(writerMagnetometer!=null){
                        writerMagnetometer.write(df.format(new Date())+"\t"+accuracy+"\t"+absoluteTime+"\t"+magneticFieldValues[0]+"\t"+magneticFieldValues[1]+"\t"+magneticFieldValues[2]+"\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //the 'Orientationdata' file records the change of three orientation angles by time,
                //but attention: the file writing is wrapped in the magnetic field sensor event
                //so the sampling time is the same as "MagnetometerData.txt". The only way to get
                //Orientation values is to
                try {
                    writerOrientation.write(df.format(new Date())+"\t"+accuracy+"\t"+absoluteTime+"\t"+Math.toDegrees(values[0])+"\t"+Math.toDegrees(values[1])+"\t"+Math.toDegrees(values[2])+"\n");
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

    public void displaySensorState(Activity context) {
        List<Sensor> Sensorlist= mSensorManager.getSensorList(Sensor.TYPE_ALL);
        List<Sensor> mSensorlist = new ArrayList<Sensor>();
        TreeSet numSet = new TreeSet();
        TextView tv;

        for (Sensor sensor : Sensorlist) {
            if (!numSet.contains(sensor.getType())){
                numSet.add(sensor.getType());
                mSensorlist.add(sensor);
            }
        }

        for (Sensor sensor : mSensorlist) {
            String text = ""
                    + " Resolution: " + sensor.getResolution()
                    + "\n\n Min Delay: " + sensor.getMinDelay()
                    + "\n\n Max Range: " + sensor.getMaximumRange();

            switch (sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    tv = (TextView) context.findViewById(R.id.item1);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    tv = (TextView) context.findViewById(R.id.item4);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                case Sensor.TYPE_GRAVITY:
                    tv = (TextView) context.findViewById(R.id.item5);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    tv = (TextView) context.findViewById(R.id.item6);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                case Sensor.TYPE_LIGHT:
                    tv = (TextView) context.findViewById(R.id.item3);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    tv = (TextView) context.findViewById(R.id.item2);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    tv = (TextView) context.findViewById(R.id.item7);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    tv = (TextView) context.findViewById(R.id.item8);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    tv = (TextView) context.findViewById(R.id.item9);
                    if (tv!=null) {
                        tv.setText(text);
                        tv.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
