package com.example.baard.mysqldemo;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;


public class overvake extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public TextView tv_EDR, tv_HR, tv_BVP, tv_aks_x, tv_aks_y, tv_aks_z, tv_navn;
    public Spinner brukerValg;
    public Button stopp, tilbake;
    public SeekBar stress;
    public String ID, bruker_ID;
    public String brukere[];
    public String navnListe[];
    public String data[];

    int nummer;

    public Context context;

    private Timer timer;
    private TimerTask timerTask;
    final Handler handler = new Handler();


    public boolean fortsett;

    //lykke til!

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i("********", " OVERVAK initiert ************");
        ID = "";
        fortsett = false;
        brukere= new String[]{};
        navnListe = new String[]{};
        data = new String[]{};
        context = this;
        nummer=0;
        bruker_ID = getIntent().getStringExtra("Bruker_ID");
        //context=this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overvake);

        tv_EDR = (TextView) findViewById(R.id.textViewEDR);
        tv_HR = (TextView) findViewById(R.id.textViewHR);
        tv_BVP = (TextView) findViewById(R.id.textViewBVP);
        tv_aks_x = (TextView) findViewById(R.id.textViewAksX);
        tv_aks_y = (TextView) findViewById(R.id.textViewAksY);
        tv_aks_z = (TextView) findViewById(R.id.textViewAksZ);
        tv_navn = (TextView) findViewById(R.id.textViewBruker);
        brukerValg = (Spinner) findViewById(R.id.spinnerBrukere);
        stopp = (Button) findViewById(R.id.buttonStopp);
        tilbake = (Button) findViewById(R.id.buttonTilbake);
        stress = (SeekBar) findViewById(R.id.seekBarStress);

        stress.setClickable(false);
        stress.setMax(600);

        finne();

        //##### Setter opp Spinner (Rullegardinmeny). innholder elementer fra navnListe, deklarert som global variabel
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, navnListe);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        brukerValg.setAdapter(adapter);
        brukerValg.setOnItemSelectedListener(this);
        //##### til hit er ikke verifisert

        stopp.setOnClickListener(new View.OnClickListener() {
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

        tilbake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context , KobleTil.class);
                intent.putExtra("Bruker_ID",bruker_ID);
                context.startActivity(intent);
            }
        });

    }

    public void finne(){
        Log.i("*******","STARTER FINNE");
        String finn_url = "http://stressapp.no/aktiv.php";

        try {

//#####     definerer URL forbindelse som skal både sende og motta informasjon
            URL url= new URL(finn_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(false);
            httpURLConnection.setDoInput(true);
            Log.i("*******","OPPRETTET URL: "+httpURLConnection);
/*
            //#####     poster data til URL forbindelsen
            OutputStream outputstream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

            String postData =   URLEncoder.encode("void","UTF-8")+"="+URLEncoder.encode(".","UTF-8")+"&";

            bufferedWriter.write(postData);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputstream.close();*/

//#####     Mottar data fra URL forbindelsen
            InputStream inputStream= httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

            String line;
            int idTeller=0;
            int navnTeller=0;
            boolean navn = false;

            Log.i("******","OVERVAAKE, INPUTSTREAM FOR WHILE");

            while ((line = bufferedReader.readLine()) != null){

                if (navn)
                {
                    Log.i("*******","HENTER AKTIVE, LINE: "  + line);
                    navnListe[navnTeller] = line;
                    navnTeller++;
                    navn=false;
                    Log.i("*******","HENTER AKTIVE, NAVN: "  + navnListe[navnTeller]);
                }
                else {
                    brukere[idTeller] = line;
                    idTeller++;
                    navn=true;
                    Log.i("*******","HENTER AKTIVE, ID: " + line + "  " + brukere[idTeller]);
                }
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            Log.i("*******","Catch malfomred url");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("*******","Catch IO-exeption");
            e.printStackTrace();
        }

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        stopp.setEnabled(true);
        ID= brukere[position];
        startTimerTask();
    }

    public void startTimer(){
        timer = new Timer();
        //startTimerTask();
        timer.schedule(timerTask, 1000, 1000);
    }

    public void stopTimerTask(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }

    public void startTimerTask(){
        Log.i("*******","TIMER TASK STARTET");
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // SETT INN HER
                        Log.i("*******","TIMERTASK KALLER henteData()");
                        henteData();
                    }
                });
            }
        };
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void henteData()
    {
        try {

            String data_URL="http://stressapp.no/overvak";

//#####     definerer URL forbindelse som skal både sende og motta informasjon
            URL url= new URL(data_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
//#####     poster data til URL forbindelsen
            OutputStream outputstream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

            String postData =   URLEncoder.encode("Bruker_ID","UTF-8")+"="+URLEncoder.encode(ID,"UTF-8");

            bufferedWriter.write(postData);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputstream.close();
//#####     Mottar data fra URL forbindelsen
            InputStream inputStream= httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            String result="";
            String line;
            int teller=0;
            while ((line = bufferedReader.readLine()) != null){
                data[teller] +=line;
                teller++;
                Log.i("******","HENTER DATA: "+data[teller]);
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        stress.setProgress(Integer.parseInt(data[0]));
        String hjelp = "EDR: "+data[0];
        tv_EDR.setText(hjelp);
        hjelp= "HR: "+data[1];
        tv_HR.setText(hjelp);
        hjelp = "BVP: "+data[2];
        tv_BVP.setText(hjelp);
        hjelp = "Aks x: "+data[3];
        tv_aks_x.setText(hjelp);
        hjelp = "Aks y: "+data[4];
        tv_aks_y.setText(hjelp);
        hjelp = "Aks z: "+data[5];
        tv_aks_z.setText(hjelp);

    }

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





    //NYE FUNKSJONER HER

  /*  public void logge(){    //hele denne funksjonen er wastable
        Random randtall = new Random();
        Double hjelp=3.0;
        int stressint=3;

        String type = "overvak";


        String EDR = Double.toString(hjelp);
        String HR = Double.toString(hjelp);
        String BVP = Double.toString(hjelp);
        String aks_x = Double.toString(hjelp);
        String aks_y = Double.toString(hjelp);
        String aks_z = Double.toString(hjelp);

        stress.setProgress(stressint);

        tv_EDR.setText("EDR: "+EDR);
        tv_HR.setText("HR: "+HR);
        tv_BVP.setText("BVP: "+BVP);
        tv_aks_x.setText("Aks x: "+aks_x);
        tv_aks_y.setText("Aks y: "+aks_y);
        tv_aks_z.setText("Aks z: "+aks_z);

        Log.i("*******","KALLER BCKGRNDWRKR");

        //String array[] = new String[] {""};
        //String arrayPointer;
        BackgroundWorker backgroundworker = new BackgroundWorker(this);
        backgroundworker.execute(type);


        Log.i("*******"," BACKGROUNDWORKER FERDIG *************");

    }*/


}
