package com.example.crashdetectionexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.crashdetectionlibrary.AlertDialog;
import com.example.crashdetectionlibrary.Globals;
import com.example.crashdetectionlibrary.SendSMS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public Button button;
    boolean impactConfirmed = false;

    //permissions must be added in application activity
    String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS
    };

    String[] LocationPacket;
    // contacts hardcoded for now, however the library can be adapted to allow for input from user
    String[] Emergency1 = {"Will", "Dunn", "0423100771"};
    String[] Emergency2 = {"Liam", "Carloss", "0449866461"};
    String[] User = {"Nicolas", "Bedoya"};

    Context mContext = this;

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
        // mMessageReceiverAlertDialog used to receive broadcast from ActivityService to call AlertDialog.class
        // which allows for the dialog to appear on the MainActivity
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverAlertDialog,
                new IntentFilter(Globals.ALERT_DIALOG_REQUEST));

        // mMessageReceiverStopService used to receive broadcast from ActivityService to stop foreground Service
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverStopService,
                new IntentFilter(Globals.END_CRASH_CHECK));

        // mMessageReceiverStartService used to receive broadcast from ActivityService to start foreground service
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverStartService,
                new IntentFilter(Globals.ACTIVATE_SENSOR_REQUEST));

        Button butStart = findViewById(R.id.butStart);
        butStart.setOnClickListener(this);

        Button butEnd = findViewById(R.id.butEnd);
        butEnd.setOnClickListener(this);
    }

    private BroadcastReceiver mMessageReceiverAlertDialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "alert dialog appearance");
            LocationPacket = intent.getStringArrayExtra("Location-Packet");
            AlertDialog mAlertDialog = new AlertDialog();
            mAlertDialog.AlertDialogAppear(mContext, Emergency1, Emergency2, User, LocationPacket);

        }
    };

    private BroadcastReceiver mMessageReceiverStopService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Service stop");
            Intent ServiceIntent = new Intent(MainActivity.this, ActivityService.class);
            stopService(ServiceIntent);

            impactConfirmed = true;
            SendSMS.sendSMS(Emergency1, Emergency2, User, LocationPacket, mContext);
            Log.d(TAG, "Crash confirmed, calling SMS now... impact confirmed = " + impactConfirmed);
        }
    };

    private BroadcastReceiver mMessageReceiverStartService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "sensor restart called");
            impactConfirmed = false;
            Intent ServiceIntent = new Intent(MainActivity.this, ActivityService.class);
            startService(ServiceIntent);
        }
    };

    private BroadcastReceiver mMessageReceiverSendSMS = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            impactConfirmed = true;
            SendSMS.sendSMS(Emergency1, Emergency2, User, LocationPacket, mContext);
            Log.d(TAG, "Crash confirmed, calling SMS now... impact confirmed = " + impactConfirmed);
        }
    };

    public void onClick(View v) {
        Button b = (Button) v;
        Intent ServiceIntent = new Intent(this, ActivityService.class);

        switch (b.getId()) {
            case R.id.butStart:
                // this starts intent
                Log.d(TAG, "Hello");
                startService(ServiceIntent);
                break;

            case R.id.butEnd:
                Log.d(TAG, "Goodbye");
                stopService(ServiceIntent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Intent ServiceIntent = new Intent(this, ActivityService.class);
        stopService(ServiceIntent);
        super.onDestroy();
    }
}