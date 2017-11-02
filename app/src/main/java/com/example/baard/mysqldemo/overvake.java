package com.example.baard.mysqldemo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.renderscript.Sampler;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/** Created by Baard 30.10.2017
 *
 * Her må det jobbes med å implementere timertask
 * denne kjører fra start, ønsker den initiert selv.
 * timertask er en work-around i forhold til sleep(), som setter hele aktiviteten i dvale, ikke bare doInBackground()
 * og for å kunne oppdatere textviews utenfor doInBackground()
 *
 * mot SQL fungerer overvake godt.
 *
 */

public class overvake extends AppCompatActivity  {

    public TextView tv_EDR, tv_HR, tv_BVP, tv_aks_x, tv_aks_y, tv_aks_z, tv_navn;
    public Spinner brukerValg;
    public Button stopp, tilbake;
    public SeekBar stress;
    public String ID, bruker_ID, EDR, HR, BVP, aks_x, aks_y, aks_z;
    public List<String> brukere;
    public List<String> navnListe;
    public String data[];
    public String finn_url;
    public boolean finne, opprettSpinner, fortsett;

    private Timer timer;
    private TimerTask timerTask;
    final Handler handler = new Handler();

    int nummer;

    public Context context;

    //lykke til!
    //takk

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overvake);

        Log.i("********", " OVERVAK initiert ************");
        ID = "";
        fortsett = false;
        brukere= new ArrayList<>();
        navnListe = new ArrayList<>();
        data = new String[]{""};
        context = this;
        nummer=0;
        bruker_ID = getIntent().getStringExtra("Bruker_ID");
        finn_url = "http://stressapp.no/aktiv.php";
        finne = opprettSpinner=true;
        EDR = HR = BVP = aks_x = aks_y = aks_z ="";

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


        Log.i("*******","Starter finne task");
        finneTask finner = new finneTask(context);
        //finner.execute();

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

    //##### initierer Timer for TimerTask
    public void startTimer(){
        timer = new Timer();
        startTimerTask();
        timer.schedule(timerTask, 1000, 1000);
    }
    //##### Når timertask stoppes, stopper også timer
    public void stopTimerTask(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }
    //##### TimerTask kjører finneTask() hvert sekund
    public void startTimerTask(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // SETT INN HER
                        Log.i("*******","TIMERTASK KALLER finnetask()");
                        finneTask finner = new finneTask(context);
                        tv_EDR.setText(EDR);
                        tv_HR.setText(HR);
                        tv_BVP.setText(BVP);
                        tv_aks_x.setText(aks_x);
                        tv_aks_y.setText(aks_y);
                        tv_aks_z.setText(aks_z);
                    }
                });
            }
        };
    }

    private class finneTask extends AsyncTask<Void, Void, Void>{

        public finneTask(Context ctx){
            super();
            context = ctx;

            Log.i("*******","I finnetask");

            // parametere her
        }

        protected Void doInBackground(Void... params){
            Log.i("*******","STARTER FINNE");
            String finn_url_aktive = "http://stressapp.no/aktiv.php";
            String finn_url_data = "http://stressapp.no/overvak.php";


            if (finne) {
                finne=false;
                try {

//#####     definerer URL forbindelse som skal både sende og motta informasjon
                    URL url = new URL(finn_url_aktive);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(false);
                    httpURLConnection.setDoInput(true);
                    Log.i("*******", "OPPRETTET URL: " + httpURLConnection);

//#####     Mottar data fra URL forbindelsen
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                    String line;
                    int idTeller = 0;
                    int navnTeller = 0;
                    boolean navn = false;

                    Log.i("******", "OVERVAAKE, INPUTSTREAM FOR WHILE");

                    while ((line = bufferedReader.readLine()) != null) {

                        if (navn) {
                            navnListe.add(line);
                            Log.i("*******", "HENTER AKTIVE, NAVN: " + navnListe.get(navnTeller));
                            navnTeller++;
                            navn = false;

                        } else {
                            brukere.add(line);
                            Log.i("*******", "HENTER AKTIVE, ID: " + brukere.get(idTeller));
                            idTeller++;
                            navn = true;

                        }
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();

                } catch (MalformedURLException e) {
                    Log.i("*******", "Catch malfomred url");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.i("*******", "Catch IO-exeption");
                    e.printStackTrace();
                }
            }
            else{
                boolean kjorda =true;
                while (kjorda) {
                    try {

//#####     definerer URL forbindelse som skal både sende og motta informasjon
                        URL url = new URL(finn_url_data);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        Log.i("*******", "OPPRETTET URL: " + httpURLConnection);

                        //#####     poster data til URL forbindelsen
                        OutputStream outputstream = httpURLConnection.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                        String postData = URLEncoder.encode("sensor_ID", "UTF-8") + "=" + URLEncoder.encode(ID, "UTF-8") + "&";

                        bufferedWriter.write(postData);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        outputstream.close();

//#####     Mottar data fra URL forbindelsen
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                        String line;
                        String type="EDR";

                        Log.i("******", "OVERVAAKE, INPUTSTREAM FOR WHILE");

                        while ((line = bufferedReader.readLine()) != null) {

                            if(type.equals("EDR")){
                                Log.i("******","IF EDR: "+line);
                                EDR=line;
                                type="HR";
                                Log.i("******","IF EDR ferdig : "+line);
                            }
                            else if(type.equals("HR")){
                                Log.i("******","IF HR: "+line);
                                HR=line;
                                type="BVP";
                                Log.i("******","IF HR ferdig: "+line);
                            }
                            else if(type.equals("BVP")){
                                BVP=line;
                                type="aks_x";
                            }
                            else if (type.equals("aks_x")){
                                aks_x=line;
                                type="aks_y";
                            }
                            else if(type.equals("aks_Y")){
                                aks_y=line;
                                type="aks_z";
                            }
                            else if (type.equals("aks_z")){
                                Log.i("********","IF aks_z"+line);
                                aks_z=line;
                                kjorda=false;

                            }
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();

                    } catch (MalformedURLException e) {
                        Log.i("*******", "Catch malfomred url");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.i("*******", "Catch IO-exeption");
                        e.printStackTrace();
                    }
                    try {
                        Log.i("********","SOVER I ETT SEKUND");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        fortsett=false;
                        Log.i("********","KUNNE IKKE OVE I ETT SEKUND");
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void Result){
            //Dette skjer når finneTask() er ferdig

            if(opprettSpinner){

                opprettSpinner=false;
                  //##### Setter opp Spinner (Rullegardinmeny). innholder elementer fra navnListe, deklarert som global variabel

                ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_item, navnListe);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                brukerValg.setAdapter(adapter);

                brukerValg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        stopp.setEnabled(true);
                        ID= brukere.get(position);
                        Log.i("*******","Bruker valgt. Bruker ID:"+ID+" Navn: "+navnListe.get(position));
                        tv_navn.setText(navnListe.get(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
            else{

            }
        }
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

}
