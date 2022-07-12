package com.guilleber.hikingtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

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
                mNameEdit.setEnabled(false);
                mEndButton.setEnabled(true);
                mStartButton.setVisibility(View.GONE);
                mPauseButton.setVisibility(View.VISIBLE);
            }
        });

        mPauseButton = findViewById(R.id.pause_button);

        mEndButton = findViewById(R.id.end_button);
        mEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mEndButton.setEnabled(false);
    }

    private EditText mNameEdit;
    private Button mStartButton;
    private ToggleButton mPauseButton;
    private Button mEndButton;
}