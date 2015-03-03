package com.antonio.android.marianoelblanqueador;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Antonio on 02/03/2015.
 */
public class JuegoView extends SurfaceView implements SurfaceHolder.Callback {

    JuegoThread thread;
    Context context;

    boolean threadStarted = false;
/************************************************************************/
/**********************DEVUELVE DEL HILO****************************/
/************************************************************************/

    public JuegoThread getThread() {
        return thread;
    }
/************************************************************************/
/**********************CONSTRUCTOR DE LA VISTA****************************/
/************************************************************************/
    public JuegoView(Context c) {
        super(c);
        context = c;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        thread = new JuegoThread(holder, context);
        setFocusable(true);
    }
/************************************************************************/
/**********************VISTA CREADA****************************/
/************************************************************************/
    public void surfaceCreated(SurfaceHolder holder) {
        holder.addCallback(this);
        setOnTouchListener(thread);
        thread.setRunning(true);
        setFocusable(true);
        if(!threadStarted) {
            thread.start();
            threadStarted = true;
        } else {
            thread.setRunning(false);
            thread = new JuegoThread(holder, context);
            thread.setRunning(true);
            thread.setPaused(false);
            thread.start();
        }
    }
/************************************************************************/
/**********************CAMBIOS EN LA VISTA****************************/
/************************************************************************/
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        thread.surfaceSize(w, h);
    }

/************************************************************************/
/**********************VISTA DESTRUIDA****************************/
/************************************************************************/
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}

