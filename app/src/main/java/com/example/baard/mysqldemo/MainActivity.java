package com.example.baard.mysqldemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

//##### TO DO:  #####
//                      ##### = gjenstår    ***** = fullført
//
//#####         hente ned bruker-ID for å lagre session     #####
//#####         Implememtere metode for sanntidsovervåkning #####
//#####         Ikke sende passord i klartekst              #####
//#####         Kommentere                                  #####


//#####         Kommentarer markert ##### beskriver programmets funksjonalitet.
//#####         Kommentarer markert ***** beskriver løsninger som er valgt og hvorfor

public class MainActivity extends AppCompatActivity {
//##### Deklarerer globale variabler
    // kun to nødvendig, editText, brukes for å hente innlogingsinformasjon fra bruker
    // implementering av kanpper skjer i XML

    EditText Brukernavn_et, Passord_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//#####     Binner variabler til elementer fra XML
        Brukernavn_et = (EditText)findViewById(R.id.editTextBrukernavn);
        Passord_et = (EditText)findViewById(R.id.editTextPassord);

    }

    public void OnLogin(View view){
//##### Funksjonen kalles fra knapp, kodet i XML
        // Ved trykk, henter informasjon fra editText
        // Kaller backgroundworker som kommuniserer med MySQL DB gjennom php script
        // type = login avgjør funksjonaliteten til Backgroundworker
        String Brukernavn = Brukernavn_et.getText().toString();
        String Passord = Passord_et.getText().toString();
        String type = "login";

        BackgroundWorker backgroundworker = new BackgroundWorker(this);
        backgroundworker.execute(type, Brukernavn, Passord);
    }

    public void OpenReg(View view){
//##### Funksjonen kalles fra kanpp, kodet i XML
        //Starter ny aktivitet, Registrer
       startActivity(new Intent(this, Registrer.class));
    }
}