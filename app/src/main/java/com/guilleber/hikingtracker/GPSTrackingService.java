package com.guilleber.hikingtracker;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Vector;

public class GPSTrackingService extends Service {
    private final IBinder mBinder = new LocalBinder();

    private String mName;
    private final int mMinDistBetween = 10;

    private Location mCurrLocation = null;
    private Vector<Double> mLatMemory = new Vector<>();
    private Vector<Double> mLngMemory = new Vector<>();
    private Vector<Integer> mAltMemory = new Vector<>();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private final LocationRequest mLocationRequest = LocationRequest.create();

    public class LocalBinder extends Binder {
        GPSTrackingService getService() {
            return GPSTrackingService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mName = (String)intent.getExtras().get("Name");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setMaxWaitTime(1000);
        mLocationRequest.setMaxWaitTime(1000);
        mLocationRequest.setSmallestDisplacement(5);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Log.d("LAT", String.valueOf(location.getLatitude()));
                    mCurrLocation = location;

                    if(mLatMemory.size() > 0) {
                        float[] results = new float[1];
                        Location.distanceBetween(mLatMemory.lastElement(), mLngMemory.lastElement(), location.getLatitude(), location.getLongitude(), results);
                        if(results[0] >= mMinDistBetween) {
                            mLatMemory.add(location.getLatitude());
                            mLngMemory.add(location.getLongitude());
                            mAltMemory.add((int)location.getAltitude());
                        }
                    } else {
                        mLatMemory.add(location.getLatitude());
                        mLngMemory.add(location.getLongitude());
                        mAltMemory.add((int)location.getAltitude());
                    }
                }
            }
        };

        startLocationUpdates();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void resume() {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ContextCompat.checkSelfPermission(GPSTrackingService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
    }

    protected void pause() {
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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

    public Vector<Double> getLatMemory() {
        return mLatMemory;
    }

    public Vector<Double> getLngMemory() {
        return mLngMemory;
    }

    public Vector<Integer> getAltMemory() {
        return mAltMemory;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}