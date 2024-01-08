package cf.arjun.dev.music_verse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            switch (intent.getAction()) {
                case "Play": {
                    if (MainActivity.musicService != null) if (MainActivity.musicService.mediaPlayer.isPlaying()) pauseMusic(); else playMusic();
                    break;
                }
                case "Prev": {
                    MainActivity.playPrevious();
                    break;
                }
                case "Next": {
                    MainActivity.playNext();
                    break;
                } case "Exit": {
                    MainActivity.fixMemoryLeaks();
                    MainActivity.musicService.stopForeground(true);
                    MainActivity.musicService.stopSelf();
                    MainActivity.musicService = null;
                    System.exit(0);
                    break;
                }
            }
        }
    }

    private void playMusic () {
        if (MainActivity.musicService != null) {
            MainActivity.musicService.mediaPlayer.start();
            MainActivity.musicService.showNotification(R.drawable.ic_pause);
            MainActivity.initPlayButton(true);
        }
    }

    private void pauseMusic () {
        if (MainActivity.musicService != null) {
            MainActivity.musicService.mediaPlayer.pause();
            MainActivity.musicService.showNotification(R.drawable.ic_play);
            MainActivity.initPlayButton(false);
        }
    }

}
