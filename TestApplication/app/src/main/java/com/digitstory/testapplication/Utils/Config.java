package com.digitstory.testapplication.Utils;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;


import com.digitstory.testapplication.Activity.ContactWatchActivity;
import com.digitstory.testapplication.Services.ContactWatchService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Config {
    public static final String TAG = "MY_APP";
    public static final String CONTACT_SVC = "CONTACT_OBSERVER_SVC";
    public static final long DIFF_IN_HOURS = 12;
    public static final int NUM_OF_CALL_LOGS = 10;

    public static String[] ANDROID_PERMISSIONS = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
    };

    private Set<String> numbers = new LinkedHashSet<String>();
    private Set<String> mails = new LinkedHashSet<String>();

    public Config(){
        numbers.add("0123456789");
        mails.add("abc@xyz.com");
    }

    public void addNumbers(List<String> list){
        numbers.addAll(list);
    }
    public void addNumbers(Set<String> list){
        numbers.addAll(list);
    }

    public void addNumbers(String number){
        numbers.add(number);
    }

    public void addMails(List<String> list){
        mails.addAll(list);
    }
    public void addMails(Set<String> list){
        mails.addAll(list);
    }

    public void addMails(String mail){
        mails.add(mail);
    }

    public boolean isNumberFound(String str){
        return str!= null ?  numbers.contains(str.trim()): false;
    }

    public boolean isMailFound(String str){
        return str!= null ?  mails.contains(str.trim()): false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Found Blocked List::");
        for(String s: numbers){
            sb.append(s).append(", ");
        }
        for(String s: mails){
            sb.append(s).append(", ");
        }
        return sb.toString();
    }

    public static  boolean safeToStart() {
        /*
        First time when application gets installed => this flag will be true
        and after restart  it will be false
         */
        Log.d(Config.TAG, "ContactWatchActivity.isAlreadyInstalled:" + ContactWatchActivity.isAlreadyInstalled + " , ContactWatchService.isRunning:" + ContactWatchService.isRunning);
        if(ContactWatchActivity.isAlreadyInstalled)
        {
            return false;
        }
        if(ContactWatchService.isRunning)
        {
            return false;
        }
        return true;
    }

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    public static boolean isNotificationServiceEnabled(Context ctx){
        String pkgName = ctx.getPackageName();
        final String flat = Settings.Secure.getString(ctx.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
