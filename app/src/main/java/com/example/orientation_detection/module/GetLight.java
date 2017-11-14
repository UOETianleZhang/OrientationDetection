package com.example.orientation_detection.module;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.example.orientation_detection.R;

import java.util.Formatter;

/**
 * Created by tianlezhang on 2017/11/14.
 */

public class GetLight extends GetData implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mLight;
    private Activity context;
    private float lightValue;


    public GetLight(Activity context) {
        this.context = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }
    @Override
    public void initSensor() {
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_LIGHT);
    }

    @Override
    public void destroySensor() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void displaySensor() {
        TextView lightText =(TextView) context.findViewById(R.id.light_value);
        if(lightText!=null)
            lightText.setText(new Formatter().format("%.1f",lightValue).toString()+"\t\tlx");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        lightValue = event.values[0];
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
