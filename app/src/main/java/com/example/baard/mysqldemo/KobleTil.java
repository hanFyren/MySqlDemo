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

//##### TOD DO: #####
//#####         Implementere oppkobling av BT                       #####
//*****         plukke opp og videresende bruker ID for sessions    *****
//#####         Kommenter                                           #####
//#####         Rydde opp - ikke hensiktsmessig før ferdigstilling  #####
//#####         Fikse krasj på button_tilbake - problemet ligger i kobletil.java?   #####

public class KobleTil extends AppCompatActivity{

//  ##### Deklarerer Globale variabler


    private Button overvaak;

    public String bruker_ID;
    public Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_koble_til);

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

    public void ConnectE4(View view){

        // --------------- Sender Bruker_ID til bluetooth.java
        Intent intent = new Intent(context , bluetooth.class);
        intent.putExtra("Bruker_ID",bruker_ID);
        context.startActivity(intent);
    }
}


