package com.example.crashdetectionlibrary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AlertDialog extends AppCompatActivity {
    public static final int TIMEOUT = 120; // units - seconds
    public static final String TAG = "AlertDialog";

    NotificationManagerCompat notificationManager;
    public void AlertDialogAppear(Context context, String[] Emergency1, String[] Emergency2, String[] User,
                                  String[] LocationPacket) {

        //notificationManager = NotificationManagerCompat.from(NotificationType.class)
        final boolean[] impactConfirmed = {false};
        final boolean[] dismiss = {false};
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("WARNING")
                .setMessage("Have you experienced a crash?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, LocationPacket[0] + " " + LocationPacket[1] + " " + LocationPacket[2]);
                        SendSMS.sendSMS(Emergency1, Emergency2, User, LocationPacket, context);
                        impactConfirmed[0] = true;

                        Intent StopServiceIntent = new Intent(Globals.END_CRASH_CHECK);
                        LocalBroadcastManager.getInstance(AlertDialog.this).sendBroadcast(StopServiceIntent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss[0] = true;
                        Log.d(TAG, "broadcast sent from dismiss");
                        // send broadcast to register sensors again
                        Intent RegisterSensorsIntent = new Intent(Globals.ACTIVATE_SENSOR_REQUEST);
                        LocalBroadcastManager.getInstance(AlertDialog.this).sendBroadcast(RegisterSensorsIntent);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();

        Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int timeout = TIMEOUT;
            @Override
            public void run() {
                if (!impactConfirmed[0] && !dismiss[0]) {
                    if (timeout > 0) {
                        dialog.setMessage("Have you experienced a crash?\n" + timeout);
                        Log.d(TAG, "Timeout in: " + timeout);
                        timeout--;
                        handler.postDelayed(this, 1000);
                    }else {
                        SendSMS.sendSMS(Emergency1, Emergency2, User, LocationPacket, context);
                        impactConfirmed[0] = true;
                        dialog.dismiss();
                    }
                }
            }
        };
        handler.post(runnable);
    }


}
