package com.noble_shit.noodle.nobleshitmusic;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by Noodle on 8/11/17.
 */

public class FileAdapter extends ArrayAdapter<File> {



    public FileAdapter(Context context, List<File> values) {
        super(context, R.layout.directory_row_layout, values);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        File file = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        View view = inflater.inflate(R.layout.directory_row_layout, parent, false);

        TextView textView = (TextView) view.findViewById(R.id.RowTextView);

        textView.setText(file.getName());

        return view;
    }


}
