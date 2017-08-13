package com.noble_shit.noodle.nobleshitmusic;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Created by Noodle on 8/11/17.
 */

public class MusicController extends MediaController {

    public MusicController(Context context){
        super(context);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode() ) {
            case KeyEvent.KEYCODE_BACK:
                this.hide();
                return  true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void show() {
        super.show();
    }


}
