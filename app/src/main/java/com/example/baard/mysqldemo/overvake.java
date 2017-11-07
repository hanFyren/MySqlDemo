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
 * Overvake gir den brukeren veileder mulighet til å overåke andre aktive brukere.
 *
 * Overvake benytter timerTask for å periodisk hente data fra database.
 *
 * Data hentes fra databse ved hjelp av doInBackground som kjøres parallelt med selve Overvake,
 * uten at dette er synlig for brukeren
 *
 */

public class overvake extends AppCompatActivity  {

//#####     Deklarerer globale variabler

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
    public int posisjon;

    private Timer timer;
    private TimerTask timerTask;
    final Handler handler = new Handler();

    int nummer;

    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overvake);

//#####     Initialiserer variabler
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
        EDR = HR = BVP = aks_x = aks_y = aks_z ="-";
        posisjon=0;

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

//*****     lytter på stopp (vises til bruker som start overvåkning/fortsett overvåkning/Pause overvåkning) og pauser eller fortsetter overvåkning
        stopp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fortsett) {
                    stopTimerTask();
                    fortsett = false;
                    stopp.setText("Fortsett Overvåkning");
                }
                else {
                    Log.i("*******","sjekker posisjon: "+posisjon+" bruker ID: "+brukere.get(posisjon));
                    ID= brukere.get(posisjon);
                    startTimer();
                    fortsett = true;
                    stopp.setText("Pause overvåkning");
                }
            }
        });

//*****     Lytter på knapp som tar deg tilbake til tilkoblingsmeny
        tilbake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), KobleTil.class);
                intent.putExtra("Bruker_ID",bruker_ID);
                context.startActivity(intent);
            }
        });
    }

//#####     initierer Timer for TimerTask
    public void startTimer(){
        timer = new Timer();
        startTimerTask();
        timer.schedule(timerTask, 1000, 2000);
    }
//#####     Når timertask stoppes, stopper også timer
    public void stopTimerTask(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }
//#####     TimerTask kjører finneTask() hvert sekund
    public void startTimerTask(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//*****     Ved start timerTask, oppdateres verdiene i tekstboksene. Timertassken kjøres hvert sekund, men siden dette skjer i starten,
//          Vil overvåkningen ligge noe bak sanntid.

                        String hjelp;
                        hjelp="EDR: "+EDR;
                        tv_EDR.setText(hjelp);
                        hjelp = "HR: "+HR;
                        tv_HR.setText(hjelp);
                        hjelp="BVP: "+BVP;
                        tv_BVP.setText(hjelp);
                        hjelp="Aks X: "+aks_x;
                        tv_aks_x.setText(hjelp);
                        hjelp="Aks Y: "+aks_y;
                        tv_aks_y.setText(hjelp);
                        hjelp="Aks Z: "+aks_z;
                        tv_aks_z.setText(hjelp);

//#####     Setter verdi stress-slider
                        double stressDbl;
                        try {
                            stressDbl = Double.parseDouble(EDR) * 100;}
                            catch(NumberFormatException ex){stressDbl=300;}
                        int stressInt;
                        stressInt = (int) stressDbl;
                        stress.setProgress(stressInt);

//#####     Kaller hente() for nye verdier
                        hente();

                    }
                });
            }
        };
    }

    public void hente(){
//#####     hente() oppretter nytt finne objekt som sørger for kommunikasjon til MySQL (gjennom php)
        finneTask henter = new finneTask(this);
        henter.execute();
    }

    private class finneTask extends AsyncTask<Void, Void, Void>{

        public finneTask(Context ctx){
            super();
            context = ctx;
        }

//#####     Metoden doInBackground() sørger for all kommunikasjon til MySQL og kjøres i bakgrunnen, tilsvarende Backgroundworker.java
        protected Void doInBackground(Void... params){

//#####     URL'ene for MySQL kommunikasjon
            String finn_url_aktive = "http://stressapp.no/aktiv.php";
            String finn_url_data = "http://stressapp.no/overvak.php";

//*****     Denne kjøres for å finne aktive brukere, resulteter i to lister, en med brukernavn som fyller spinner
//          og en med bruker ID som som brukes i spørring for å hente ned data
            if (finne) {
                finne=false;
                try {

//#####     definerer URL forbindelse som skal motta informasjon
                    URL url = new URL(finn_url_aktive);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(false);
                    httpURLConnection.setDoInput(true);

//#####     Mottar data fra URL forbindelsen
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                    String line;
                    int idTeller = 0;
                    int navnTeller = 0;
                    boolean navn = false;

                    Log.i("******", "OVERVAAKE, INPUTSTREAM FOR WHILE");

                    while ((line = bufferedReader.readLine()) != null) {

//#####     Fyller navnListe og brukere med data fra database
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

//*****     Denne henter ned data fra brukeren som er valgt å overvåke
            else{
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

                        String postData = URLEncoder.encode("Bruker_ID", "UTF-8") + "=" + URLEncoder.encode(ID, "UTF-8") + "&";

                        bufferedWriter.write(postData);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        outputstream.close();

//#####     Mottar data fra URL forbindelsen
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                        String line;
                        String type="EDR";

//*****     Henter her ned data fra databasen. If-settninger for å knytte data til rett vraiabel
                        while ((line = bufferedReader.readLine()) != null) {
                            Log.i("******", "STARTEN AV WHILE line: "+line);

                            if(type.equals("EDR")){
                                Log.i("******","IF EDR: "+line);
                                EDR=line;
                                type="HR";
                                Log.i("******","IF EDR ferdig : "+line);
                            }
                            if(type.equals("HR")){
                                Log.i("******","IF HR: "+line);
                                HR=line;
                                type="BVP";
                                Log.i("******","IF HR ferdig: "+line);
                            }
                            if(type.equals("BVP")){
                                BVP=line;
                                type="aks_x";
                                Log.i("******","IF BVP ferdig: "+line);
                            }
                            if (type.equals("aks_x")){
                                aks_x=line;
                                type="aks_y";
                                Log.i("******","IF aks_x ferdig: "+aks_x);
                            }
                            if(type.equals("aks_y")){
                                aks_y=line;
                                type="aks_z";
                                Log.i("******","IF aks_y ferdig: "+aks_y);
                            }
                            if (type.equals("aks_z")){
                                Log.i("********","IF aks_z"+line);
                                aks_z=line;
                                Log.i("*******","IF aks_z, aks-z: "+aks_z);
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

            return null;
        }

        @Override
        protected void onPostExecute(Void Result){

//*****     Dette skjer når finneTask() er ferdig

//#####     Om spinner ikke er opprettet, gjøres dette her
            if(opprettSpinner){
                stopTimerTask();
                opprettSpinner=false;

//#####     Setter opp Spinner (Rullegardinmeny). innholder elementer fra navnListe, deklarert som global variabel
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_item, navnListe);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                brukerValg.setAdapter(adapter);

                brukerValg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

//#####     Når bruker velges, knyttes id fra listen brukere til variabelen ID
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        stopp.setEnabled(true);
                        posisjon=position;
                        ID= brukere.get(posisjon);
                        tv_navn.setText(navnListe.get(position));
                        finne=false;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {}
                });
            }
        }
    }

//#####     Ved pause, kjører ikke lenger timerTask()
    @Override
    protected void onPause(){
        super.onPause();
        stopTimerTask();
    }

//#####     Når overvake igjen kjøres etter pause, stater timerTask igjen
    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

//#####     Om overvake skulle krasje, starter man igjen ved innloggingsmenyen
    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopTimerTask();
        context.startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
}
