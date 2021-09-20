package com.example.crashdetectionlibrary;

import static com.example.crashdetectionlibrary.NotificationBase.CHANNEL_ID;

import android.app.Notification;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationType {
    public static void ProvideCrashNotification(Context context, NotificationManagerCompat notificationManager) {
        Notification crashNotification = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            crashNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Crash Detected")
                    .setContentText("Have you experienced a crash?")
                    .setSmallIcon(R.drawable.ic_vintage)
                    //.setPriority(NotificationCompat.PRIORITY_HIGH);
                    //.addAction(R.mipmap.ic_launcher_round, "Yes", actionConfirmIntent)
                    //.addAction(R.mipmap.ic_launcher, "Dismiss", actionDismissIntent)
                    //.setContentIntent(intent)
                    .build();
        }

        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(2, crashNotification);
    }

    public static Notification ProvideServiceNotification(Context context) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("location service")
                .setSmallIcon(R.drawable.ic_vintage)
                .build();

        return notification;
    }

}
