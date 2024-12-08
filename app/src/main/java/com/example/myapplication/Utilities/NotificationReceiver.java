package com.example.myapplication.Utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.Activities.Dash_Board;
import com.example.myapplication.R;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId;
        String task, task_des;
        task = intent.getStringExtra("task_name");
        task_des = intent.getStringExtra("task_description");
        notificationId = intent.getIntExtra("notification_id", 0);
        Intent in = new Intent(context, Dash_Board.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 3, in, PendingIntent.FLAG_IMMUTABLE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "deadline_notification")
                .setContentTitle(task)
                .setContentText(task_des)
                .setSmallIcon(R.drawable.app_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }
}
