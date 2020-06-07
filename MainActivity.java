package org.rydalinc.fluttergetmessages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.systemchannels.PlatformChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;


public class MainActivity extends FlutterActivity {
    String firebasseToekn = null;
    FlutterEngine engine;


    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        engine = flutterEngine;
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), "org.rydalinc.fluttergetmessages")
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("getFirebaseToken")) {
                                FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                                result.success(firebasseToekn);
                            }
                            if (call.method.equals("deleteFirebaseToken")) {
                                FirebaseMessaging.getInstance().setAutoInitEnabled(false);
                                new Thread(() -> {
                                    try {
                                        // Remove InstanceID initiate to unsubscribe all topic
                                        // TODO: May be a better way to use FirebaseMessaging.getInstance().unsubscribeFromTopic()
                                        FirebaseInstanceId.getInstance().deleteInstanceId();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }
                        }
                );

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter("DATA"));
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("ERROR", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        firebasseToekn = task.getResult().getToken();
                        Log.i("Token:", firebasseToekn);


                    }
                });

        if (getIntent().getExtras() != null) {
            String sent_time = "null";
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d("INCOMING:", "Key: " + key + " Value: " + value);
                if (key.equals("google.sent_time")) {
                    sent_time = value.toString();

                }

            }
            Context context = getActivity();
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("flutter.message_timestamp", sent_time);
            Log.i("SENT_TIME", sent_time);
            editor.commit();


        }

    }

    public void new_message_update(String action, String data) {
        MethodChannel channel = new MethodChannel(engine.getDartExecutor().getBinaryMessenger(), "platform_channel");
        channel.invokeMethod(action, data, new MethodChannel.Result() {
            @Override
            public void success(Object o) {
                Log.i("SENDING:", "success");

                // this will be called with o = "some string"
            }

            @Override
            public void error(String s, String s1, Object o) {
                Log.i("SENDING:", "failed");
            }

            @Override
            public void notImplemented() {
            }
        });
    }

    protected BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("AH", "Intent recieved");
            String timestamp = intent.getStringExtra("message");
            new_message_update("foo", timestamp);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }
}