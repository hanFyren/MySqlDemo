package com.example.baard.mysqldemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Created by Arnar S Reiten and Baard Askildt 05.09.2017
 *
 * StressApp er en implementering av Empaticas fysiologiske sensor E4
 *
 * Denne appen brukes til å loggføre og overvåke disse fysiologiske dataene
 * og fremstiller disse som en indikasjon på stress
 *
 * Gjennom kommunikasjon med vår database kan du med StressApp:
 * Opprette ny bruker - Registrer.Java
 * Innlogging - MainActivity.java
 * Koble til din E4 og se data, samtidig som disse sendes til database - bluetooth.java
 * Overvåke en sanntidslogging - overvake.java
 *
 *
 *
 */


//#####         Kommentarer markert ##### beskriver programmets funksjonalitet.
//#####         Kommentarer markert ***** beskriver løsninger som er valgt og hvorfor


public class MainActivity extends AppCompatActivity {


    EditText Brukernavn_et, Passord_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("*******","MainActivity starter");

//##### Knytter XML elementer til Java variabler
        Brukernavn_et = (EditText)findViewById(R.id.editTextBrukernavn);
        Brukernavn_et.setHint("Brukernavn");
        Passord_et = (EditText)findViewById(R.id.editTextPassord);
        Passord_et.setHint("Passord");
    }

    public void OnLogin(View view){
//##### Funksjonen kalles fra knapp, kodet i XML
        // Ved trykk, hentes informasjon fra editText
        // Kaller backgroundworker som kommuniserer med MySQL DB gjennom php script
        // type = login avgjør funksjonaliteten til Backgroundworker
        String Brukernavn = Brukernavn_et.getText().toString();
        String Passord = Passord_et.getText().toString();
        String type = "login";

        BackgroundWorker backgroundworker = new BackgroundWorker(this);
        backgroundworker.execute(type, Brukernavn, Passord);
    }

    public void OpenReg(View view){
//##### Funksjonen kalles fra knapp, kodet i XML
        //Starter ny aktivitet, Registrer
       startActivity(new Intent(this, Registrer.class));
    }
}