package com.example.bs.testjob;

import android.app.Service;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;
import android.os.Build;

public class HideNotificationService extends Service
{
    public HideNotificationService()
    {
    }

    @Override
    public void onCreate()
    {
        Log.d("DestroyService", "Service: onCreate");
        Notification.Builder builder = new Notification.Builder(this);
        //.setSmallIcon(R.drawable.ic_launcher);
        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = builder.getNotification();
        else
            notification = builder.build();

        startForeground(777, notification);
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("DestroyService", "Service: onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("DestroyService", "Service: onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Log.d("DestroyService", "Service: onTaskRemoved");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
