package com.noble_shit.noodle;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

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
    private File directory;
    private int playbackPosition;
    private int playIndex;

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
        playbackPosition = 0;
        playIndex = 0;

        initMediaPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    /******************
     * PUBLIC METHODS *
     *****************/

    public void setDirectory(File argDirectory) {
        directory = argDirectory;
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
