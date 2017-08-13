package com.noble_shit.noodle.nobleshitmusic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Noodle on 8/13/17.
 */

public class AboutDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder AboutDialog = new AlertDialog.Builder(getActivity());

        AboutDialog.setTitle("About");

        AboutDialog.setMessage("No Bull Music is a simple music playing application that sits right" +
                " on top of your phone's file system.\n\nMost music apps will create a simple database of media files" +
                " that categorizes your music using the metadata attached to your music files.\n" +
                "That might work great for some people, but for those of us who get our music from all over " +
                "and don't always have reliable metadata attached to our music files, this makes a hellish mess, and " +
                " means we can't find our music easily and reliably.\n\nNoBull doesn't use meta data, it assumes" +
                " that you're smart enough to organize your music without help from and app. It merely reads your filesystem and " +
                " tries to play what you tell it to play. \n\nBecause you are smart enough, you don't need your phone to tell you where " +
                " your music is?\n\n" +
                "NoBull Music: Version " + getString(R.string.app_version) + "\n" +
                "Author: " + getString(R.string.author));

        return AboutDialog.create();

    }
}
