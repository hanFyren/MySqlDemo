package com.example.baard.mysqldemo;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
//import java.util.logging.Handler;


//##### TO DO:  #####
//#####         finne ut hvordan funksjonen kan kjøre uten glitch med låst skjerm   #####
//#####         Legge til tilbake knapp                                             #####
//#####         Vatte inn data fra BT                                               #####
//#####         Implementere sessions                                               #####
//#####         Kommentere                                                          #####
//#####         kartlegge ressursbruk, burde dette vært en under-funskjon?          #####
//#####         Beholde BT kobling ved avslutt                                      #####

public class Logge extends AppCompatActivity {
//##### Deklarerer globale variabler

    public TextView tv_EDR, tv_HR, tv_BVP, tv_aks_x, tv_aks_y, tv_aks_z;
    public Button pause, avslutt;
    public SeekBar stress;
    public String ID,bruker_ID;
    public Boolean forste;

    private Timer timer;
    private TimerTask timerTask;
    final Handler handler = new Handler();

    public boolean fortsett;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logge);
        Log.i("********", " LOGGE initiert ************");

//##### Henter variabler fra KobleTil og definerer globale variabler
        ID = getIntent().getStringExtra("ID");
        bruker_ID = getIntent().getStringExtra("Bruker_ID");
        fortsett = false;
        forste = true;
        context=this;
//##### Knytter XML elemnter til java variabler
        tv_EDR = (TextView) findViewById(R.id.textViewEDR);
        tv_HR = (TextView) findViewById(R.id.textViewHR);
        tv_BVP = (TextView) findViewById(R.id.textViewBVP);
        tv_aks_x = (TextView) findViewById(R.id.textViewAksX);
        tv_aks_y = (TextView) findViewById(R.id.textViewAksY);
        tv_aks_z = (TextView) findViewById(R.id.textViewAksZ);
        pause = (Button) findViewById(R.id.buttonStopp);
        avslutt = (Button) findViewById(R.id.buttonAvslutt);
        stress = (SeekBar) findViewById(R.id.seekBarStress);

//***** seekBarStress skal vise informasjon, ikke hente. Er derfor ikke klikkbar.
//##### Setter rekkevidde til seekBarStress
        stress.setClickable(false);
        stress.setMax(600);

//##### Lytter på pause knapp starter og stopper timer-task
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fortsett) {
                    stopTimerTask();
                    fortsett = true;
                }
                else {
                    startTimer();
                    fortsett = false;
                }
            }
        });

//#####Lytter på avslutt starter Backgroundworker med argumenter
        avslutt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String type ="siste";
                BackgroundWorker backgroundworker1 = new BackgroundWorker(context);
                backgroundworker1.execute(type, ID, bruker_ID);

                Intent intent = new Intent(context , KobleTil.class);
                intent.putExtra("Bruker_ID",bruker_ID);
                context.startActivity(intent);
            }
        });


    }

//##### pause og resume av aktiviteten starter og stopper timeren
    @Override
    protected void onPause(){
        super.onPause();
        stopTimerTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

//##### onDestroy initierer KobleTil igjen
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Intent intent = new Intent(context , KobleTil.class);
        intent.putExtra("Bruker_ID",bruker_ID);
        context.startActivity(intent);
    }

//##### initierer Timer for TimerTask
    public void startTimer(){
        timer = new Timer();
        startTimerTask();
        timer.schedule(timerTask, 1000, 1000);
    }

    public void stopTimerTask(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }
//##### TimerTask kjører logge() hvert sekund
    public void startTimerTask(){
        Log.i("*******","TIMER TASK STARTET");
        timerTask = new TimerTask() {
            @Override
            public void run() {
              handler.post(new Runnable() {
                  @Override
                  public void run() {
                      // SETT INN HER
                      Log.i("*******","TIMERTASK KALLER LOGGE");
                      logge();
                  }
              });
            }
        };
    }


//##### sender data til backgroundworker for å lese til DB
    public void logge(){
        Random randtall = new Random();
        Double hjelp;
        int stressint;

        String type = "forste";

//***** Generering av tilfeldige tall til implementering av BT er i orden
        hjelp=  (100* (4.5*randtall.nextDouble()) )  ;
        hjelp = Double.valueOf(Math.round(hjelp));
        stressint=hjelp.intValue();
        hjelp= hjelp/100;
        String EDR = Double.toString(hjelp);

        hjelp=  (100* (50+(230-50)*randtall.nextDouble()) )  ;
        hjelp = Double.valueOf(Math.round(hjelp));
        hjelp= hjelp/100;
        String HR = Double.toString(hjelp);

        hjelp=  (100* (1+(20-1)*randtall.nextDouble()) )  ;
        hjelp = Double.valueOf(Math.round(hjelp));
        hjelp= hjelp/100;
        String BVP = Double.toString(hjelp);

        hjelp=  (100* (10*randtall.nextDouble()) )  ;
        hjelp = Double.valueOf(Math.round(hjelp));
        hjelp= hjelp/100;
        String aks_x = Double.toString(hjelp);

        hjelp=  (100* (10*randtall.nextDouble()) )  ;
        hjelp = Double.valueOf(Math.round(hjelp));
        hjelp= hjelp/100;
        String aks_y = Double.toString(hjelp);

        hjelp=  (100* (10*randtall.nextDouble()) )  ;
        hjelp = Double.valueOf(Math.round(hjelp));
        hjelp= hjelp/100;
        String aks_z = Double.toString(hjelp);

//##### Setter verdier synlig for brukeren
        stress.setProgress(stressint);
        tv_EDR.setText("EDR: "+EDR);
        tv_HR.setText("HR: "+HR);
        tv_BVP.setText("BVP: "+BVP);
        tv_aks_x.setText("Aks x: "+aks_x);
        tv_aks_y.setText("Aks y: "+aks_y);
        tv_aks_z.setText("Aks z: "+aks_z);

        Log.i("*******","KALLER BCKGRNDWRKR");

//##### Sørger for å opprette første del av sesjoner
        if (forste){
            BackgroundWorker backgroundworker1 = new BackgroundWorker(this);
            backgroundworker1.execute(type,ID, bruker_ID);
            forste = false;
        }

//##### Staretr
        type="logge";

            BackgroundWorker backgroundworker = new BackgroundWorker(this);
            backgroundworker.execute(type, EDR, HR, BVP, aks_x, aks_y, aks_z, ID, bruker_ID);




        Log.i("*******"," BACKGROUNDWORKER FERDIG *************");

    }
}
