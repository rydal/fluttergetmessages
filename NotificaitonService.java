package org.rydalinc.fluttergetmessages;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.flutter.app.FlutterApplication;
import io.flutter.plugin.common.PluginRegistry;

import static androidx.room.Room.*;

public class NotificationService  extends FirebaseMessagingService   {
    String TAG= "fluttergetmessages";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            // TODO(developer): Handle FCM messages here.
            // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
            Log.d(TAG, "From: " + remoteMessage.getFrom());



            AppDatabase db = databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "fluttergetmessages.db").build();
            Log.e("PARANOIA","KEY OF E");
            stopSelf();
        } catch (Exception ex) {
            Log.e("MYAPP", "exception", ex);
            stopSelf();
        }


        Map<String, String> data = remoteMessage.getData();

        String message = data.get("message");
        String silent = data.get("silent");
        String read = "0";
        String strDate = Long.toString(remoteMessage.getSentTime());


        Log.i("QUERY:",remoteMessage.toString());
        AppDatabase db = databaseBuilder(getApplicationContext(),
                AppDatabase.class, "fluttergetmessages.db").build();
        db.userDao().put_message(strDate,message,silent, read);
        Log.i("WOOP",db.userDao().getAll().toString());

        if (silent.equals(1)) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "fluttergetmessages notifications";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "fluttergetmessages notifications", NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                notificationChannel.setDescription("fluttergetmessages updates.");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    //     .setPriority(Notification.PRIORITY_MAX)
                    .setContentTitle(topic)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher);

            notificationManager.notify(/*notification id*/1, notificationBuilder.build());
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
            Intent localIntent = new Intent("DATA");
            Log.d(TAG, "sending intent");
            localIntent.putExtra("message", strDate);
            localBroadcastManager.sendBroadcast(localIntent);
        }
    }
}






