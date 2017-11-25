package com.example.baard.mysqldemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Random;

/** Created by Baard 30.10.2017
 *
 * KobleTil er kun en meny hvor en kan velge om man skal logge aktivitet,
 * om man er logget inn som veileder kan man velge å overvåke aktivitet
 *
 */

public class KobleTil extends AppCompatActivity{

//##### Deklarerer Globale variabler


    private Button overvaak;

    public String bruker_ID;
    public Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_koble_til);
        Log.i("*******","KobleTil starter");

        context=this;

//##### Henter Bruker_ID fra Backgroundworker
        bruker_ID = getIntent().getStringExtra("Bruker_ID");

//##### knytter XML elementer til Java variabler
        overvaak = (Button) findViewById(R.id.buttonOvervak);
        overvaak.setEnabled(false);

//***** Kun godkjente bruker_ID skal kunne overvåke andre brukere, overvaak er derfor deaktivert som standard
        if (bruker_ID.equals("139") && bruker_ID!=null) {
            overvaak.setEnabled(true);
        }

        overvaak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//##### Funksjon om Overvåk trykket
                Intent intent = new Intent(context, overvake.class);
                intent.putExtra("Bruker_ID", bruker_ID);
                startActivity(intent);
            }
        });
    }

//##### funksjon om Logge trykket
    public void ConnectE4(View view){

        // --------------- Sender Bruker_ID til bluetooth.java
        Intent intent = new Intent(context , bluetooth.class);
        intent.putExtra("Bruker_ID",bruker_ID);
        context.startActivity(intent);
    }
}


