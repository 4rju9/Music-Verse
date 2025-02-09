package cf.arjun.dev.music_verse;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private MediaSessionCompat mediaSession;
    private final MyBinder myBinder = new MyBinder();
    public MediaPlayer mediaPlayer = null;
    List<MusicModel> musicList = new ArrayList<>();
    int currentIndex = 0;
    private int current_progress = 0;
    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        mediaSession = new MediaSessionCompat(getBaseContext(), "Music Verse");
        return myBinder;
    }

    public void initMediaPlayer (int current, boolean play) {

        currentIndex = current;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(musicList.get(currentIndex).getPath());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener( v -> MainActivity.playNext());
        } catch (Exception ignore) {}

        if (play) {
            mediaPlayer.start();
            mediaPlayer.seekTo(current_progress);
            showNotification(R.drawable.ic_pause);
        }

    }

    public void playPrev () {
        current_progress = 0;
        currentIndex--;
        if (currentIndex < 0) currentIndex = musicList.size() - 1;
        initMediaPlayer(currentIndex, true);
    }

    public void playNext () {
        current_progress = 0;
        currentIndex++;
        currentIndex %= musicList.size();
        initMediaPlayer(currentIndex, true);
    }

    public void setCurrentProgress (int progress) {
        this.current_progress = progress;
    }

    public void showNotification (int play) {

        if (currentIndex >= musicList.size()) {
            return;
        }

        MusicModel model = musicList.get(currentIndex);
        int flags;

        if (Build.VERSION.SDK_INT < 31) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            flags = PendingIntent.FLAG_MUTABLE;
        }

        // TODO: Create intents for notification actions
        Intent prevIntent = new Intent(getBaseContext(), NotificationReceiver.class).setAction(MyApplication.PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, prevIntent, flags);

        Intent playIntent = new Intent(getBaseContext(), NotificationReceiver.class).setAction(MyApplication.PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, playIntent, flags);

        Intent nextIntent = new Intent(getBaseContext(), NotificationReceiver.class).setAction(MyApplication.NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, nextIntent, flags);

        Intent exitIntent = new Intent(getBaseContext(), NotificationReceiver.class).setAction(MyApplication.EXIT);
        PendingIntent exitPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, exitIntent, flags);

        // TODO: show the notification of current music playing.
        Notification notification = new NotificationCompat.Builder(getBaseContext(), MyApplication.CHANNEL_ID)
                .setContentTitle(model.getTitle())
                .setContentText(model.getArtist())
                .setSmallIcon(R.drawable.ic_music_note)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.boombox))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSilent(true)
                .addAction(R.drawable.ic_previous_button, "Previous", prevPendingIntent)
                .addAction(play, "Play", playPendingIntent)
                .addAction(R.drawable.ic_next_button, "Next", nextPendingIntent)
                .addAction(R.drawable.ic_close, "Exit", exitPendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(7, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(7, notification);
        }

    }

    class MyBinder extends Binder {
        public MusicService currentService () {
            return MusicService.this;
        }
    }

}