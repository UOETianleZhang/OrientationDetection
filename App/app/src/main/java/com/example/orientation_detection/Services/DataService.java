package com.example.orientation_detection.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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

//Main Service
public class DataService extends Service {

    private GetOrientation orientation;
    private GetLight light;
    private static String TAG = "Service";
    private PowerManager.WakeLock wakeLock;
    private long zeroTime;

    private DataBinder mBinder = new DataBinder();
    public class DataBinder extends Binder {
        public GetOrientation getOrientationData() { return orientation; }
        public GetLight getLightData() { return light; }
        public void setOrientationData (GetOrientation orientation) {
            DataService.this.orientation = orientation;
        }
        public void setLightData (GetLight light) {
            DataService.this.light = light;
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
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

        //make the Service run on the foreground
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0, intent,0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("\"Sensors\" is running")
                .setContentText("Tap to go back.")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.logo1)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.logo1))
                .setContentIntent(pi)
//                .setPriority(Notification.)
                .build();
        startForeground(1,notification);

        orientation = new GetOrientation(DataService.this);
        orientation.initSensor();
        light = new GetLight(DataService.this);
        light.initSensor();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
            zeroTime = orientation.getZerotime();
            }
        }).start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        orientation.destroySensor();
        light.destroySensor();
    }
}
