package com.noble_shit.noodle;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener
{
    // Music Service Log TAG
    private static final String TAG = "MUSIC_SERVICE";

    private MediaPlayer mediaPlayer;
    private List<File> directory;
    private int playIndex;
    private String filename = "";
    private static final int NOTIFY_ID = 1;

    private final IBinder musicServiceBinder = new MusicServiceBinder();

    public static final String MEDIA_PLAYER_PREPARED = "com.noble_shit.noodle.MusicService.MEDIA_PLAYER_PREPARED";

    private boolean prepared = false;

    // Audio Focus fields
    private boolean audioFocusGranted = false;

    private static final String CMD_NAME = "command";
    private static final String CMD_PAUSE = "pause";
    private static final String CMD_STOP = "pause";
    private static final String CMD_PLAY = "play";

    // Jellybean
    private static String SERVICE_CMD = "com.sec.android.app.music.musicservicecommand";
    private static String PAUSE_SERVICE_CMD = "com.sec.android.app.music.musicservicecommand.pause";
    private static String PLAY_SERVICE_CMD = "com.sec.android.app.music.musicservicecommand.play";


    // Honeycomb
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            SERVICE_CMD = "com.android.music.musicservicecommand";
            PAUSE_SERVICE_CMD = "com.android.music.musicservicecommand.pause";
            PLAY_SERVICE_CMD = "com.android.music.musicservicecommand.play";
        }
    };

    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;


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
        start();

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

        prepared = true;

        // BroadCast that the mediaPlayer is prepared
        Intent onPreparedIntent = new Intent(MEDIA_PLAYER_PREPARED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.i(TAG, "AUDIOFOCUS_GAIN");
                start();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.e(TAG, "AUDIOFOCUS_LOSS");
                audioFocusGranted = false;
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                break;
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                Log.e(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                break;
            default:
                //
        }

    }

    /******************
     * PUBLIC METHODS *
     *****************/

    public boolean isPrepared() {
        return prepared;
    }

    public void setDirectory(List<File> argDirectory) {
        directory = argDirectory;
    }


    public void playFile() {
        mediaPlayer.reset();
        prepared = false;
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
        if(!isPlaying()) {
            if(!audioFocusGranted && requestAudioFocus()) {
                forceMusicStop();
                setupBroadcastReciever();
            }
        }
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

    public String getFilename() {
        return filename;
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

    /***********************
     * AUDIO FOCUS METHODS *
     **********************/

    private boolean requestAudioFocus() {
        if (!audioFocusGranted) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocusGranted = true;
            } else {
                // FAILED
                Log.e("TAG",
                        ">>>>>>>>>>>>> FAILED TO GET AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
            }
        }
        return audioFocusGranted;
    }

    private void abandonAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int result = audioManager.abandonAudioFocus(this);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioFocusGranted = false;
        } else {
            // FAILED
            Log.e("TAG",
                    ">>>>>>>>>>>>> FAILED TO ABANDON AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
        }
    }

    private void forceMusicStop() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager.isMusicActive()) {
            Intent intentToStop = new Intent(SERVICE_CMD);
            intentToStop.putExtra(CMD_NAME, CMD_STOP);
            sendBroadcast(intentToStop);
        }
    }

    private void setupBroadcastReciever() {
        intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String cmd = intent.getStringExtra(CMD_NAME);
                Log.i("TAG", "intentReceiver.onReceive " + action + " / " + cmd);

                if (PAUSE_SERVICE_CMD.equals(action)
                        || (SERVICE_CMD.equals(action) && CMD_PAUSE.equals(cmd))) {
                    Log.d(TAG, "start()");
                    start();
                }

                if (PLAY_SERVICE_CMD.equals(action)
                        || (SERVICE_CMD.equals(action) && CMD_PLAY.equals(cmd))) {
                    Log.d(TAG, "pause()");
                    pause();
                }
            }
        };

        // Do the right thing when something else tries to play
        if (!receiverRegistered) {
            IntentFilter commandFilter = new IntentFilter();
            commandFilter.addAction(SERVICE_CMD);
            commandFilter.addAction(PAUSE_SERVICE_CMD);
            commandFilter.addAction(PLAY_SERVICE_CMD);
            registerReceiver(intentReceiver, commandFilter);
            receiverRegistered = true;
        }
    }
}
