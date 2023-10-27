package cf.arjun.dev.music_verse;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplication extends Application {

    public static String CHANNEL_ID = "channel1";
    public static String PLAY = "Play";
    public static String PREV = "Prev";
    public static String NEXT = "Next";
    public static String EXIT = "Exit";

    @Override
    public void onCreate() {
        super.onCreate();

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)) {

            // TODO: Do all the necessary task

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Now Playing Song", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("This is a very important channel for showing songs!!");

            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

        }

    }
}
