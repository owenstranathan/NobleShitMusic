package com.noble_shit.noodle.nobleshitmusic;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Created by Noodle on 8/11/17.
 */

public class MusicController extends MediaController {

    MainActivity main;

    public MusicController(Context context){
        super(context, true);
        main = (MainActivity) context;
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        switch (event.getKeyCode() ) {
//            case KeyEvent.KEYCODE_BACK:
//                return  true;
//            default:
//                return super.dispatchKeyEvent(event);
//        }
//    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void show() {
        super.show();
    }


}
