package com.satoshi.reflectshooting;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * Created by Satoshi on 2017/01/24.
 */
public class Graphics {
    private SurfaceHolder holder;
    private Paint paint;
    private Canvas canvas;
    private int oriX;
    private int oriY;

    public Graphics(int w, int h, SurfaceHolder holder){
        this.holder = holder;
        this.holder.setFormat(PixelFormat.RGBA_8888);
        this.holder.setFixedSize(w,h);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setOrigin(int x, int y) {
        oriX = x;
        oriY = y;
    }

    public void lock() {
        canvas = holder.lockCanvas();
        if(canvas == null) return;
        canvas.translate(oriX, oriY);
    }

    public void unlock() {
        if(canvas == null) return;
        holder.unlockCanvasAndPost(canvas);
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public  void setTextSize(int textSize) {
        paint.setTextSize(textSize);
    }

    public void drawText(String str, int x, int y) {
        if(canvas == null) return;
        canvas.drawText(str, x, y, paint);
    }

    public void drawBitmap(Bitmap bitmap, int x, int y) {
        if(canvas == null) return;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Rect src = new Rect(0, 0, w, h);
        Rect dst = new Rect(x, y, x+w, y+h);
        canvas.drawBitmap(bitmap, src, dst, null);
    }

    public void clearCanvas(){
        if(canvas == null) return;
        //canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.BLACK);
    }
}
