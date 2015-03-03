package com.antonio.android.marianoelblanqueador;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Antonio on 02/03/2015.
 */
public class JuegoThread extends Thread implements View.OnTouchListener {
    Context context;
    Resources res;
    Random random;
    //NUMERO DE COLUMNAS Y FILAS
    final static int UNITS_HORIZONTAL = 16;
    final static int UNITS_VERTICAL = 18;
    // Tamaños de la bomba y mariano
    final static int BOMB_RADIUS = 12;
    final static int MARIANOHEIGHT = 17;

    final static int JUEGONUEVO = 0;
    final static int JUGANDO = 1;
    final static int GAMEOVER = 2;
    final static int PAUSADO = 3;
    final static int SIGUENTENIVEL = 4;
    SurfaceHolder holder;
    Paint smalltextpaint, medtextpaint, bigtextpaint;
    Paint roundrectpaint, paint;
    RectF rect;
    Bitmap mariano, dinero, disparo, background,bmpAcierto,bmpMuerto,fondoinicio;
    private List<TempSprite> temps = new ArrayList<TempSprite>();
    boolean running = true;
    int state = JUEGONUEVO;
    int canvaswidth = 500;
    int canvasheight = 500;
    // tamaño unidad dinero
    int unidadH;
    int unidadW;
    long tiempoA;
    int puntos, highscore;
    int nivel;
    int vidas, maxvidas;
    float disparoX, disparoY, gravedadDisparo, velocidadDiscparo;
    float start, velocidadInicialMariano;
    float MarianoX, MarianoY, velocidad, gravedad, velocidadMariano, aceleracionMariano;
    int[] dineros;
/************************************************************************/
/**********************CONSTRUCTOR DEL HILO****************************/
/************************************************************************/
    public JuegoThread(SurfaceHolder hold, Context c) {
        holder = hold;
        context = c;
        random = new Random();
        res = c.getResources();
        //para pintar los recuadros y textos de inicio y final de partida
        smalltextpaint = new Paint();
        smalltextpaint.setColor(Color.BLACK);
        smalltextpaint.setTextSize((float) 16);
        medtextpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        medtextpaint.setColor(Color.BLACK);
        medtextpaint.setTextSize((float) 22);
        bigtextpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bigtextpaint.setColor(Color.BLACK);
        bigtextpaint.setTextSize((float) 38);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        roundrectpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        roundrectpaint.setARGB(188, 202, 222, 155);
        rect = new RectF(unidadW, unidadH, canvaswidth - unidadW, canvasheight - unidadH);
    }
/************************************************************************/
/**********************PAUSA Y PLAY DEL JUEGO****************************/
/************************************************************************/
    public void setRunning(boolean run) {
        running = run;
        tiempoA = System.nanoTime();
    }

    public void setPaused(boolean pause) {

        if(!pause && state == PAUSADO) {
            tiempoA = System.nanoTime();
            state = JUGANDO;
        }

        if(pause && state == JUGANDO) {
            state = PAUSADO;
        }
    }
/************************************************************************/
/************************VALORES POR DEFECTO*****************************/
/************************************************************************/
    public void getSettings() {
        velocidadDiscparo = (float)0.85;
        velocidadInicialMariano =(float) 0.56;
        aceleracionMariano = (float)0.02;
        maxvidas = 2;
    }
/************************************************************************/
/*******************AÑADE IMAGENES JUEGOA LA VISTA***********************/
/************************************************************************/
    public void surfaceSize(int width, int height) {
        synchronized(holder) {
            getSettings();
            canvasheight = height;
            canvaswidth = width;
            unidadW = width / UNITS_HORIZONTAL;
            unidadH = height / UNITS_VERTICAL;
            start = MARIANOHEIGHT * unidadH;
            gravedadDisparo = canvasheight * velocidadDiscparo;
            //para hacer velocidad proporcional a la pantalla
            velocidad = canvaswidth * velocidadMariano;
            gravedad = unidadH;
            mariano = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.mariano),
                    (int) (unidadW), (int) (unidadH), true);
            dinero = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.dinero),
                    unidadW, unidadH, true);
            disparo = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.bomb),
                    BOMB_RADIUS * 2, BOMB_RADIUS * 2, true);
            bmpAcierto = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.blood),
                    unidadW, unidadH, true);
            bmpMuerto = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.blood1),
                    unidadW, unidadH, true);
            fondoinicio = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.fondo),
                    width-2*unidadW+unidadW/2, height-2*unidadH, true);
            rect = new RectF(unidadW, unidadH, canvaswidth - unidadW, canvasheight - unidadH);
            background = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.landscape),
                    width, height, true);
        }
    }
/************************************************************************/
/******************************RESTART***********************************/
/************************************************************************/
    public void restart() {
        synchronized(holder) {
            puntos = 0;
            iniciarNivel(0);
        }
    }

/************************************************************************/
/*************************METODOS NIVELES*******************************/
/************************************************************************/
    public void iniciarNivel(int lvl) {
        synchronized(holder) {
            if(lvl == 0) {
                vidas = maxvidas;
                puntos = 0;
                velocidadMariano = velocidadInicialMariano;
            } else {
                velocidadMariano += aceleracionMariano;
            }
            nivel = lvl;
            disparoY = 0;
            MarianoY = start;
            MarianoX = 0;
            gravedadDisparo = canvasheight * velocidadDiscparo;
            velocidad = canvaswidth * velocidadMariano;
            gravedad = unidadH;
            dineros = generaNivel(lvl);
        }
    }

    public int[] generaNivel(int lvl) {
        // MATRIZ DINERO
        int[] generated = new int[UNITS_HORIZONTAL];
        int min = nivel;
        int max = nivel + 3;
        if(min < 3) {
            min = 3;
        }
        if(max < 5) {
            max = 5;
        }
        if(max >= MARIANOHEIGHT) {
            max = MARIANOHEIGHT - 1;
        }
        if(min > max) {
            min = max;
        }
        for(int i = 1; i < UNITS_HORIZONTAL - 1; i++) {
            generated[i] = (int) (random.nextFloat() * (max - min + 1) + min - 0.5);
        }
        return generated;
    }
/************************************************************************/
/*******************************ONDRAW***********************************/
/************************************************************************/
    public void draw(Canvas canvas) {
        canvas.drawBitmap(background, (float) 0, (float) 0, paint);
        // Dibuja dinero (enemigo)
        for(int i = 0; i < dineros.length; i++) {
            for(int j = 0; j < dineros[i]; j++) {
                canvas.drawBitmap(dinero, (float) i * unidadW,
                        (float) canvasheight - (j + 1) * unidadH, paint);
            }
        }
        // Dibuja a mariano
        canvas.drawBitmap(mariano, MarianoX - unidadW,
                canvasheight - MarianoY, paint);
        if(disparoY > 0) {
            // Dibuja el disparo
            canvas.drawBitmap(disparo, disparoX - BOMB_RADIUS, canvasheight - disparoY - BOMB_RADIUS, paint);
        }
        // Dibuja temporal
        for (int i = temps.size() - 1; i >= 0; i--) {
            temps.get(i).onDraw(canvas);
        }
        //Dibuja texto y mensajes
        RectF rect1 = new RectF(2, 2, 3*unidadW, 3*unidadH);
        canvas.drawRoundRect(rect1, 2, 2, roundrectpaint);
        canvas.drawText(res.getString(R.string.level) + (nivel + 1), (float) 0, unidadH, smalltextpaint);
        canvas.drawText(res.getString(R.string.score) + puntos, (float) 0, unidadH * 2, smalltextpaint);
        canvas.drawText(res.getString(R.string.lives) + vidas, (float) 0, unidadH * 3, smalltextpaint);
    }
/************************************************************************/
/*********************ACTUALIZAR IMAGENES FPS****************************/
/************************************************************************/
    public void update() {
        synchronized(holder) {
            long tick = System.nanoTime();
            // tiempo en segundos
            double tiempo = (tick - tiempoA)/1000000;
            tiempo /= 1000;
            tiempoA = tick;
            if(disparoY != 0) {
                disparoY -= gravedadDisparo * tiempo;
                //para determinar la columna divide la posicion del disparo entre la dimension d cada columna,esto devuelve el numero de
                //filas de dinero si al * unitheght es mayor o = que bombY esque ha colisionado
                if(dineros[(int) disparoX / unidadW] * unidadH >= disparoY || disparoY >= canvasheight) {
                    // Si hay colision entre disparo y dinero suma puntos
                    if(dineros[(int) disparoX / unidadW] > 0) {
                        puntos++;
                        dineros[(int) disparoX / unidadW]--;
                        temps.add(new TempSprite(temps, canvaswidth,canvasheight,((int)disparoX/unidadW)*unidadW,disparoY, bmpAcierto));
                        System.out.println("unitw"+unidadW+"bomby"+disparoY+" uh0"+unidadH);
                    }
                    disparoY = 0;
                }
            }
            MarianoX += velocidad * tiempo;
            if(MarianoX >= canvaswidth) {
                MarianoX = MarianoX % canvaswidth;
                MarianoY -= gravedad;
            }
            // colision mariano con billetes
            if(dineros[(int) MarianoX / unidadW] * unidadH >= MarianoY) {
                vidas--;
                temps.add(new TempSprite(temps, canvaswidth,canvasheight, MarianoX,MarianoY, bmpMuerto));
                dineros[(int) MarianoX / unidadW]--;
                MarianoY = start;
                MarianoX = 0;
            }
            if(vidas < 0) {
                state = GAMEOVER;
                if(recuperar()!=-1){
                    highscore=recuperar();
                }else{
                    highscore=0;
                }if(puntos > highscore) {
                    grabar(puntos+"");
                }
                return;
            }
            // si queda dinero sale y sigue la partida
            for(int i = 0; i < dineros.length; i++) {
                if(dineros[i] > 0) {
                    return;
                }
            }
            // si no sube de nivel
            iniciarNivel(nivel + 1);
            state = SIGUENTENIVEL;
        }
    }
/************************************************************************/
/**********************GUARDAR LEER +PUNTUACION**************************/
/************************************************************************/
    public void grabar(String punt) {
        System.out.println("todo "+"graba");
        int n=0;
        try{
            n=Integer.parseInt(punt);
        }catch (Exception e){

        }
        try {
            File file = new File( Environment.getExternalStorageDirectory(), "highscore.txt");
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(file));
            System.out.println("todo "+n);
            osw.write(n+"");
            osw.flush();
            osw.close();
        } catch (IOException ioe) {
        }
    }

    public int recuperar() {
        int n=0;
        String todo = "";
        File file = new File( Environment.getExternalStorageDirectory(), "highscore.txt");
        if(file.exists()){
            try {
                FileInputStream fIn = new FileInputStream(file);
                InputStreamReader archivo = new InputStreamReader(fIn);
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                if (linea != null) {
                    todo = todo + linea +"";
                    System.out.println("todo lee y existe"+ todo);
                }
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
            try{
                n=Integer.parseInt(todo);
                System.out.println("todo recupera "+n+" entra");
            }catch(Exception e){
                return -1;
            }
            return n;
        }
        return -1;
    }
/************************************************************************/
/****************************GAMEOVER************************************/
/************************************************************************/
    public void drawgameover(Canvas canvas) {
        canvas.drawRoundRect(rect, (float) unidadH, (float) unidadH, roundrectpaint);
        if(recuperar()!=-1){
            highscore=recuperar();
        }else{
            highscore=0;
        }
        canvas.drawText(res.getString(R.string.gameover), (float) 100, 100, bigtextpaint);
        canvas.drawText(res.getString(R.string.highscore) + highscore, (float) 100, 150, medtextpaint);
        canvas.drawText(res.getString(R.string.score) + puntos, (float) 100, 170, medtextpaint);
    }
/************************************************************************/
/**********************JUGARDENUEVO****************************/
/************************************************************************/
    public void drawnewgame(Canvas canvas) {
        //canvas.drawRoundRect(rect, (float) unitheight, (float) unitheight, roundrectpaint);
        canvas.drawBitmap(fondoinicio,(float) unidadH*2, (float) unidadH, roundrectpaint);
        canvas.drawText(res.getString(R.string.welcome), (float) 100, 100, bigtextpaint);
        canvas.drawText(res.getString(R.string.clicktostart), (float) 100, 200, bigtextpaint);
    }
/************************************************************************/
/**************************SIGUIENTE NIVEL*******************************/
/************************************************************************/
    public void drawlevelup(Canvas canvas) {
        canvas.drawRoundRect(rect, (float) unidadH, (float) unidadH, roundrectpaint);
        canvas.drawText(res.getString(R.string.clearedlevel)  + " " + nivel, (float) 100, 100, medtextpaint);
        canvas.drawText(res.getString(R.string.ontolevel) + " " + (nivel + 1), (float) 100, 150, medtextpaint);
    }
/************************************************************************/
/*******************************ONTOUCH**********************************/
/************************************************************************/
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            click();
        }
        return true;
    }

    public void click() {
        //CLICK INICIAR JUEGO TRAS GAMEOVER
        if(vidas < 0) {
            iniciarNivel(0);
        }
        //CLICK VOLVER AL JUEGO SI ESTA EN PAUSA
        if(state != JUGANDO) {
            state = JUGANDO;
            tiempoA = System.nanoTime();
            return;
        }
        //CLICK DISPARAR MIENTRAS SE EST JUGANDO
        if(disparoY == 0) {
            disparoY = MarianoY - unidadH;
            disparoX = MarianoX;
            if(disparoX >= canvaswidth - BOMB_RADIUS)
                disparoX = canvaswidth - BOMB_RADIUS - 1;
            if(disparoX <= BOMB_RADIUS)
                disparoX = BOMB_RADIUS + 1;
        }
    }
/************************************************************************/
/*********************************RUN************************************/
/************************************************************************/
    public void run() {
        restart();
        while(running) {
            Canvas c = null;
            try {
                synchronized(holder) {
                    c = holder.lockCanvas(null);
                    switch(state) {
                        case JUGANDO:
                            if(vidas < 0) {
                                draw(c);
                                drawgameover(c);
                                state = GAMEOVER;
                            } else {
                                update();
                                draw(c);
                            }
                            break;
                        case GAMEOVER:
                            draw(c);
                            drawgameover(c);
                            break;
                        case SIGUENTENIVEL:
                            draw(c);
                            drawlevelup(c);
                            break;
                        case JUEGONUEVO:
                            draw(c);
                            drawnewgame(c);
                            break;
                        case PAUSADO:
                            draw(c);
                            break;
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                if(c != null) {
                    holder.unlockCanvasAndPost(c);
                } else {
                }
            }
        }
    }
}
