package com.noble_shit.noodle;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.noble_shit.noodle.nobleshitmusic.MainActivity;
import com.noble_shit.noodle.nobleshitmusic.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Noodle on 8/11/17.
 */

public class MusicService extends Service
    implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener
{

    private MediaPlayer mediaPlayer;
    private List<File> directory;
    private int playIndex;
    private String filename = "";
    private static final int NOTIFY_ID = 1;

    private final IBinder musicServiceBinder = new MusicServiceBinder();

    /**********
     * BINDER *
     *********/

    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;


    }

    /********************
     * OVERRIDE METHODS *
     *******************/

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        playIndex = 0;

        initMediaPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer.getCurrentPosition() > 0) {
            mediaPlayer.reset();
            next();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();

        // Set currently playing notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_play)
                .setTicker(filename)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(filename);
        Notification not = builder.getNotification();

        startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    /******************
     * PUBLIC METHODS *
     *****************/

    public void setDirectory(List<File> argDirectory) {
        directory = argDirectory;
    }


    public void playFile() {
        mediaPlayer.reset();
        File song = directory.get(playIndex);

        // Set the file name used in onPrepared ^ up there someplace
        filename = song.getName();

        try {
            mediaPlayer.setDataSource(song.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();

    }

    public void setPlayIndex(int i) { playIndex = i; }


    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    public void start() {
        mediaPlayer.start();
    }

    public void previous() {
        playIndex--;
        if (playIndex < 0) {
            playIndex = directory.size() - 1;
        }
        playFile();
    }

    public void next() {
        playIndex++;
        if(playIndex >= directory.size()) {
            playIndex = 0;
        }
        playFile();
    }

    /*******************
     * PRIVATE METHODS *
     ******************/

    private void initMediaPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }
}
