package cf.arjun.dev.music_verse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    public static MusicService musicService = null;
    @SuppressLint("StaticFieldLeak")
    private static SeekBar seekbar;
    private RecyclerView recyclerView;
    @SuppressLint("StaticFieldLeak")
    public static ImageButton prev, play, next;
    @SuppressLint("StaticFieldLeak")
    public static TextView musicTitle;
    @SuppressLint("StaticFieldLeak")
    private static ImageView musicImage;
    private static Thread updateSeekbar;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("currentIndex")) {
                index = savedInstanceState.getInt("currentIndex");
            } else {
                index = 0;
            }
        }

        makeFullScreen();
        setupUIViews();
        if (checkRuntimePermission()) {
            makeServiceCall();
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("currentIndex", musicService.currentIndex);

    }

    private void makeServiceCall () {
        // TODO: Setup and Start the Service.

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        startService(intent);
    }

    private void makeFullScreen () {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void setupUIViews () {

        seekbar = findViewById(R.id.musicSeekbar);
        recyclerView = findViewById(R.id.rvMusic);
        prev = findViewById(R.id.skipPrevButton);
        next = findViewById(R.id.skipNextButton);
        play = findViewById(R.id.playButton);
        musicImage = findViewById(R.id.musicImage);
        musicTitle = findViewById(R.id.musicTitle);

    }

    private void setupRecyclerView () {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MusicAdapter adapter = new MusicAdapter();
        recyclerView.setAdapter(adapter);
        musicService.musicList = getSongs();
        adapter.setSongs(musicService.musicList);

    }

    @SuppressLint("Range")
    private List<MusicModel> getSongs () {
        List<MusicModel> songs = new ArrayList<>();
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
                null, MediaStore.Audio.Media.DATE_ADDED + " DESC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    songs.add(new MusicModel(id, title, artist, data));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return songs;
    }

    private void initButtons () {

        prev.setOnClickListener( v -> playPrevious());

        play.setOnClickListener( v -> {
            initPlayButton(false);
            if (musicService.mediaPlayer != null && musicService.mediaPlayer.isPlaying()) {
                musicService.mediaPlayer.pause();
            } else {
                initPlayButton(true);
                initPlayer(musicService.currentIndex, true);
            }

        });

        next.setOnClickListener( v -> playNext());

    }

    public static void initPlayButton (boolean isTrue) {
        if (isTrue) {
            play.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
        } else {
            play.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
        }
    }

    private static void setupCurrentMusic(String name) {
        musicImage.setImageResource(R.drawable.boombox);
        musicTitle.setText(name);
        musicTitle.setSelected(true);
    }

    private static void createPlayer() {
        if (musicService != null) {

            if (musicService.musicList.size() == 0) return;
            String name = musicService.musicList.get(musicService.currentIndex).getTitle();
            if (updateSeekbar != null) updateSeekbar.interrupt();
            setupCurrentMusic(name);
            seekbar.setMax(musicService.mediaPlayer.getDuration());
            seekbar.setProgress(0);
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    musicService.mediaPlayer.pause();
                    initPlayButton(false);
                    if (updateSeekbar != null) updateSeekbar.interrupt();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (musicService.mediaPlayer != null) {
                        int progress = seekbar.getProgress();
                        musicService.mediaPlayer.start();
                        musicService.mediaPlayer.seekTo(progress);
                        seekbar.setProgress(progress);
                        initPlayButton(true);
                        initSeekbarUpdater(true);
                    }

                }
            });


            // removed seekbar code from here. {}

            initSeekbarUpdater(false);

        }
    }

    private static void initSeekbarUpdater(boolean reStart) {

        if (reStart) updateSeekbar = null;

        updateSeekbar = new Thread() {
            @Override
            public void run() {
                int currentPosition = 0;
                try {
                    while (currentPosition < musicService.mediaPlayer.getDuration()) {
                        currentPosition = musicService.mediaPlayer.getCurrentPosition();
                        seekbar.setProgress(currentPosition);
                        musicService.setCurrentProgress(currentPosition);
                        //noinspection BusyWait
                        Thread.sleep(1000);
                    }
                } catch (IllegalStateException | InterruptedException ignore) {}
            }
        };
        updateSeekbar.start();

    }

    private void initPlayer (int current, boolean play) {
        if (musicService != null) {
            musicService.initMediaPlayer(current, play);
            createPlayer();
            initPlayButton(play);
        }

    }

    public static void playPrevious() {
        initPlayButton(true);
        if (musicService != null) {
            musicService.playPrev();
            createPlayer();
        }
    }

    public static void playNext() {
        initPlayButton(true);
        if (musicService != null) {
            musicService.playNext();
            createPlayer();
        }
    }

    public static void fixMemoryLeaks () {
        seekbar = null;
        prev = null;
        next = null;
        play = null;
        musicImage = null;
        musicTitle = null;
    }

    class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> {

        List<MusicModel> songs = new ArrayList<>();

        @SuppressLint("NotifyDataSetChanged")
        public void setSongs (List<MusicModel> songs) {
            this.songs = songs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MusicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_adpater_single_item, parent, false);
            return new MusicHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MusicHolder holder, int position) {

            holder.title.setText(songs.get(position).getTitle());
            holder.image.setImageResource(R.drawable.spotify2);
            holder.itemView.setOnClickListener( v -> {
                if (updateSeekbar != null) updateSeekbar.interrupt();
                if (musicService != null) {
                    musicService.setCurrentProgress(0);
                }
                initPlayButton(true);
                initPlayer(position, true);
            });

        }

        @Override
        public int getItemCount() {
            return songs.size();
        }

        class MusicHolder extends RecyclerView.ViewHolder {

            ImageView image;
            TextView title;

            public MusicHolder(@NonNull View item) {
                super(item);
                image = item.findViewById(R.id.musicImageSingleItem);
                title = item.findViewById(R.id.tvMusicTitleSingleItem);
            }
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.currentService();
        setupRecyclerView();
        initButtons();
        if (index != -1) {
            if (musicService != null) {
                initPlayer(index, false);
            }
            index = -1;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    private void requestRuntimePermission () {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 7);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 7);
        }
    }

    private boolean checkRuntimePermission () {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermission();
            return false;
        }

        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 7) {
            if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeServiceCall();
            }
        } else {
            requestRuntimePermission();
        }

    }
}