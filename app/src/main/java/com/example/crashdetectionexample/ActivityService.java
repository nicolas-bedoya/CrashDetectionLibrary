package com.example.crashdetectionexample;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.example.crashdetectionlibrary.NotificationBase.CHANNEL_ID;

import com.example.crashdetectionlibrary.AlertDialog;
import com.example.crashdetectionlibrary.Globals;
import com.example.crashdetectionlibrary.SendSMS;
import com.example.crashdetectionlibrary.SensorLocation;
import com.example.crashdetectionlibrary.NotificationType;


public class ActivityService extends Service implements LocationListener, SensorEventListener {

    private static final String TAG = "ActivityService";
    protected LocationManager locationManager;
    public SensorManager sensorManager;
    private static final int DELAY = 10000;

    Sensor accelerometer, gyroscope, magnetometer;
    NotificationManagerCompat notificationManager;
    double xA, yA, zA;
    double xG, yG, zG;
    double xM, yM, zM;

    double[] sensorData = new double[4];
    double[] accelerationData = new double[4];
    double[] gyroscopeData = new double [4];

    double[] LocationDetails = new double[3];
    int currentVelocity, previousVelocity;

    double longitude, latitude;

    String[] Emergency1 = {"Will", "Dunn", "0423100771"};
    String[] Emergency2 = {"Liam", "Carloss", "0449866461"};
    String[] User = {"Nicolas", "Bedoya"};

    String[] LocationPacket;

    boolean firstInstance = true;
    boolean impactGyroDetected = false, impactAccelDetected = false;
    boolean impactVelocityDetected = false;
    boolean impactDetected = false;

    int impactVelocityTimer = 0;
    int impactSensorTimer = 0;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand (location) called");
        // starting foreground, therefore notification has to be made clear to alert user
        // channel ID obtained from ActivityNotification so notifications
        Notification ServiceNotification;
        ServiceNotification = NotificationType.ProvideServiceNotification(this);
        startForeground(1, ServiceNotification);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        registerUpdates();
    }

    public void registerUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // given that the permission has already been accepted by the user, permission will
            // not need to be given
            return;
        }
        // updating location every 50ms (but really its approximately 1 second)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, this);
        sensorManager.registerListener(ActivityService.this, accelerometer, DELAY);
        sensorManager.registerListener(ActivityService.this, gyroscope, DELAY);
        sensorManager.registerListener(ActivityService.this, magnetometer, DELAY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!impactGyroDetected && !impactDetected && !impactAccelDetected) {
            sensorData = SensorLocation.SensorChanged(event, sensorManager);

            if (sensorData[3] == Globals.GYROSCOPE_ID) {
                gyroscopeData = sensorData;
                if (gyroscopeData[4] == Globals.IMPACT_TRUE) {
                    Log.d(TAG, "impact gyroscope detected");
                    impactGyroDetected = true;
                }
            }
            else if (sensorData[3] == Globals.ACCELERATION_ID) {
                accelerationData = sensorData;
                if (accelerationData[4] == Globals.IMPACT_TRUE) {
                    Log.d(TAG, "impact acceleration detected");
                    impactAccelDetected = true;
                }
            }
        }
        impactVelocityDetected = true; // remove me
        if ((impactAccelDetected || impactGyroDetected) && impactVelocityDetected) {
            impactAccelDetected = false;
            impactGyroDetected = false;
            impactVelocityDetected = false;
            Log.d(TAG, "impactDetected = " + impactDetected);
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, gyroscope);
            sensorManager.unregisterListener(this, magnetometer);
            locationManager.removeUpdates(this);
            NotificationType.ProvideCrashNotification(this, notificationManager);
            LocationPacket = SensorLocation.getCompleteAddressString(this, longitude, latitude);
            AlertDialog.AlertDialogAppear(this, Emergency1, Emergency2, User, LocationPacket);
            //SendSMS.sendSMS(Emergency1, Emergency2, User, LocationPacket,this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "hello location");
        if (!impactVelocityDetected) {
            if (firstInstance) {
                currentVelocity = (int) ((int) location.getSpeed() * 3.6); // units in km/h
                firstInstance = false;
            } else {
                previousVelocity = currentVelocity;
                currentVelocity = (int) ((int) location.getSpeed() * 3.6); // units in km/h
            }
            LocationDetails = SensorLocation.LocationChanged(location, currentVelocity, previousVelocity);
            if (LocationDetails[2] == Globals.IMPACT_TRUE) {
                impactVelocityDetected = true;
            }
        } else {
            impactVelocityTimer++;
            if (impactVelocityTimer == 3) {
                impactVelocityDetected = false;
                impactAccelDetected = false;
                impactGyroDetected = false;
                impactVelocityTimer = 0;
            }
        }

        if (impactGyroDetected || impactAccelDetected) {
            impactSensorTimer++;
            Log.d(TAG, "impactSensorTimer " + impactSensorTimer);
            if (impactSensorTimer == 3) {
                impactVelocityDetected = false;
                impactAccelDetected = false;
                impactGyroDetected = false;
                impactSensorTimer = 0;
            }
        }
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, gyroscope);
        sensorManager.unregisterListener(this, magnetometer);
        locationManager.removeUpdates(this);
        impactDetected = false;
        impactVelocityDetected = false;
        impactGyroDetected = false;
        impactAccelDetected = false;
        super.onDestroy();
    }
}
