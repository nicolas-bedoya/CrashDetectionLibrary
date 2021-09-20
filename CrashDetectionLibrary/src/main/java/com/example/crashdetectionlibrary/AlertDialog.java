package com.example.crashdetectionlibrary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class AlertDialog extends AppCompatActivity {
    public static final int TIMEOUT = 120; // units - seconds
    public static final String TAG = "AlertDialog";

    public static void AlertDialogAppear(Context context, String[] Emergency1, String[] Emergency2, String[] User,
                                         String[] LocationPacket) {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("WARNING")
                .setMessage("Have you experienced a crash?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SendSMS.sendSMS(Emergency1, Emergency2, User, LocationPacket, context);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        dialog.show();

        Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int timeout = TIMEOUT;
            @Override
            public void run() {
                if(timeout > 0) {
                    dialog.setMessage("Have you experienced a crash?\n" + timeout);
                    Log.d(TAG, "Timeout in: " + timeout);
                    timeout--;
                    handler.postDelayed((Runnable) context, 1000);

                } else {
                    if (dialog.isShowing()) {
                        // send broadcast to stop service
                    }
                }
            }

        };
        handler.post(runnable);
    }


}
