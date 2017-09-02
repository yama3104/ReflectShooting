package com.satoshi.reflectshooting;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.KeyEvent;

/**
 * Created by Satoshi on 2017/01/05.
 */

public class ReflectShooting extends Activity {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new MySurfaceView(this));
    }

    @Override
    public void onStart() {
        super.onStart();
        MySurfaceView mySV = new MySurfaceView(this);

        mySV.loadInterstitial();
        Log.d("debug", "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        MySurfaceView mySV = new MySurfaceView(this);
        //Log.d("stop", "onStop");
        if(mySV.mediaPlayer != null) mySV.mediaPlayer.pause();
        if(mySV.thread != null && mySV.scene == 2) mySV.surfaceDestroyed(mySV.getHolder());
    }

    @Override
    public void onRestart() {
        super.onRestart();
        MySurfaceView mySV = new MySurfaceView(this);
        //Log.d("scene",""+mySV.scene);
        if(mySV.mediaPlayer != null) mySV.mediaPlayer.start();

        /*if(mySV.thread == null) {
            mySV.surfaceCreated(mySV.getHolder());
            Log.d("debug", "onRestart");
        }*/
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//ボタンが押された時に呼ばれる
        if(keyCode == KeyEvent.KEYCODE_BACK) return true;
        return false;
    }
}
