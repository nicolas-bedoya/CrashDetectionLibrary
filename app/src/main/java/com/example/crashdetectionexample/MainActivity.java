package com.example.crashdetectionexample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.crashdetectionlibrary.CrashDetectionLogic;
import com.example.crashdetectionlibrary.SendSMS;
import com.example.crashdetectionlibrary.SensorLocation;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public Button button;

    //permissions must be added in application activity
    String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS
    };

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // permissions must be completely checked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Checking if all related permissions have not accepted by user
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(PERMISSIONS, 1);
            }

        }
        Button butStart = findViewById(R.id.butStart);
        butStart.setOnClickListener(this);

        Button butEnd = findViewById(R.id.butEnd);
        butEnd.setOnClickListener(this);
    }

    public void onClick(View v) {
        Button b = (Button) v;
        Intent intent = new Intent(this, ActivityService.class);

        switch (b.getId()) {
            case R.id.butStart:
                // this starts intent
                Log.d(TAG, "Hello");
                startService(intent);
                break;

            case R.id.butEnd:
                Log.d(TAG, "Goodbye");
                stopService(intent);
        }

    }


}