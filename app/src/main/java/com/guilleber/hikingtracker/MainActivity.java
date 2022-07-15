package com.guilleber.hikingtracker;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText mNameEdit;
    private Button mStartButton;
    private ToggleButton mPauseButton;
    private Button mEndButton;

    private Handler mRefreshHandler;
    private Runnable mRefreshRunnable;
    private final static int UPDATE_INTERVAL = 1000;

    private GPSTrackingService mGPSService;
    private boolean mGPSBounded = false;

    private TextView mLat;
    private TextView mLng;
    private TextView mAlt;

    private OfflineMap mOfflineMap;
    private ElevationMap mElevationMap;

    private final InputFilter mFilter = (source, start, end, dest, dstart, dend) -> {
        return source.toString().substring(start, end).replace(" ", "_").replaceAll("[^_a-zA-Z0-9]", "");
    };

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GPSTrackingService.LocalBinder binder = (GPSTrackingService.LocalBinder) service;
            mGPSService = binder.getService();
            mOfflineMap.setPosMemory(mGPSService.getLatMemory(), mGPSService.getLngMemory());
            mElevationMap.setAltMemory(mGPSService.getAltMemory());
            mPauseButton.setChecked(true);
            mGPSBounded = true;
            mNameEdit.setEnabled(false);
            mEndButton.setEnabled(true);
            mStartButton.setVisibility(View.GONE);
            mPauseButton.setVisibility(View.VISIBLE);
            mRefreshHandler.postDelayed(mRefreshRunnable,UPDATE_INTERVAL);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mGPSBounded = false;
            mRefreshHandler.removeCallbacks(mRefreshRunnable);
            mNameEdit.setEnabled(true);
            mEndButton.setEnabled(false);
            mPauseButton.setVisibility(View.GONE);
            mStartButton.setVisibility(View.VISIBLE);
        }
    };

    private final ActivityResultLauncher<String[]> requestLocationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    Intent intent = new Intent(this, GPSTrackingService.class);
                    intent.putExtra("Name", mNameEdit.getText().toString());
                    startForegroundService(intent);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNameEdit = findViewById(R.id.name_edittext);
        mNameEdit.setFilters(new InputFilter[] {mFilter});
        mNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mStartButton.setEnabled(!s.toString().isEmpty());
            }
        });

        mStartButton = findViewById(R.id.start_button);
        mStartButton.setEnabled(false);
        mStartButton.setOnClickListener(v -> requestLocationPermission.launch(new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE
        }));

        mPauseButton = (ToggleButton) findViewById(R.id.pause_button);
        mPauseButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b && mGPSBounded) {
                mGPSService.resume();
                mRefreshHandler.postDelayed(mRefreshRunnable,UPDATE_INTERVAL);
            } else {
                mGPSService.pause();
                mRefreshHandler.removeCallbacks(mRefreshRunnable);
            }
        });

        mEndButton = findViewById(R.id.end_button);
        mEndButton.setOnClickListener(v -> {
            mNameEdit.setEnabled(true);
            mEndButton.setEnabled(false);
            mPauseButton.setVisibility(View.GONE);
            mStartButton.setVisibility(View.VISIBLE);
            mRefreshHandler.removeCallbacks(mRefreshRunnable);
            unbindService(connection);
            mGPSBounded = false;
            Intent intent = new Intent(v.getContext(), GPSTrackingService.class);
            stopService(intent);
        });
        mEndButton.setEnabled(false);

        mAlt = findViewById(R.id.alt);
        mLat = findViewById(R.id.lat);
        mLng = findViewById(R.id.lng);

        mOfflineMap = findViewById(R.id.offline_map);
        mElevationMap = findViewById(R.id.elevation_map);

        mRefreshHandler = new Handler();
        mRefreshRunnable = () -> {
            mAlt.setText((int) mGPSService.getCurrAlt() + " m");
            mLat.setText(String.format("%.2f", mGPSService.getCurrLat()));
            mLng.setText(String.format("%.2f", mGPSService.getCurrLng()));
            mOfflineMap.invalidate();
            mElevationMap.invalidate();
            mRefreshHandler.postDelayed(mRefreshRunnable, UPDATE_INTERVAL);
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRefreshHandler.removeCallbacks(mRefreshRunnable);
    }
}