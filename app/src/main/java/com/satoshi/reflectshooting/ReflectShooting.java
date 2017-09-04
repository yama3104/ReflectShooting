package com.satoshi.reflectshooting;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;

/**
 * Created by Satoshi on 2017/01/05.
 */

public class ReflectShooting extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new MySurfaceView(this));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        MySurfaceView mySV = new MySurfaceView(this);

        mGoogleApiClient.connect();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            mIntentInProgress = false;
            if (resultCode != RESULT_OK) {
                // エラーの場合、resultCodeにGamesActivityResultCodes内の値が入っている
                return;
            }
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, String.format("ログインしました"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();

        // サインインしていない場合、サインイン処理を実行する
        if (errorCode == ConnectionResult.SIGN_IN_REQUIRED
                && !mIntentInProgress && connectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                connectionResult.startResolutionForResult(this, 100);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public void sendScore(int score){
        if (mGoogleApiClient.isConnected()) {
            Games.Leaderboards.submitScore(mGoogleApiClient,"CgkIwr-QjMAKEAIQAA", score);
        }
    }

    public void showLeaderBoards(){
        if (mGoogleApiClient.isConnected()){
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,"CgkIwr-QjMAKEAIQAA"),10);
        }
    }
}
