package com.digitstory.testapplication.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;

import com.digitstory.testapplication.Utils.Event;
import com.digitstory.testapplication.Utils.GMail;


public class RunAsyncTask extends AsyncTask<String, Void, String> {

    private Exception exception;
    private GMail gmail;
    private Context context;

    public RunAsyncTask(GMail curGmail, Context ctx)
    {
        this.gmail = curGmail;
        this.context = ctx;
    }

    protected String doInBackground(String... urls) {
        try {
            this.gmail.sendEmail(getDeviceName());
        } catch (Exception e) {
            this.exception = e;

        }
        return Event.OK;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (model.startsWith(manufacturer)) {
            return getCompleteId(capitalize(model), android_id);
        } else {
            return getCompleteId(capitalize(manufacturer) + " " + model, android_id);
        }
    }

    private String getCompleteId(String capitalize, String android_id) {
        return "[" + capitalize +"][" + android_id + "]";
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    protected void onPostExecute() {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}