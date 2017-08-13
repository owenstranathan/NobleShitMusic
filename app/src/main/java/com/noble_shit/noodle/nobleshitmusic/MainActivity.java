package com.noble_shit.noodle.nobleshitmusic;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import com.noble_shit.noodle.MusicService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        AdapterView.OnItemClickListener,
        MediaController.MediaPlayerControl
{

    // UI MEMBERS

    private File            directory;
    private ListView        directoryListView;
    private File            baseDirectory = Environment.getExternalStorageDirectory();

    private DirectoryStack  directoryStack;

    private TextView        directoryTextView;
    private TextView        currentFileTextView;

    private boolean activityPaused = false;


    // MUSIC SERVICE MEMBERS

    private MusicService    musicService;
    private Intent          playIntent;
    private boolean         musicServiceBound = false;

    // MUSIC CONTROLLER MEMBERS

    private MusicController musicController;


    private Menu menu;

    // Broadcast receiver to determine when music player has been prepared
    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            // When music player has been prepared, show controller
            showController();
        }
    };



    /****************
     * MENU METHODS *
     ***************/

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.GoToHome:
                directory=baseDirectory;
                updateDirectoryListView();
                updateDirectoryTextView();
                return true;
            case R.id.SetAsHome:
                baseDirectory = directory;
                updateDirectoryTextView();
                return true;
            case R.id.About:
                // show about dialog
                DialogFragment aboutDialog = new AboutDialogFragment();
                aboutDialog.show(getFragmentManager(), "aboutDialog");
                return true;
            case R.id.Donate:
                // link to patreon
                return true;
            case R.id.Quit:
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                // No break on purpose
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /********************
     * OVERRIDE METHODS *
     *******************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null)
            recover(savedInstanceState);        // Recover a saved instance
        else
            initializeBaseDirectory();          // initialize from fresh start

        // Initialize the directory stack
        directoryStack = new DirectoryStack(directory.toString());

        // Retrieve a reference to the Directory ListView and keep it
        directoryListView = (ListView) findViewById(R.id.DirectoryListView);
        if (directoryListView != null) {
            directoryListView.setAdapter(
                    new FileAdapter(
                            this,
                            directoryList()
                    )
            );
            directoryListView.setOnItemClickListener(this);
        }

        // Retrieve a reference to the current directory text view and keep it
        directoryTextView = (TextView) findViewById(R.id.DirectoryTextView);

        //Retrieve a reference to the currentFileTextView and keep it
        currentFileTextView = (TextView) findViewById(R.id.CurrentSongTextView);

        // Debug assertion check
        if (BuildConfig.DEBUG && directoryTextView == null) { throw new AssertionError("directoryTextView is null"); }
        if (BuildConfig.DEBUG && currentFileTextView == null) { throw new AssertionError("currentFileTextView is null"); }


        // Update the directory TextView to display the current directory
        updateDirectoryTextView();

        setMusicController();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("BASE_DIRECTORY", baseDirectory.toString());
        outState.putString("CURRENT_DIRECTORY", directory.toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File file = (File) adapterView.getItemAtPosition(i);

        if (file.isDirectory()) {
            directoryStack.push(file.getName());
            directory = file;
            updateDirectoryListView();
            updateDirectoryTextView();
        } else {
            // Send everything in this directory to the MusicService
            playSong(i);
        }
    }

    @Override
    public void onBackPressed() {
        directoryStack.pop();
        if (directory.equals(Environment.getExternalStorageDirectory())) {
            super.onBackPressed();
        } else {
            directory = directory.getParentFile();
            updateDirectoryListView();
            updateDirectoryTextView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityPaused) {
            setMusicController();
            activityPaused = false;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(onPrepareReceiver, new IntentFilter(MusicService.MEDIA_PLAYER_PREPARED));
    }

    @Override
    protected void onStop() {
        saveSettings();
        hideController();
        super.onStop();
    }



    /*******************
     * Private methods *
     ******************/

    private void recover(Bundle savedInstanceState) {
        this.baseDirectory = new File(savedInstanceState.getString("BASE_DIRECTORY"));
        this.directory = new File(savedInstanceState.getString("CURRENT_DIRECTORY"));
    }

    private void saveSettings() {
        // Save the base directory, forget current directory
        SharedPreferences.Editor shrdPrfEdit = getPreferences(Context.MODE_PRIVATE).edit();
        shrdPrfEdit.putString("BASE_DIRECTORY", baseDirectory.toString());
        shrdPrfEdit.commit();

    }

    private void initializeBaseDirectory() {
        // Retrieve base directory setting
        SharedPreferences shrdprf = getPreferences(Context.MODE_PRIVATE);
        String baseDirectoryString = shrdprf.getString("BASE_DIRECTORY", null);
        if (baseDirectoryString == null) {
            baseDirectoryString = Environment.getExternalStorageDirectory().toString();
        }
        this.baseDirectory = new File(baseDirectoryString);

        // Set the current directory to here
        this.directory = this.baseDirectory;
    }


    private void updateDirectoryListView() {
        FileAdapter adapter = (FileAdapter) directoryListView.getAdapter();
        adapter.clear();
        adapter.addAll(directoryList());
        adapter.notifyDataSetChanged();
    }

    private void updateDirectoryTextView() {
        String currentDirectory = " ";
        if(directory.getParent() != null)
            currentDirectory += directory.getParentFile().getName() + " > ";
        currentDirectory += directory.getName();
        SpannableString spnstr = new SpannableString(currentDirectory);
        if(directory.equals(baseDirectory)) {
            ImageSpan imgspn = new ImageSpan(this, R.drawable.ic_home);
            spnstr.setSpan(imgspn, 0, 1, 0);
        }
        directoryTextView.setText(spnstr);
    }

    private void updateCurrentSongTextView() {
        String currentSong = musicService.getFilename();
        currentFileTextView.setText(currentSong);
    }


    private List<File> directoryList() {
        List list = new ArrayList();
        for(File f : directory.listFiles()){
            if(!f.isHidden())
                list.add(f);
        }
        Collections.sort(list);
        return list;
    }


    /***************************
     * SERVICE RELATED METHODS *
     **************************/

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) iBinder;
            // get the service
            musicService = binder.getService();
            // Not sure about this
            //musicService.setDirectory(directory);
            musicServiceBound = true;

            updateCurrentSongTextView();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicServiceBound = false;
        }
    };

    // Start the service automatically when the activity starts
    @Override
    protected void onStart() {
        super.onStart();

        if(playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    /***************************
     * MUSICCONTROLLER METHODS *
     **************************/

    private void setMusicController() {
        if(musicController == null)
            musicController = new MusicController(this);
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrevious();
            }
        });

        musicController.setMediaPlayer(this);
        musicController.setAnchorView(findViewById(R.id.DirectoryListView));
        musicController.setEnabled(true);
    }


    private void showController() {
        musicController.show(0);
    }

    private void hideController() {
        musicController.hide();
    }

    public void toggleController(View view) {
        if(musicService.isPrepared()) {
            if (musicController.isShowing()) {
                hideController();
            } else {
                showController();
            }
        }
    }

    /************************************
     * MEDIACONTROLLER OVERRIDE METHODS *
     ***********************************/


    @Override
    public void start() { musicService.start();}

    @Override
    public void pause() {
        musicService.pause();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicServiceBound && musicService.isPlaying()) {
            return musicService.getDuration();
        }else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicServiceBound && musicService.isPlaying()) {
            return musicService.getCurrentPosition();
        } else return 0;
    }

    @Override
    public void seekTo(int i) { musicService.seekTo(i); }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicServiceBound)
            return musicService.isPlaying();
        else return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        if (musicService != null && musicServiceBound && musicService.isPlaying())
            return true;
        else return false;
    }

    @Override
    public boolean canSeekForward() {
        if (musicService != null && musicServiceBound && musicService.isPlaying())
            return true;
        else return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void playPrevious() {
        musicService.previous();
        updateCurrentSongTextView();
    }

    private void playNext() {
        musicService.next();
        updateCurrentSongTextView();
    }

    private void playSong(int i) {
        musicService.setDirectory(directoryList());
        musicService.setPlayIndex(i);
        musicService.playFile();
        updateCurrentSongTextView();
    }


    /******************
    * Public Methods *
     *****************/

    public Menu getMenu(){
        return menu;
    }



}

