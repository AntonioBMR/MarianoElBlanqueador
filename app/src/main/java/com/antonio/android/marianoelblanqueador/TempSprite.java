package com.antonio.android.marianoelblanqueador;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.List;

/**
 * Created by Antonio on 02/03/2015.
 */
/************************************************************************/
/*************SPRITE CUANDO DESAPARECE DINERO O JUGADOR******************/
/************************************************************************/
public class TempSprite {
    private float x;
    private float y;
    private Bitmap bmp;
    private int life = 15;
    private List<TempSprite> temps;

    public TempSprite(List<TempSprite> temps,float w,float h,  float x,
                      float y, Bitmap bmp) {

        this.x=x;
        this.y=h-y;
        this.bmp = bmp;
        this.temps = temps;
    }

    public void onDraw(Canvas canvas) {
        update();
        canvas.drawBitmap(bmp, x, y, null);
    }
    //le da un tiempo y luego lo borra
    private void update() {
        if (--life < 1) {
            temps.remove(this);
        }
    }
}
