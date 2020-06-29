package com.digitstory.testapplication.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;

import com.digitstory.testapplication.Utils.Config;


public class autoStart extends BroadcastReceiver {
    public autoStart() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        
        
// check is it the correct intent action, incoming sms
        if (intent.getAction() != null) {
            String intentAction = intent.getAction();
            Log.d(Config.TAG, intent.getAction() + ".");
            if (intentAction.equals("android.provider.Telephony.SMS_RECEIVED")) {
                // if the intent action is incoming sms, then extract the info from the intent, show in log
                for (SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    if (message != null) {
                        String add = message.getDisplayOriginatingAddress();
                        String msg = message.getDisplayMessageBody();
                        String date1 = DateFormat.format("yyMMdd,HH:mm:ss", message.getTimestampMillis()).toString();
                        String smsContent = "received sms from " + add + ". Content: " + msg + "." + "Date:" + date1 +".";
                        Log.d("incomingSms",smsContent);
                    }
                }
            } 
        }
    }
}