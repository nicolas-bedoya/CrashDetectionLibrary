package com.example.crashdetectionexample;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.example.crashdetectionlibrary.NotificationBase.CHANNEL_ID;

import com.example.crashdetectionlibrary.AlertDialog;
import com.example.crashdetectionlibrary.Globals;
import com.example.crashdetectionlibrary.SendSMS;
import com.example.crashdetectionlibrary.SensorLocation;
import com.example.crashdetectionlibrary.NotificationType;


public class ActivityService extends Service implements LocationListener, SensorEventListener {

    MainActivity mainActivity = new MainActivity();

    private static final String TAG = "ActivityService";
    protected LocationManager locationManager;
    public SensorManager sensorManager;
    private static final int DELAY = 10000;

    Sensor accelerometer, gyroscope, magnetometer;
    NotificationManagerCompat notificationManager;

    double[] sensorData = new double[4];
    double[] accelerationData = new double[4];
    double[] gyroscopeData = new double [4];

    double[] LocationDetails = new double[3];
    int currentVelocity, previousVelocity; // used for finding velocity change, units in m/s

    double longitude, latitude;

    String[] LocationPacket; // stores location details in string array which is passed into sendSMS for when
    // crash is detected

    boolean firstInstance = true; // used for logic in finding the current and previous velocity
    boolean impactGyroDetected = false;
    boolean impactAccelDetected = false;
    boolean impactVelocityDetected = false; // set to true if change in velocity is greater than 10m/s
    boolean impactDetected = false;

    int impactVelocityTimer = 0;
    int impactSensorTimer = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand (location) called");
        // starting foreground, therefore notification has to be made clear to alert user
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

        registerUpdates();
    }

    // called to register sensor and location listeners
    // resets flags for detection to occur with a fresh slate
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
        // sensorManager.registerListener(ActivityService.this, magnetometer, DELAY);

        impactDetected = false;
        impactVelocityDetected = false;
        impactGyroDetected = false;
        impactAccelDetected = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorEventListener listener = this;
        if (!impactGyroDetected && !impactDetected && !impactAccelDetected) {
            sensorData = SensorLocation.SensorChanged(event, listener);
            //sensorData = {dataX, dataY, dataZ, ID, CrashFlag}
            // ID -> checks whether it relates to gyroscope or acceleration
            // CrashFlag -> checks whether a crash is detected

            // NOTE: x,y,z might not need to be passed into the application from library
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
                    impactVelocityDetected = true; // remove me
                }
            }
        }

        // CrashCheck here, checks if all flags are triggered to TRUE
        // If they are, then the alertDialog is processed and added to screen
        if ((impactAccelDetected || impactGyroDetected)  && impactVelocityDetected) {
            impactAccelDetected = false;
            impactGyroDetected = false;
            impactVelocityDetected = false;
            Log.d(TAG, "impactDetected = " + impactDetected);

            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, gyroscope);
            locationManager.removeUpdates(this);

            NotificationType.ProvideCrashNotification(this, notificationManager);
            Log.d(TAG, "lat: " + latitude + " long: " + longitude);
            LocationPacket = SensorLocation.getCompleteAddressString(this, latitude, longitude);

            Intent alertIntent = new Intent(Globals.ALERT_DIALOG_REQUEST);
            alertIntent.putExtra("Location-Packet", LocationPacket);
            Log.d(TAG, LocationPacket[0] + " " + LocationPacket[1] + " " + LocationPacket[2]);

            LocalBroadcastManager.getInstance(ActivityService.this).sendBroadcast(alertIntent);
            stopSelf();

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
        // try and add this into sensorLocation to filter out as much as possible from the app
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

            latitude = LocationDetails[0];
            longitude = LocationDetails[1];

            if (LocationDetails[2] == Globals.IMPACT_TRUE) {
                impactVelocityDetected = true;
            }
        } else {
            impactVelocityTimer++;
            if (impactVelocityTimer == 4) {
                impactVelocityDetected = false;
                impactAccelDetected = false;
                impactGyroDetected = false;
                impactVelocityTimer = 0;
            }
        }

        // This is implemented here since location is checked every second, therefore the impact
        // accel and gyro can be reset after 3 seconds within the location listener
        if (impactGyroDetected || impactAccelDetected) {
            impactSensorTimer++;
            Log.d(TAG, "impactSensorTimer " + impactSensorTimer);
            if (impactSensorTimer == 4) {
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
        locationManager.removeUpdates(this);

        impactDetected = false;
        impactVelocityDetected = false;
        impactGyroDetected = false;
        impactAccelDetected = false;
        stopForeground(true);
        super.onDestroy();
    }
}
