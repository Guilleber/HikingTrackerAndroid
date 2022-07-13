package com.guilleber.hikingtracker;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

public class GPSTrackingService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private int counter = 0;

    private Location mCurrLocation = null;

    private Handler mRefreshHandler;
    private Runnable mRefreshRunnable;
    private final static int UPDATE_INTERVAL = 1000;

    private FusedLocationProviderClient mFusedLocationClient;

    public class LocalBinder extends Binder {
        GPSTrackingService getService() {
            return GPSTrackingService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mRefreshHandler = new Handler();
        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if(ContextCompat.checkSelfPermission(GPSTrackingService.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }

                mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    mCurrLocation = location;
                                }
                            }
                        });

                mRefreshHandler.postDelayed(mRefreshRunnable, UPDATE_INTERVAL);
            }
        };
        mRefreshHandler.postDelayed(mRefreshRunnable, UPDATE_INTERVAL);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public double getCurrAlt() {
        if(mCurrLocation != null)
            return mCurrLocation.getAltitude();
        else
            return 0.0;
    }

    public double getCurrLat() {
        if(mCurrLocation != null)
            return mCurrLocation.getLatitude();
        else
            return 0.0;
    }

    public double getCurrLng() {
        if(mCurrLocation != null)
            return mCurrLocation.getLongitude();
        else
            return 0.0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}