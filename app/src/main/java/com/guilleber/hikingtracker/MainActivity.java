package com.guilleber.hikingtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

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

    private TextView mTestText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNameEdit = findViewById(R.id.name_edittext);
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
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GPSTrackingService.class);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        });

        mPauseButton = findViewById(R.id.pause_button);
        mPauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    mRefreshHandler.postDelayed(mRefreshRunnable,UPDATE_INTERVAL);
                } else {
                    mRefreshHandler.removeCallbacks(mRefreshRunnable);
                }
            }
        });

        mEndButton = findViewById(R.id.end_button);
        mEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNameEdit.setEnabled(true);
                mEndButton.setEnabled(false);
                mPauseButton.setVisibility(View.GONE);
                mStartButton.setVisibility(View.VISIBLE);
                mRefreshHandler.removeCallbacks(mRefreshRunnable);
                unbindService(connection);
            }
        });
        mEndButton.setEnabled(false);

        mTestText = findViewById(R.id.test_text);

        mRefreshHandler = new Handler();
        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                mTestText.setText(String.valueOf(mGPSService.dummyFunction()));
                mRefreshHandler.postDelayed(mRefreshRunnable, UPDATE_INTERVAL);
            }
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRefreshHandler.removeCallbacks(mRefreshRunnable);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GPSTrackingService.LocalBinder binder = (GPSTrackingService.LocalBinder) service;
            mGPSService = binder.getService();
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
}