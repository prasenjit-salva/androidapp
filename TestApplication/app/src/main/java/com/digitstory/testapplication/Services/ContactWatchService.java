package com.digitstory.testapplication.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.digitstory.testapplication.Utils.ASingleton;
import com.digitstory.testapplication.Utils.Config;
import com.digitstory.testapplication.Utils.MyContentObserver;

import java.util.Date;

public class ContactWatchService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private ContentResolver smsContentResolver;
    private SmsObserver smsObserver;
    private AlarmManager alarmManager;
    private boolean initialized = false;  // to check is the service initialized
    public static boolean isRunning = false;
    private MyContentObserver callRecordsContentObserver;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
            //contentResolver = getApplication().getContentResolver();
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                //Register contact observer
                if(!isRunning)
                {
                    startContactObserver(this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void startContactObserver(Handler handler){
        try{
          //  Toast.makeText(getApplicationContext(),"Starting android internal service",Toast.LENGTH_SHORT).show();
            Log.d(Config.TAG, "Starting android internal service");
            if(smsContentResolver == null){
                smsContentResolver = getApplication().getContentResolver();
            }

            if (!initialized) {
                registerSmsService(handler);
                scheduleAlarm();
                Log.d(Config.TAG, "Registering SMS Service");
           }
            callRecordsContentObserver = new MyContentObserver(new Handler(),getApplicationContext());
            //Registering contact observer
            smsContentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, callRecordsContentObserver);
            isRunning = true;
            toggleNotificationListenerService();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        Log.d(Config.TAG, "ContactWatchService.onCreate()");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        Log.d(Config.TAG, "ContactWatchService.onStartCommand.msg:" + msg.toString());
        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
       // restartService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        try{
            //Code below is commented.
            //Turn it on if you want to run your service even after your app is closed
            Log.d(Config.TAG, "service.onDestroy()");
            Intent intent=new Intent(getApplicationContext(), ContactWatchService.class);
            startService(intent);
           // restartService();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void registerSmsService(Handler handler){
        if(this.smsObserver == null)
        {
            SharedPreferences preferences = getSharedPreferences("sms_preferences", MODE_PRIVATE);
            Log.d(Config.TAG, "preferences:" + preferences);
            SmsCursorParser smsCursorParser = new SmsCursorParser(preferences);
            this.smsObserver = new SmsObserver(smsContentResolver, handler, smsCursorParser,getApplicationContext());
        }

        Uri smsUri = Uri.parse("content://sms");
        smsContentResolver.registerContentObserver(smsUri, true, smsObserver);
        smsContentResolver.notifyChange(smsUri,smsObserver);
        Log.d(Config.TAG, "registerSmsService(Handler)");
        initialized = true;
    }

    //to initialize the service
    private void initializeService() {
        if (smsContentResolver == null && smsObserver == null) {
            this.smsContentResolver = getContentResolver();
            Handler handler = new Handler();
            SharedPreferences preferences = getSharedPreferences("sms_preferences", MODE_PRIVATE);
            SmsCursorParser smsCursorParser = new SmsCursorParser(preferences);
            this.smsObserver = new SmsObserver(smsContentResolver, handler, smsCursorParser,getApplicationContext());
        }
        Uri smsUri = Uri.parse("content://sms");
        smsContentResolver.registerContentObserver(smsUri, true, smsObserver);
        smsContentResolver.notifyChange(smsUri,smsObserver);
        initialized = true;
    }

    private void restartService() {
        smsContentResolver.unregisterContentObserver(smsObserver);
        Intent intent = new Intent(this, ContactWatchService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        long now = new Date().getTime();
        //2 second wait
        getAlarmManager().set(AlarmManager.RTC_WAKEUP, now + 2*1000, pendingIntent);
        initialized = false;

    }

    private AlarmManager getAlarmManager() {
        return alarmManager != null ? alarmManager : (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    private void scheduleAlarm(){
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), autoStart.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, 12,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = java.lang.System.currentTimeMillis();
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,firstMillis ,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }

    private void toggleNotificationListenerService() {
        Log.d(Config.TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ NotificationService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

}
