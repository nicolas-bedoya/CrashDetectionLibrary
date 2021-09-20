package com.example.crashdetectionlibrary;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SendSMS {
    public static void sendSMS(String[] Contact1, String[] Contact2, String[] User,
            String[] LocationPacket, Context context) {
        final String TAG = "SendSMS";
        try {
            double longitudeTmp = Double.parseDouble(LocationPacket[1]);
            double latitudeTmp = Double.parseDouble(LocationPacket[0]);

            String location_longitude = String.format("%.2f", longitudeTmp);
            String location_latitude = String.format("%.2f", latitudeTmp);
            Log.d(TAG, location_longitude + " " + longitudeTmp);
            String SMS1 = "Hi " + Contact1[0] + " " + Contact1[1] + ", " + User[0] +
                    " " + User[1] + " has experienced a crash. " + "They are located at (" + location_latitude
                    + "," + location_longitude + "), "  + LocationPacket[2];

            String SMS2 = "Hi " + Contact2[0] + " " + Contact2[1] + ", " + User[0] +
                    " " + User[1] + " has experienced a crash. " + "They are located at (" + location_latitude
                    + "," + location_longitude + "), "  + LocationPacket[2];

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(Contact1[2], null, SMS1, null, null);
            smsManager.sendTextMessage(Contact2[2], null, SMS2, null, null);

            Log.d(TAG, "message is sent");
            Log.d(TAG, SMS1);
            Toast.makeText(context, "Message is sent", Toast.LENGTH_SHORT).show();
        }

        catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "failed to send message");
            Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }
}
