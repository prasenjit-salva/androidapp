package com.digitstory.testapplication.Services;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.digitstory.testapplication.Activity.ContactWatchActivity;
import com.digitstory.testapplication.Utils.ASingleton;
import com.digitstory.testapplication.Utils.Config;

public class PhoneUnlockedReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {

        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        Log.d(Config.TAG, "key pad event");
        if (keyguardManager.isKeyguardSecure() && Config.safeToStart()) {

            Log.d(Config.TAG, "Starting ContactWatchService");
            Intent myIntent = new Intent(context, ContactWatchService.class);
            context.startService(myIntent);

        }
    }


}