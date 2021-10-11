package com.example.crashdetectionlibrary;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SendSMS {
    public static void sendSMS(String[] Emergency1, String[] Emergency2, String[] User,
            String[] LocationPacket, Context context) {
        final String TAG = "SendSMS";
        try {
            Log.d(TAG, "SMS -> lat: " + LocationPacket[1] + " long: " + LocationPacket[2]);
            double longitudeTmp = Double.parseDouble(LocationPacket[2]);
            double latitudeTmp = Double.parseDouble(LocationPacket[1]);

            String location_latitude = String.format("%.2f", latitudeTmp);
            String location_longitude = String.format("%.2f", longitudeTmp);

            String latitude_link = String.format("%.6f", latitudeTmp);
            String longitude_link = String.format("%.6f", longitudeTmp);

            String address = LocationPacket[0];

            Log.d(TAG, location_latitude + " " + location_longitude);
            Log.d(TAG, "sms - " + address);

            String SMS1 = "Hi " + Emergency1[0] + " " + Emergency1[1] + ", " + User[0] +
                    " " + User[1] + " may have experienced a crash. " + "They are located at " + address + " (" + location_latitude
                    + "," + location_longitude + ") " + LocationPacket[0] ;

            String SMS2 = "Hi " + Emergency2[0] + " " + Emergency2[1] + ", " + User[0] +
                    " " + User[1] + " may have experienced a crash. " + "They are located at " + address + " (" + location_latitude
                    + "," + location_longitude + ") "  + LocationPacket[0] ;

            String SMS_link = "http://maps.google.com/maps?q=" + latitude_link + "," + longitude_link + "+(My+Point)&z=14&ll=" + latitude_link + "," + longitude_link;

            // String SMS_link = "http://maps.google.com/?ie=UTF8&hq=&ll=" + latitude_link + "," + longitude_link + "&z=13";
            // String SMS_link = "https://www.google.com/maps/search/?api=1&query=" + longitude_link + "%2C" + latitude_link;
            // String SMS_link = "http://maps.google.com/maps?q=loc:" + latitude_link + "," + longitude_link; // working

            SmsManager smsManager = SmsManager.getDefault();

            smsManager.sendTextMessage(Emergency1[2], null, SMS1, null, null);
            smsManager.sendTextMessage(Emergency1[2], null, SMS_link, null, null);

            smsManager.sendTextMessage(Emergency2[2], null, SMS2, null, null);
            smsManager.sendTextMessage(Emergency2[2], null, SMS_link, null, null);

            Log.d(TAG, "messages sent");
            Log.d(TAG, SMS1);
            Log.d(TAG, SMS2);
            Toast.makeText(context, "Messages sent", Toast.LENGTH_SHORT).show();
        }

        catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "failed to send message");
            Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }
}
