package com.example.orientation_detection.module;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.example.orientation_detection.R;

import org.w3c.dom.Text;

import java.util.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by tianlezhang on 2017/11/4.
 */

public class GetOrientation {
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetic;
    private Activity context;
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] values = new float[3];

    public GetOrientation(Activity context){
        this.context = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(new MySensorEventListener(), accelerometer, Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(new MySensorEventListener(), magnetic,Sensor.TYPE_MAGNETIC_FIELD);


    }

    private void calculateOrientation() {
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
       SensorManager.getOrientation(R, values);

    }

    public void displayOrientation() {
        TextView rotation_x = (TextView) context.findViewById(R.id.rotation_vector_x);
        TextView rotation_y = (TextView) context.findViewById(R.id.rotation_vector_y);
        TextView rotation_z = (TextView) context.findViewById(R.id.rotation_vector_z);
        //calculateOrientation();
            //Log.d("GetOrientation", "displayOrientation: loop");
            rotation_x.setText(new Formatter().format("%.1f", Math.toDegrees(values[0])).toString()+"°");
            rotation_y.setText(new Formatter().format("%.1f", Math.toDegrees(values[1])).toString()+"°");
            rotation_z.setText(new Formatter().format("%.1f", Math.toDegrees(values[2])).toString()+"°");
    }

    class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            }
            calculateOrientation();
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }


}
