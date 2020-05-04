package com.tumblr.dateapp.iloveyou;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // Check if message contains a data payload.온그라운드상태일때
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title").toString();
            String text = remoteMessage.getData().get("text").toString();
            sendNotification(title,text);
        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendNotification(String title, String text) {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this,
                0 /* Request code */,
                i,
                PendingIntent.FLAG_ONE_SHOT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pi);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(0, builder.build());
    }
}
