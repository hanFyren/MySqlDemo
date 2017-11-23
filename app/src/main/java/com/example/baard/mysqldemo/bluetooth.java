package com.example.baard.mysqldemo;

import android.Manifest;
import android.app.Activity;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v7.widget.SwitchCompat;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.util.Timer;
import java.util.TimerTask;




//TODO: kalle Bluetooth.java fra fra koble til, fremfor innlogging, trenger bruker_ID for å logge DONE
//TODO: Sende bruker_ID fra Kobletil.java til bluetooth.java slik: DONE  --> denne ligger nå i Connect E4 funksjonen
/*
        Intent intent = new Intent(context , bluetooth.class);
        intent.putExtra("Bruker_ID",bruker_ID);
        context.startActivity(intent);
        */
//TODO: plukke opp bruker_ID i bluetooth.java slik: DONE --> denne ligger nå i onCreate i denne filen.
/*
bruker_ID = getIntent().getStringExtra("Bruker_ID");
 */

//TODO: Implementer seekBar, se Logge.java -> public seekBar stress; DONE
//TODO: implemnter funksjonen forste() i onCreate tilsvarende logge. Denne skal kun kjøres en gang DONE
// og trenger ingen data fra E4, så passer fint der. om en unik ID fra klokken ikke er klar,
// kan vi øke delay på start timerTask eller kjøre forste() senere
//
//TODO: Implementere tilbake knapp (og pause?) DONE
//TODO: finne en unik id fra klokken til variabelen ID DONE
//TODO: Verifisere at rette variabler sendes til BackgroundWorker


public class bluetooth extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate {

    public TextView tv_EDR, tv_HR, tv_BVP, tv_aks_x, tv_aks_y, tv_aks_z;
    public Button pause, avslutt;
    public SeekBar stress;
    public String ID, bruker_ID;
    public Boolean forste;
    public ProgressBar loggeProgressBar;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private static final long STREAMING_TIME = 7200000; // Definerer streametid

    private static final String EMPATICA_API_KEY = "234acf07689e4d2aacfe46bf5b6a816c"; // Dette er vår API-nøkkel

    private EmpaDeviceManager deviceManager = null;

    private TextView accel_xLabel;
    private TextView accel_yLabel;
    private TextView accel_zLabel;
    private TextView bvpLabel;
    private TextView edaLabel;
    private TextView ibiLabel;
    private TextView temperatureLabel;
    private TextView batteryLabel;
    private TextView statusLabel;
    private TextView deviceNameLabel;
    private RelativeLayout dataCnt;

    //--------Oppretter variabler for å sende klokkedata til BackgroundWorker

    public String sendGsr;
    public String sendX;
    public String sendY;
    public String sendZ;
    public String sendBvp;
    public String sendBlvl;
    public String sendibi;
    public String sendHR;
    public String sendDN;


    //--------- Oppretter Backgroundworker




//#####     Deklarer disse for å opprette en timertask.
    private Timer timer;
    private TimerTask timerTask;
    final Handler handler = new Handler();
    public Boolean DB;

    public Button loggPause;
    public boolean fortsett = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);


        // ------------------------ Henter inn Bruker_ID
        bruker_ID = getIntent().getStringExtra("Bruker_ID");
        forste = true;
        DB=false;



        // Initialize vars that reference UI components
        statusLabel = (TextView) findViewById(R.id.status);
        dataCnt = (RelativeLayout) findViewById(R.id.dataArea);
        accel_xLabel = (TextView) findViewById(R.id.accel_x);
        accel_yLabel = (TextView) findViewById(R.id.accel_y);
        accel_zLabel = (TextView) findViewById(R.id.accel_z);
        bvpLabel = (TextView) findViewById(R.id.bvp);
        edaLabel = (TextView) findViewById(R.id.eda);
        ibiLabel = (TextView) findViewById(R.id.ibi);
        temperatureLabel = (TextView) findViewById(R.id.temperature);
        batteryLabel = (TextView) findViewById(R.id.battery);
        deviceNameLabel = (TextView) findViewById(R.id.deviceName);


        stress = (SeekBar) findViewById(R.id.seekBar);
        stress.setClickable(false);
        stress.setMax(600);

        loggPause=(Button) findViewById(R.id.logbtn);


        loggeProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        loggeProgressBar.setVisibility(View.GONE);




        sendGsr = sendX = sendY = sendZ = sendBvp = sendibi = "0";

        initEmpaticaDeviceManager();

        loggPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fortsett) {
                    stopTimerTask();
                    fortsett = false;
                    String sett="Logg";
                    loggPause.setText(sett);
                    loggeProgressBar.setVisibility(View.VISIBLE);
                }
                else {
                    startTimer();
                    fortsett = true;
                    String sett1="Pause";
                    loggPause.setText(sett1);
                    loggeProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }



//#####     TIMER TASK IMPLEMENTERING   ######


    //#####     initierer Timer for TimerTask
    public void startTimer(){
        timer = new Timer();
        startTimerTask();
        timer.schedule(timerTask, 500, 1000);
        //venter 1000ms før den starter, kjører deretter hvert 1000ms
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
        loggeProgressBar.setVisibility(View.VISIBLE);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        //HER IMPLEMTERES DET TIMERTASKEN SKAL GJØRE HVERT SEK. Bla.
                        // Å SETTE VERDI TIL SLIDER OG KALLE FUNKSJONEN SOM KALLER BACKGROUNDWORKER


//#####     Setter verdi stress-slider
                        double stressDbl;
                        try {
                            stressDbl = Double.parseDouble(sendGsr) * 100;}
                            //denne gangingen fordi slideren tar verdier fra 0 til 600 for å
                            // beholde desimaler, jeg antok 6 høyeste gsr som er realistisk å måle
                        catch(NumberFormatException ex){stressDbl=300;}
                        int stressInt;
                        stressInt = (int) stressDbl;
                        //stress.setProgress(stressInt);

//#####     Kaller hente() for nye verdier
                        if(DB)laste();

                    }
                });
            }
        };
    }

    public void laste(){

        String type = "forste";

        if(forste) {

            Log.i("******", "Kjører til forste");
            BackgroundWorker LastOpp = new BackgroundWorker(this);
            LastOpp.execute(type, ID, bruker_ID);
            forste=false;
        }

        type="logge";
        BackgroundWorker backgroundworker = new BackgroundWorker(this);
        backgroundworker.execute
                (type, sendGsr, sendibi, sendBvp, sendX, sendY, sendZ, ID, bruker_ID);
        //ID er klokkens unike ID, bruker_ID er ID til den som har på seg klokken
    }



//#####     TIMERTASK IMPLEMENTERING SLUTT      #####





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // Hvis request ble kansellert vil arrays være tomme.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Tillatelse gitt, da starter initEmpaticaDeviceManager
                    initEmpaticaDeviceManager();
                } else {
                    // Dersom tillatelsen ikke er der vil brukeren bli spurt om å skru på denne

                    Log.i("bluetooth", "runs until showrequestpermission");
                    final boolean needRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale
                                    (this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices " +
                                    "cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // prøver igjen
                                    if (needRationale) {
                                        // "never ask again"-flagget er ikke satt, spør igjen om
                                        // tillatelse
                                        initEmpaticaDeviceManager();
                                    } else {
                                        //"never ask again"-flagg er satt, derfor blir
                                        // tillatelsesforspørsel avkoblet, man må inn i app
                                        // settings for å endre denne
                                        Intent intent = new Intent
                                                (Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts
                                                ("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // hvis ingen tillatelse er gitt, er exit den eneste muligheten.
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    private void initEmpaticaDeviceManager() {

        // Android 6 (API level 23) trenger nå ACCESS_COARSE_LOCATION
        // tillatelse for å benytte BLE (Bluetooth Low Energy)
        if (ContextCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions
                    (this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);

            Toast.makeText(this, "init kjørt", Toast.LENGTH_SHORT).show();
        } else {
            //Skaper en ny EmpaDeviceManager. Bluetooth.java er både dens data og status-delegat.
            deviceManager = new EmpaDeviceManager
                    (getApplicationContext(), this, this);
            Toast.makeText(this, "Device Manager initialisert", Toast.LENGTH_SHORT).show();

            // Dersom det ikke er lagt inn noen API-key vil dette komme opp som en feilmelding i
            // appen før den vil lukkes når brukeren trykker på "Lukk"

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Advarsel")
                        .setMessage("Vennligst skriv inn din API KEY")
                        .setNegativeButton("Lukk", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // uten tillatelsen er exit den eneste mulighet
                                finish();
                            }
                        })
                        .show();
                return;
            }
            //Device Manageren blir initialisert ved hjelp av API-nøkkelen.
            // Dette fordrer at man har internett-forbindelse.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }

    @Override
    protected void onPause() {

        //Hvis pause er satt vil timeren stoppe.
        super.onPause();
        if (deviceManager != null) {
            stopTimerTask();


        }
    }

    @Override
    protected void onResume(){
        //ved gjenopptagelse av aktiviteten vil timeren starte igjen
        super.onResume();
        startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Når aktiviteten termineres vil BackgroundWorker motta "siste" som type og derme
        // avslutte loggingen i SQL. Sesjonen er da over.
        String type ="siste";
        BackgroundWorker backgroundworker = new BackgroundWorker(this);
        backgroundworker.execute(type, ID, bruker_ID);

        if (deviceManager != null) {
            deviceManager.cleanUp();
            //Device Manager klargjøres for eventuelt ny aktivitet
        }
    }

    @Override
    public void didDiscoverDevice
            (BluetoothDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Sjekker om den oppdagede enheten kan bli brukt med vår API-key,
        // If Allowed er alltid false

        if (allowed) {
            // Stopper skanning og bruker den første godkjente enheten den finner.
            deviceManager.stopScanning();
            try {
                // Kobler til enheten
                deviceManager.connectDevice(bluetoothDevice);
                updateLabel(deviceNameLabel, "To: " + deviceName);
                ID = deviceName;
                DB=true;
            } catch (ConnectionNotAllowedException e) {
                // Dette vil kun skje dersom en prøver å koble til og allowed == false
                Toast.makeText
                        (bluetooth.this, "Sorry, you can't connect to this device",
                                Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {

        // Oppfordrer brukeren til å skru på Bluetooth dersom dette ikke er gjort.
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Dette er en funksjon som brukes i fall brukeren ikke skrur på Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus status, EmpaSensorType type) {
        // Denne har vi ikke sett behov for å implementere ennå
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Oppdaterer User Interface
        updateLabel(statusLabel, status.name());

        // Device Manager er klar til bruk
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Starter skanningen etter tilgjengelige enheter innen rekkevidde.
            deviceManager.startScanning();
            // Device Manager har opprettet en forbindelse med en enhet
        } else if (status == EmpaStatus.CONNECTED) {
            // Stopper streamingen av data når STREAMING_TIME er utløpt

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataCnt.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Programmet avsluttes
                            Toast.makeText(bluetooth.this, "Programmet avsluttes",
                                    Toast.LENGTH_SHORT).show();

                            deviceManager.disconnect();
                        }
                    }, STREAMING_TIME);
                }
            });
            // Device Manager har koblet fra enheten
        } else if (status == EmpaStatus.DISCONNECTED) {
            updateLabel(deviceNameLabel, "");
        }

    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {

        // Her henter vi inn de tre akselerometer-verdiene. disse oppdateres så videre inn i UI.
        // Samtidig konverterer vi de til stringer som plukkes opp av BackgroundWorker.

        updateLabel(accel_xLabel, "" + x);
        updateLabel(accel_yLabel, "" + y);
        updateLabel(accel_zLabel, "" + z);
        sendX = String.valueOf(x);
        sendY = String.valueOf(y);
        sendZ = String.valueOf(z);

    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {

        // Denne funksjonen mottar verdien for blodvolumtrykk og oppdaterer Label i UI.
        // BVP konverteres til string som kan plukkes opp av BackgroundWorker

        updateLabel(bvpLabel, "" + bvp);
        sendBvp = String.valueOf(bvp);
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {

        // Henter batterinivå, gjør ikke noe annet med dette annet enn å oppdatere label i UI
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));

    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {

        // Dette er selve gullegget:
        // Elektrodermisk aktivitet registreres og blir passert gjennom datadelegate til
        // denne funksjonen som først oppdaterer UI med ny label, for så å konvertere gsr
        // (EDA-verdien) til string for BackgroundWorker. Videre blir det satt en ny double som
        // brukes til å oppdatere vår ProgressBar som er en visuell fremstilling av et "stressnivå".


        updateLabel(edaLabel, "EDA: " + gsr);
        sendGsr = String.valueOf(gsr);
        double stressDbl;

        try {
            stressDbl = Double.parseDouble(sendGsr) * 100;}
            //denne gangingen fordi slideren tar verdier fra 0 til 600 for å beholde desimaler,
            // jeg antok 6 høyeste gsr som er realistisk å måle
        catch(NumberFormatException ex){stressDbl=300;}

        int stressInt;
        stressInt = (int) stressDbl;
        stress.setProgress(stressInt);

    }


    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        // Tar i mot interbeat intervals, oppdaterer label i UI og konverterer til string for
        // BackgroundWorker.
        updateLabel(ibiLabel, "" + ibi);
        sendibi = String.valueOf(ibi);
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        updateLabel(temperatureLabel, "" + temp);
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);

            }
        });
    }

    public void startLogging(View view){

        //ved kalling på startLogging vil Timer starte igjen.

        startTimer();
    }

    public void pauseLogging(View view){

        //ved kalling på pauseLogging vil Timer stoppe.
        stopTimerTask();
    }

    public void avsluttLogging(View view){

        //Stopper Timer
        stopTimerTask();

        // BackgroundWorker får beskjed om å sende meldingen "siste" som avslutter sesjonen i SQL
        String type ="siste";
        BackgroundWorker backgroundworker = new BackgroundWorker(this);
        backgroundworker.execute(type, ID, bruker_ID);

        //Ved avsluttLogging vil man bli sendt tilbake KobleTil-menyen
        Intent intent = new Intent(this , KobleTil.class);
        intent.putExtra("Bruker_ID",bruker_ID);
        this.startActivity(intent);
    }


}


