package com.example.orientation_detection.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.orientation_detection.R;
import com.example.orientation_detection.activities.MainActivity;
import com.example.orientation_detection.module.GetLight;
import com.example.orientation_detection.module.GetOrientation;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DataIntentService extends IntentService {

    public DataIntentService() {
        super("DataIntentService");
    }
    private GetOrientation orientation;
    private GetLight light;
    private static String TAG = "Service";
    private PowerManager.WakeLock wakeLock;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

    private FileOutputStream out;
    private BufferedWriter writer;
    private float [] orientationValues;
    private float lightValue;
    private long zeroTime;

    private DataBinder mBinder = new DataBinder();
    public class DataBinder extends Binder {
        public GetOrientation getOrientationData() { return orientation; }
        public GetLight getLightData() { return light; }
        public void setOrientationData (GetOrientation orientation) {
            DataIntentService.this.orientation = orientation;
        }
        public void setLightData (GetLight light) {
            DataIntentService.this.light = light;
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: 绑定成功");
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        //make the Service run on the foreground
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0, intent,0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("This is content title")
                .setContentText("This is content text")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
        startForeground(1,notification);

        orientation = new GetOrientation(DataIntentService.this);
        orientation.initSensor();
        light = new GetLight(DataIntentService.this);
        light.initSensor();

        //to control CPU not to pause when sensors are working, therefore we can still write data
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        wakeLock.acquire();

        try {
            out = this.openFileOutput("OrientationLightData.txt",Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write("time(ns)\tAzimuth\tPitch\tRoll\tLight\n");
            writer.write("Current time:" + df.format(new Date()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onHandleIntent: 我来了！");
                zeroTime = orientation.getZerotime();
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long currentTime = System.nanoTime()-zeroTime;
                    orientationValues = orientation.getValues();
                    lightValue = light.getLightValue();
                    try {
                        writer.write(df.format(new Date())+"\t"+currentTime+"\t"+Math.toDegrees(orientationValues[0])+"\t"+Math.toDegrees(orientationValues[1])+"\t"+Math.toDegrees(orientationValues[2])+"\t"+lightValue+"\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "run: 什么??");
                    orientation.refresh();
                }
            }
        }).start();
        return Service.START_STICKY;
    }

        @Override
    protected void onHandleIntent(Intent intent) {  //Everything done inside is already in the subthread
            //Log.d(TAG, "onHandleIntent: 我来了！");
//        while (true) {
//            try {
//                Thread.sleep(100000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            orientation.refresh();
//            Log.d(TAG, "onHandleIntent: orientation.refresh()");
//        }
        zeroTime = orientation.getZerotime();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long currentTime = System.nanoTime()-zeroTime;
            orientationValues = orientation.getValues();
            lightValue = light.getLightValue();
            try {
                writer.write(df.format(new Date())+"\t"+currentTime+"\t"+Math.toDegrees(orientationValues[0])+"\t"+Math.toDegrees(orientationValues[1])+"\t"+Math.toDegrees(orientationValues[2])+"\t"+lightValue+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Service 被销毁了");
        super.onDestroy();
        orientation.destroySensor();
        light.destroySensor();
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
