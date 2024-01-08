package cf.arjun.dev.music_verse;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

public class MyApplication extends Application {

    public static String CHANNEL_ID = "7";
    public static String PLAY = "Play";
    public static String PREV = "Prev";
    public static String NEXT = "Next";
    public static String EXIT = "Exit";

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // TODO: Do all the necessary task

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Now Playing Song", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("This is a very important channel for showing songs!!");
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

        }

    }
}
