package com.example.crashdetectionlibrary;

import android.app.Notification;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.Locale;

import static com.example.crashdetectionlibrary.NotificationBase.CHANNEL_ID;

public class SensorLocation implements Globals {

    protected Context context;
    private static final int TIMEOUT = 10; // units seconds
    private static final int DELAY = 10000; // units in milliseconds
    private static final String TAG = "ActivityService";

    //public SensorLocation() {}

    public static double[] LocationChanged(@NonNull Location location, int currentVelocity, int previousVelocity) {
        double[] LocationChangeArray;
        int impactVelocity = 0;
        int longitude = (int)location.getLongitude();
        int latitude = (int)location.getLatitude();
        int velocityChange = currentVelocity - previousVelocity;

        // previous velocity is greater than current velocity in moments of crash, therefore
        // check to see if velocity change is negative
        if (velocityChange < 0) {
            // if the velocity change is greater than 10, set impactVelocity to true
            if (Math.abs(velocityChange) > 10) {
                impactVelocity = 1;
            }
        }
        LocationChangeArray = new double[]{latitude,longitude, impactVelocity};

        Log.d(TAG, "Lat: " + latitude + " Long: " + longitude + " VChange: " + velocityChange);
        return LocationChangeArray;
    }

    // change the type to a list of arrays of doubles
    public static double[] SensorChanged(SensorEvent event, SensorManager sensorManager) {
        double sensorFlag = 0;
        // impactSensorType not used yet
        double[] SensorChangedArray = new double[4];
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorFlag = 1;
            double xG = event.values[0];
            double yG = event.values[1];
            double zG = event.values[2];

            double impactGyroscope = 0;
            double[] gyroscopeData = new double[3];
            gyroscopeData[0]= xG; gyroscopeData[1] = yG; gyroscopeData[2] = zG;

            // gyroscope value of 25rad/s was allocated as a threshold for now
            if (Math.abs(xG) > 25 || Math.abs(yG) > 25 || Math.abs(zG) > 25) {
                impactGyroscope = 1;
                Log.d(TAG, "impactGyroscope true");
            }
            SensorChangedArray = new double[]{xG, yG, zG, sensorFlag, impactGyroscope};
        }

        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorFlag = 2;
            double xA = event.values[0];
            double yA = event.values[1];
            double zA = event.values[2];

            double[] accelerationData = new double[3];
            accelerationData[0] = xA; accelerationData[1] = yA; accelerationData[2] = zA;
            double impactAccelerometer = 0;

            // acceleration of 35 was allocated as a threshold for now
            if (Math.abs(xA) > 35 || Math.abs(yA) > 35 || Math.abs(zA) > 35 ) {
                impactAccelerometer = 1;
                Log.d(TAG, "impactAccelerometer true");
            }

            SensorChangedArray = new double[]{xA, yA, zA, sensorFlag, impactAccelerometer};

        } else {
            SensorChangedArray = new double[]{0, 0, 0, 0};
        }
        return SensorChangedArray;
    }

    // finding the address of user from geocoder
    public static String[] getCompleteAddressString(Context context, double latitude, double longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.d(TAG, strReturnedAddress.toString());
            } else {
                Log.d(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Cannot get Address!");
        }
        return new String[]{strAdd, String.valueOf(latitude), String.valueOf(longitude)};
    }

}
