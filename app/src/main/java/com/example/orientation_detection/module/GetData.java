package com.example.orientation_detection.module;

import android.app.Activity;

/**
 * Created by tianlezhang on 2017/11/9.
 */

public abstract class GetData {
    public abstract void initSensor();
    public abstract void displaySensor(Activity context);
    public abstract void destroySensor();
}
