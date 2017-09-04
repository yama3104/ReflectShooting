package com.satoshi.reflectshooting;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.games.Games;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Satoshi on 2017/01/05.
 */
//位置を表す変数は基本質点として扱うこと
    //TODO: 電話使うときは音を消す
public class MySurfaceView  extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {
    Context cnt;
    ReflectShooting rs = new ReflectShooting();

    private final int
            W = 480,
            H = 800;

    static final int
            S_TITLE = 0,
            S_RANKING = 1,
            S_PLAY = 2,
            S_GAMEOVER = 3,
            S_PAUSE = 4;


    static SurfaceHolder holder;
    static Thread thread;
    private Graphics g;
    static MediaPlayer mediaPlayer;
    static SoundPool soundPool;
    private AudioAttributes audioAttributes;
    static int scene;
    private int time = 0;
    private int score = 0;
    private int life = 0;
    private long titleStartTime, rankStartTime, playStartTime, goverStartTime;
    private int touchX, touchY;
    private int touchXunder = W * 9/10;
    private int btnSelectX, btnSelectY;
    private int RND;
    Random rnd = new Random();
    private int ranking[] = new int[11];
    private String rankKey[] = new String[10];
    private int damagedID, pressbtnID, reflectID, beateneID;

    private Bitmap title;
    private Bitmap start_btn0, start_btn1, rank_btn0, rank_btn1;
    private Bitmap bullet;
    private int px = W / 5;  //弾の位置のx成分
    private int py = H / 5;  //弾の位置のy成分
    private int vx = 10;     //弾の速度のx成分
    private int vy = 10;     //弾の速度のy成分

    private Bitmap board;
    int boardX = W * 9 / 10;
    int boardY = H * 3/4;     //自機の位置(y座標)
    int boardXbefore = W*9/10;

    private Bitmap enemy1, enemy2, enemy3;
    private Bitmap itemSlow, itemFast;
    private int healID, slowID, fastID;
    List<Point> enemies1 = new ArrayList<Point>();
    List<Point> enemies2 = new ArrayList<Point>();
    List<Point> enemies3 = new ArrayList<Point>();
    List<Point> itemsPink = new ArrayList<Point>();
    List<Point> itemsSlow = new ArrayList<Point>();
    List<Point> itemsFast = new ArrayList<Point>();

    private Bitmap back;
    private Bitmap secretBack;
    private Bitmap pause, pauseBack, toTitle, resume;
    private Bitmap heart;
    private int isSecret = 4;

    private boolean EmulatorTest = false;  /////製品版ではfalseに直す/////
    private String UnitID;
    // publisher ID
    private String AdMobInterID = "ca-app-pub-6378485568392983/5924032955";
    private String AdMobBannID = "ca-app-pub-6378485568392983/4837579356";
    // Test ID
    private String EmulatorTestID = "ca-app-pub-3940256099942544/6300978111";

    static InterstitialAd interstitialAd;
    // インタースティシャル用のView
    static View viewAd = null;


    /////コンストラクタ/////
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)//これ使うと古いバージョンでエラーが起きる
    public MySurfaceView(Activity activity) {
        super(activity);
        this.cnt = activity;

        Resources r = getResources();
        title = BitmapFactory.decodeResource(r, R.drawable.title);
        back = BitmapFactory.decodeResource(r, R.drawable.back);
        bullet = BitmapFactory.decodeResource(r, R.drawable.bullet);
        board = BitmapFactory.decodeResource(r, R.drawable.board);
        enemy1 = BitmapFactory.decodeResource(r, R.drawable.enemy1);
        enemy2 = BitmapFactory.decodeResource(r, R.drawable.enemy2);
        enemy3 = BitmapFactory.decodeResource(r, R.drawable.enemy3);
        start_btn0 = BitmapFactory.decodeResource(r, R.drawable.start_btn0);
        start_btn1 = BitmapFactory.decodeResource(r, R.drawable.start_btn1);
        rank_btn0 = BitmapFactory.decodeResource(r, R.drawable.rank_btn0);
        rank_btn1 = BitmapFactory.decodeResource(r, R.drawable.rank_btn1);
        heart = BitmapFactory.decodeResource(r, R.drawable.heart);
        secretBack = BitmapFactory.decodeResource(r, R.drawable.secretback);
        pause = BitmapFactory.decodeResource(r, R.drawable.pause);
        pauseBack = BitmapFactory.decodeResource(r, R.drawable.pauseback);
        toTitle = BitmapFactory.decodeResource(r, R.drawable.totitle);
        resume = BitmapFactory.decodeResource(r, R.drawable.resume);
        itemSlow = BitmapFactory.decodeResource(r, R.drawable.itemslow);
        itemFast = BitmapFactory.decodeResource(r, R.drawable.itemfast);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool(30, AudioManager.STREAM_MUSIC, 0);
        } else {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(30)
                    .build();
        }

        damagedID = soundPool.load(activity, R.raw.damaged, 1);
        pressbtnID = soundPool.load(activity, R.raw.pressbtn, 1);
        reflectID = soundPool.load(activity, R.raw.reflect, 1);
        beateneID = soundPool.load(activity, R.raw.beatene, 1);
        healID = soundPool.load(activity, R.raw.heal, 1);
        slowID = soundPool.load(activity, R.raw.slow, 1);
        fastID = soundPool.load(activity, R.raw.fast, 1);

        if(scene == S_TITLE && mediaPlayer == null) playTitleBGM();
        //mediaPlayer = MediaPlayer.create(activity, R.raw.bgm);
        //mediaPlayer.setLooping(true);

        holder = getHolder();
        holder.addCallback(this);

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        int dh = W * p.y / p.x;

        g = new Graphics(W, dh, holder);
        g.setOrigin(0, (dh - H) / 2);

        //for(int i=0; i<11; i++) ranking[i] = 0;//こいつのせいでランキングがたまにリセットされる
        for(int i=0; i<10; i++) rankKey[i] = "rank" + i;

        if(EmulatorTest){
            UnitID = EmulatorTestID;
        }
        else{
            UnitID = AdMobInterID;
        }
        interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId(UnitID);

        // Set the AdListener.
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("debug",  "onAdLoaded()");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //String message = String.format("onAdFailedToLoad (%s)", getErrorReason(errorCode));
                Log.d("debug","onAdFailedToLoad()");

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d("debug","onAdOpened()");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.d("debug","");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the application after tapping on an ad.
                Log.d("debug","onAdClosed()");
            }
        });
    }

    public void surfaceCreated(SurfaceHolder holder) {
        //if(thread == null) {
            Log.d("debug", "surfaceCreated");
            thread = new Thread(this);
            thread.start();
        //}
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if(thread != null) {
            thread = null;
            Log.d("debug", "surfaceDestroyed");
        }
    }

    //////////////////////////////////////////////
    ////ゲームの本体。ここにいろいろ書けーい。////
    //////////////////////////////////////////////
    public void run() {

        SharedPreferences pref = cnt.getSharedPreferences("game_score", cnt.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        while (thread != null) {
            if (scene == S_TITLE) {
                time = 0;
                score = 0;
                enemies1.clear();
                enemies2.clear();
                enemies3.clear();
                itemsPink.clear();
                itemsSlow.clear();
                itemsFast.clear();
                life = 3;
                boardX = W * 9 / 10;
                px = (W/5) + (int)(Math.random()*10000%100);
                py = H / 5;
                vx = 10;
                vy = 10;
                for (int i = 0; i < 10; i++) ranking[i] = pref.getInt(rankKey[i], 0);

                g.lock();
                g.clearCanvas();
                g.setColor(Color.WHITE);
                g.drawBitmap(title, 0, 0);
                g.drawBitmap(start_btn0, 150, H * 3 / 5);
                g.drawBitmap(rank_btn0, 150, H * 3 / 5 + 50);
                if (150 < touchX && touchX < 330 && H * 3 / 5 < touchY && touchY < H * 3 / 5 + 30) {
                    g.drawBitmap(start_btn1, 150, H * 3 / 5);
                }
                if (150 < touchX && touchX < 330 && H * 3 / 5 + 50 < touchY && touchY < H * 3 / 5 + 80) {
                    g.drawBitmap(rank_btn1, 150, H * 3 / 5 + 50);
                }
                g.unlock();
            } else if (scene == S_RANKING) {
                g.lock();
                g.clearCanvas();
                g.setColor(Color.BLACK);
                g.drawBitmap(back, 0, 0);
                g.setTextSize(30);
                for (int i = 0; i < 9; i++)
                    g.drawText(i + 1 + "位  ：" + ranking[i], W / 5, 100 + H / 12 * i);
                g.drawText(10 + "位：" + ranking[9], W / 5, 100 + H / 12 * 9);
                g.unlock();

            } else if (scene == S_PLAY) {
                //Log.d("debug", ""+vx);
                //Log.d("debug", ""+py);
                //以下は使う画像のサイズ取得
                int boardWidth = board.getWidth();
                int bulletWidth = bullet.getWidth();
                int bulletHeight = bullet.getHeight();
                int enemyWidth1 = enemy1.getWidth();
                int enemyHeight1 = enemy1.getHeight();
                /*int enemyWidth2 = enemy2.getWidth();
                int enemyHeight2 = enemy2.getHeight();
                int enemyWidth3 = enemy3.getWidth();
                int enemyHeight3 = enemy3.getHeight();*/

                g.lock();
                g.clearCanvas();

                time++;
                g.setColor(Color.WHITE);
                if (isSecret > 0) {
                    g.setColor(Color.WHITE);
                    g.drawBitmap(back, 0, 0);
                } else {
                    g.setColor(Color.BLACK);
                    g.drawBitmap(secretBack, 0, 0);
                }
                g.drawBitmap(bullet, px, py);
                g.drawBitmap(board, boardX - boardWidth / 2, boardY);
                g.drawBitmap(pause, W - (50 + 20), 20);

                //敵の描画
                int enePos = rnd.nextInt(W - 120) + 60;
                if (time % 20 == 0) {
                    if (Math.abs(vy) >= 35) {
                        enemies3.add(new Point(enePos, -50));
                    } else if (Math.abs(vy) >= 25) {
                        enemies2.add(new Point(enePos, -50));
                    } else {
                        enemies1.add(new Point(enePos, -50));
                    }
                }
                RND = (int)(Math.random()*10000 % 100);
                Log.d("debug", ""+RND);
                for (int i = 0; i < enemies1.size(); i++) {
                    Point pos1 = enemies1.get(i);
                    g.drawBitmap(enemy1, pos1.x-30, pos1.y-30);

                    pos1.y += 7;
                    /////敵に弾を当てた時の処理/////
                    if (Math.sqrt(Math.pow(pos1.x - (px + bulletWidth/2), 2)
                            + Math.pow(pos1.y - (py + bulletHeight/2), 2)) < 50) {
                        if(1<=RND && RND<=7) {
                            itemsPink.add(new Point(pos1.x, pos1.y));
                        } else if(11<=RND && RND<=20) {
                            itemsSlow.add(new Point(pos1.x, pos1.y));
                        } else if(21<=RND && RND<=50) {
                            itemsFast.add(new Point(pos1.x, pos1.y));
                        }
                        enemies1.remove(i);
                        playSE(beateneID);
                        score += Math.abs(vy);
                    }

                    if (pos1.y > H + 100) {
                        enemies1.remove(i);
                    }
                }
                RND = (int)(Math.random()*10000 % 100);
                for (int i = 0; i < enemies2.size(); i++) {
                    Point pos2 = enemies2.get(i);
                    g.drawBitmap(enemy2, pos2.x-40, pos2.y-40);

                    pos2.y += 7;
                    /////敵に弾を当てた時の処理/////
                    if (Math.sqrt(Math.pow(pos2.x - (px + bulletWidth/2), 2)
                            + Math.pow(pos2.y - (py + bulletHeight/2), 2)) < 50) {
                        if(1<=RND && RND<=7) {
                            itemsPink.add(new Point(pos2.x, pos2.y));
                        } else if(11<=RND && RND<=25) {
                            itemsSlow.add(new Point(pos2.x, pos2.y));
                        } else if(41<=RND && RND<=47) {
                            itemsFast.add(new Point(pos2.x, pos2.y));
                        }
                        enemies2.remove(i);
                        playSE(beateneID);
                        score += Math.abs(vy);
                    }
                    if (pos2.y > H + 100) {
                        enemies2.remove(i);
                    }
                }
                RND = (int)(Math.random()*10000 % 100);
                for (int i = 0; i < enemies3.size(); i++) {
                    Point pos3 = enemies3.get(i);
                    g.drawBitmap(enemy3, pos3.x-40, pos3.y-40);

                    pos3.y += 7;
                    /////敵に弾を当てた時の処理/////
                    if (Math.sqrt(Math.pow(pos3.x - (px + bulletWidth/2), 2)
                            + Math.pow(pos3.y - (py + bulletHeight/2), 2)) < 50) {
                        if(1<=RND && RND<=15) {
                            itemsPink.add(new Point(pos3.x, pos3.y));
                        } else if(21<=RND && RND<=35) {
                            itemsSlow.add(new Point(pos3.x, pos3.y));
                        } else if(41<=RND && RND<=52) {
                            itemsFast.add(new Point(pos3.x, pos3.y));
                        }
                        enemies3.remove(i);
                        playSE(beateneID);
                        score += Math.abs(vy);
                    }
                    if (pos3.y > H + 100) {
                        enemies3.remove(i);
                    }
                }

                /////アイテムの描画と処理/////
                for (int i = 0; i < itemsPink.size(); i++) {
                    Point posP = itemsPink.get(i);
                    g.drawBitmap(heart, posP.x - 17, posP.y - 15);
                    posP.y += 5;

                    if (boardX - boardWidth/2<posP.x+17 && boardX + boardWidth/2>posP.x-17 && boardY<posP.y+15 && posP.y<boardY+15+15){
                        if(life < 3) {
                            life++;
                            playSE(healID);
                            itemsPink.remove(i);
                        }
                    }

                    if (posP.y > H + 100) {
                        itemsPink.remove(i);
                    }
                }
                for (int i = 0; i < itemsSlow.size(); i++) {
                    Point posS = itemsSlow.get(i);
                    g.drawBitmap(itemSlow, posS.x - 16, posS.y - 16);
                    posS.y += 5;

                    if (boardX - boardWidth/2<posS.x+16 && boardX + boardWidth/2>posS.x-16 && boardY<posS.y+16 && posS.y<boardY+15+16){
                        if(vx > 0) {
                            if (Math.abs(vx) >= 12) vx -= 2;
                            else vx = 10;
                        } else {
                            if (Math.abs(vx) >= 12) vx += 2;
                            else vx = 10;
                        }
                        if(vy > 0) {
                            if (Math.abs(vy) >= 12) vy -= 2;
                            else vy = 10;
                        } else {
                            if (Math.abs(vy) >= 12) vy += 2;
                            else vy = 10;
                        }
                        playSE(slowID);
                        itemsSlow.remove(i);
                    }

                    if (posS.y > H + 100) {
                        itemsSlow.remove(i);
                    }
                }
                for (int i = 0; i < itemsFast.size(); i++) {
                    Point posF = itemsFast.get(i);
                    g.drawBitmap(itemFast, posF.x - 16, posF.y - 16);
                    posF.y += 5;

                    if (boardX - boardWidth/2<posF.x+16 && boardX + boardWidth/2>posF.x-16 && boardY<posF.y+16 && posF.y<boardY+15+16){
                        if(vx > 0) {
                            if (Math.abs(vx) >= 12) vx += 3;
                            else vx = 10;
                        } else {
                            if (Math.abs(vx) >= 12) vx -= 3;
                            else vx = 10;
                        }
                        if(vy > 0) {
                            if (Math.abs(vy) >= 12) vy += 3;
                            else vy = 10;
                        } else {
                            if (Math.abs(vy) >= 12) vy -= 3;
                            else vy = 10;
                        }
                        playSE(fastID);
                        itemsFast.remove(i);
                    }

                    if (posF.y > H + 100) {
                        itemsFast.remove(i);
                    }
                }

                g.setTextSize(30);
                g.drawText("Score: " + score, 10, 100);
                for (int i = 0; i < life; i++) g.drawBitmap(heart, 40 * i + 10, 110);
                g.unlock();

                //壁に衝突時
                if (px <= 0 || W - bulletWidth <= px) {
                    vx = -vx;
                    playSE(reflectID);
                }
                if (py <= 0) {
                    vy = -vy;
                    playSE(reflectID);
                }
                if (H - bulletHeight <= py) {
                    if (life > 0) {
                        /*if(Math.abs(vy) >= 15){
                            vx *= 0.7;
                            vy *= 0.7;
                        } else {
                            if(vx > 0) vx = 10; else vx = -10;
                            if(vy > 0) vy = 10; else vy = -10;
                        }*/
                        vy = -vy;
                        playSE(damagedID);
                    }
                    life--;
                }
                //板に衝突時
                if (boardY < py + bulletHeight && py + bulletHeight < boardY+vy+5 && vy > 0) {
                    if (boardX - 20 - boardWidth / 2 < px + bulletWidth && boardX + 20 + boardWidth/2 > px) {
                        if(vx>0) vx += 1;
                        else vx -= 1;
                        vy += 1;
                        vy = -vy;
                        playSE(reflectID);
                    }
                }

                if(0<=px+vx || px+bulletWidth+vx<=W){
                    px += vx;
                } else {
                    if(px < 240) px = 0; else px = W-bulletWidth;
                }
                if(0<=py+vy || py+bulletHeight+vy<=H){
                    py += vy;
                } else {
                    if(py < 400) py = 0; else py = H-bulletHeight;
                }

                boardXbefore = boardX;
                if (Math.abs(touchXunder - boardX) < 30) {
                    boardX = touchXunder;
                } else if (touchXunder > boardX) {
                    boardX += 50;
                } else {
                    boardX -= 50;
                }

                //ゲームオーバーになったときの処理
                if (life <= 0) {
                    scene = S_GAMEOVER;
                    goverStartTime = System.currentTimeMillis();
                    stopBGM();
                    rs.sendScore(score);
                    ranking[10] = score;
                    rankSort(ranking);
                    for (int i = 0; i < 10; i++) {
                        editor.putInt(rankKey[i], ranking[i]);
                        editor.apply();
                    }
                }
            } else if (scene == S_PAUSE) {
                g.lock();
                g.clearCanvas();
                if (isSecret > 0) {
                    g.setColor(Color.WHITE);
                    g.drawBitmap(back, 0, 0);
                } else {
                    g.setColor(Color.BLACK);
                    g.drawBitmap(secretBack, 0, 0);
                }
                g.drawBitmap(bullet, px, py);
                g.drawBitmap(pauseBack, 40, 200);
                g.drawBitmap(pauseBack, 40, 400);
                g.drawBitmap(toTitle, 120, 250);
                g.drawBitmap(resume, 120, 450);
                g.unlock();
            } else if (scene == S_GAMEOVER) {
                g.lock();
                g.clearCanvas();
                g.setColor(Color.WHITE);
                g.drawBitmap(back, 0, 0);
                g.setTextSize(40);
                g.drawText("Score: " + score, W/5, H/2);
                g.unlock();
            }
            try {
                Thread.sleep(30);
            } catch (Exception e) {
            }
        }
    }

    ////////////////////////////////////
    /////画面がタッチされた時の処理/////
    ////////////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = (int)(event.getX() * W) / getWidth();
        touchY = (int)(event.getY() * H) / getHeight();
        if(touchY > H*3/4-30) touchXunder = touchX;
        btnSelectX = -1;
        btnSelectY = -1;
        if(event.getAction() == MotionEvent.ACTION_UP){
            btnSelectX = (int)(event.getX() * W) / getWidth();
            btnSelectY = (int)(event.getY() * H) / getHeight();
        }
        if(scene == S_TITLE) {
            //ゲームスタートボタン
            if (System.currentTimeMillis() - titleStartTime > 500) {
                if (150 < btnSelectX && btnSelectX < 330 && H * 3 / 5 < btnSelectY && btnSelectY < H * 3 / 5 + 30) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN) loadInterstitial();
                    scene = S_PLAY;
                    playSE(pressbtnID);
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
                    playBGM();
                }
            }
            //ランキングボタン
            if (System.currentTimeMillis() - titleStartTime > 500) {
                if (150 < btnSelectX && btnSelectX < 330 && H * 3 / 5 + 50 < btnSelectY && btnSelectY < H * 3 / 5 + 80) {
                    Log.d("debug", "" + interstitialAd.isLoaded());
                    Log.d("debug", "" + Math.random()*100);
                    if (interstitialAd.isLoaded() && (int)(Math.random()*100 % 2) == 0) interstitialAd.show();
                    scene = S_RANKING;
                    playSE(pressbtnID);
                    rankStartTime = System.currentTimeMillis();
                }
            }

            if(isSecret == 4 && 0<=touchX && touchX<=50 && 0<=touchY && touchY<=50) isSecret--;
            if(isSecret == 3 && W-50<=touchX && touchX<=W && 0<=touchY && touchY<=50) isSecret--;
            if(isSecret == 2 && W-50<=touchX && touchX<=W && H-50<=touchY && touchY<=H) isSecret--;
            if(isSecret == 1 && 0<=touchX && touchX<=50 && H-50<=touchY && touchY<=H) isSecret--;

        } else if(scene == S_RANKING) {
            if (System.currentTimeMillis() - rankStartTime > 300) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) loadInterstitial();
                scene = S_TITLE;
                titleStartTime = System.currentTimeMillis();
            }

        } else if(scene == S_PLAY) {
            if (390 <= btnSelectX && btnSelectX <= 460 && 20 <= btnSelectY && btnSelectY <= 70){
                playSE(pressbtnID);
                scene = S_PAUSE;
            }
            if (thread == null) surfaceCreated(holder);

        } else if (scene == S_PAUSE) {
            //タイトル画面へボタン
            if (120<=btnSelectX && btnSelectX<=360 && 250<= btnSelectY && btnSelectY<=350){
                playSE(pressbtnID);
                scene = S_TITLE;
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
                playTitleBGM();
                isSecret = 4;
                titleStartTime = System.currentTimeMillis();
            }
            //続けるボタン
            if (120<=btnSelectX && btnSelectX<=360 && 450<= btnSelectY && btnSelectY<=550){
                playSE(pressbtnID);
                scene = S_PLAY;
            }

        } else if (scene == S_GAMEOVER) {
            if (System.currentTimeMillis() - goverStartTime > 1000) {
                if(touchX<150 || 330<touchX || touchY<480 || 560<touchY) {
                    Log.d("debug", "" + interstitialAd.isLoaded());
                    if (interstitialAd.isLoaded() && (int)(Math.random()*100) % 3 == 0) {
                        interstitialAd.show();
                    }
                    scene = S_TITLE;
                    playTitleBGM();
                    isSecret = 4;
                    titleStartTime = System.currentTimeMillis();
                }
            }
            return true;
        }
        return true;
    }

    private static Bitmap readBitmap(Context context, String name) {
        int res = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return BitmapFactory.decodeResource(context.getResources(), res);
    }

    private void playSE(int ID){
        soundPool.play(ID, 1, 1, 0, 0, 1);
        //return;
    }

    public void playBGM() {
        //stopBGM();
        try{
            mediaPlayer = MediaPlayer.create(cnt, R.raw.bgm);
            mediaPlayer.setLooping(true);
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        } catch (Exception e) {
        }
    }

    public void playTitleBGM() {
        try{
            mediaPlayer = MediaPlayer.create(cnt, R.raw.title);
            mediaPlayer.setLooping(true);
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        } catch (Exception e) {
        }
    }

    public void stopBGM() {
        if(mediaPlayer == null) return;
        try{
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        } catch (Exception e) {
        }
    }

    private void rankSort(int[] ranking) {
        for(int i=1; i<ranking.length; i++){
            int tmp = ranking[i];
            int j = i-1;
            while(j >= 0 && ranking[j] < tmp) {
                ranking[j+1] = ranking[j];
                j--;
            }
            ranking[j+1] = tmp;
        }
    }

    public void loadInterstitial() {
        AdRequest adRequest;
        if(EmulatorTest){
            // Test
            adRequest = new AdRequest.Builder()
                    .addTestDevice("A255EA4137B35BF31CBE357B12ABC50A")
                    .addTestDevice(EmulatorTestID)
                    .build();
        }
        else{
            // 広告リクエストを作成する (本番)
            adRequest = new AdRequest.Builder().build();
        }
        // Load the interstitial ad.
        interstitialAd.loadAd(adRequest);
        Log.d("debug", "AdLoaded");
    }
}
