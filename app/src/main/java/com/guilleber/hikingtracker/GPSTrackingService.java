package com.guilleber.hikingtracker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class GPSTrackingService extends Service {
    private final IBinder binder = new LocalBinder();
    private int counter = 0;

    public class LocalBinder extends Binder {
        GPSTrackingService getService() {
            return GPSTrackingService.this;
        }
    }

    public GPSTrackingService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public int dummyFunction() {
        counter++;
        return counter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}