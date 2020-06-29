package com.digitstory.testapplication.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.digitstory.testapplication.Utils.Config;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Config.TAG, "MyReceiver on boot completed");

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)
        || intent.getAction().equals(Intent.ACTION_REBOOT)) {
            Log.d(Config.TAG, "INSIDE MyReceiver.ACTION_BOOT_COMPLETED_FILTER");
            if(Config.safeToStart()) {
                Log.d(Config.TAG, "INSIDE MyReceiver.ACTION_BOOT_COMPLETED_FILTER and safe to start filter");
                Log.d(Config.TAG, "" + Config.isNotificationServiceEnabled(context));
               // Intent notificationIntent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
               // context.startActivity(notificationIntent);
                ContactsFetcher cf = new ContactsFetcher(context);
                cf.getContactList();
                Intent myIntent = new Intent(context, ContactWatchService.class);
                context.startService(myIntent);

                /*
                Intent i = new Intent("com.digitstory.testapplication.Services.ContactWatchService");
                i.setClass(context, ContactWatchService.class);
                context.startService(i);
                 */
            }
        }


    }
}