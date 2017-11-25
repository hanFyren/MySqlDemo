package com.example.baard.mysqldemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by baard on 14.09.2017.
 *
 * BackGroundworker benyttes for å opprette forbindelse til DB
 * gjennom PHP script. PHP-scriptene nåes vie URL forbindelse
 *
 * Backgroundworker tar x parametere, hvor det første definerer funksjonen,
 * altså hvilket script som skal nyttes og hvilke andre parametere som kan nåes
 *
 */


public class BackgroundWorker extends AsyncTask <String, Void, String> {
//##### Deklarerer globale variabler
    private Context context;
    private AlertDialog alertDialog;
    BackgroundWorker(Context ctx) {context = ctx;    }


    @Override
    protected String doInBackground(String... params) {
        Log.i("**********"," BavkgroundWorker starter");
//##### Oppretter stringer med URL'er som skal nyttes
//***** params[0] er type som er sendt fra alt som kaller Bakcgroundworker. avgjør funksjonaliteten ved hjelp av if settningene under
        String type = params[0];
        String login_url = "http://stressapp.no/login.php";
        String reg_url = "http://stressapp.no/registrering.php";
        String logge_url = "http://stressapp.no/logge.php";
        String forste_sesjon_url = "http://stressapp.no/forste_sesjon.php";
        String siste_url = "http://stressapp.no/siste_sesjon.php";


        if (type.equals("login")) {
//#####     Try siden vi benytter URL forbindelse som kan feile
            try {
//#####     henter resterende variabler fra params[]
                String brukernavn = params[1];
                String passord = params[2];
//#####     definerer URL forbindelse som skal både sende og motta informasjon
                URL url= new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
//#####     poster data til URL forbindelsen
                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                String postData =   URLEncoder.encode("brukernavn","UTF-8")+"="+URLEncoder.encode(brukernavn,"UTF-8")+"&"+
                                    URLEncoder.encode("passord","UTF-8")+"="+URLEncoder.encode(passord,"UTF-8");

                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputstream.close();
//#####     Mottar data fra URL forbindelsen
                InputStream inputStream= httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result="";
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    result +=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
//#####     returnerer mottatt data fra URL
                return result; //bruker_ID eller 'Kunne ikke logge inn'
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (type.equals("reg")){
            try {
//#####     henter resterende variabler fra params[]
                String fornavn = params[1];
                String etternavn = params[2];
                String brukernavn = params[3];
                String passord = params[4];
//#####     definerer URL forbindelse som skal både sende og motta informasjon
                URL url= new URL(reg_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
//#####     poster data til URL forbindelsen
                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                String postData =   URLEncoder.encode("Fornavn","UTF-8")+"="+URLEncoder.encode(fornavn,"UTF-8")+"&"+
                                    URLEncoder.encode("Etternavn","UTF-8")+"="+URLEncoder.encode(etternavn,"UTF-8")+"&"+
                                    URLEncoder.encode("Brukernavn","UTF-8")+"="+URLEncoder.encode(brukernavn,"UTF-8")+"&"+
                                    URLEncoder.encode("Passord","UTF-8")+"="+URLEncoder.encode(passord,"UTF-8");

                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputstream.close();
//#####     Mottar data fra URL forbindelsen
                InputStream inputStream= httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result="";
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    result +=line;
                }
                inputStream.close();
                httpURLConnection.disconnect();
//#####     returnerer mottatt data fra URL
                return result; // 'Brukernavnet er allerede i bruk' eller 'Ny bruker opprettett!'

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        else if (type.equals("forste"))
        {

            try{
//#####     henter resterende variabler fra params[]
                String ID = params[1];
                String bruker_ID = params[2];
//#####     definerer URL forbindelse som skal både sende og motta informasjon
                URL url= new URL(forste_sesjon_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
//#####     poster data til URL forbindelsen
                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                String postData = URLEncoder.encode("ID","UTF-8")+"="+URLEncoder.encode(ID,"UTF-8")+"&"+
                                  URLEncoder.encode("Bruker_ID","UTF-8")+"="+URLEncoder.encode(bruker_ID,"UTF-8");

                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputstream.close();
//#####     Mottar data fra URL forbindelsen
                InputStream inputStream= httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result="";
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    result +=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
//#####     returnerer mottatt data fra URL
                return result; //'forste sesjon ok'

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(type.equals("logge")){
            try {
//#####     henter resterende variabler fra params[]
                String EDR=params[1];
                String HR= params[2];
                String BVP= params[3];
                String aks_x= params[4];
                String aks_y= params[5];
                String aks_z= params[6];
                String ID= params[7];
                String bruker_ID = params[8];
//#####     definerer URL forbindelse som skal både sende og motta informasjon
                URL url= new URL(logge_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
//#####     poster data til URL forbindelsen
                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                String postData =   URLEncoder.encode("EDR","UTF-8")+"="+URLEncoder.encode(EDR,"UTF-8")+"&"+
                                    URLEncoder.encode("HR","UTF-8")+"="+URLEncoder.encode(HR,"UTF-8")+"&"+
                                    URLEncoder.encode("BVP","UTF-8")+"="+URLEncoder.encode(BVP,"UTF-8")+"&"+
                                    URLEncoder.encode("aks_x","UTF-8")+"="+URLEncoder.encode(aks_x,"UTF-8")+"&"+
                                    URLEncoder.encode("aks_y","UTF-8")+"="+URLEncoder.encode(aks_y,"UTF-8")+"&"+
                                    URLEncoder.encode("aks_z","UTF-8")+"="+URLEncoder.encode(aks_z,"UTF-8")+"&"+
                                    URLEncoder.encode("ID","UTF-8")+"="+URLEncoder.encode(ID,"UTF-8")+"&"+
                                    URLEncoder.encode("Bruker_ID","UTF-8")+"="+URLEncoder.encode(bruker_ID,"UTF-8");


                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputstream.close();
//#####     Mottar data fra URL forbindelsen
                InputStream inputStream= httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result="";
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    result +=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
//#####     returnerer mottatt data fra URL
                return result; //Logg ok

        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        }

        else if(type.equals("siste")){

            try {
//#####     henter resterende variabler fra params[]
                String bruker_ID=params[2];


//#####     definerer URL forbindelse som skal både sende og motta informasjon
                URL url= new URL(siste_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
//#####     poster data til URL forbindelsen
                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                String postData = URLEncoder.encode("bruker_ID","UTF-8")+"="+URLEncoder.encode(bruker_ID,"UTF-8");

                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputstream.close();
//#####     Mottar data fra URL forbindelsen
                InputStream inputStream= httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                String result="";
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    result +=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
//#####     returnerer mottatt data fra URL
                return result; //siste ok

            }catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
//##### Backgroundworker returnerer ingenting
        return type;
    }



    @Override
    protected void onPreExecute() {
//##### Oppretter en dialogboks før Backgroundworker kjører
        alertDialog= new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Status");

    }

    //#######   HER MÅ DET RYDDES OPP!!!    #####
    @Override
    protected void onPostExecute(String aVoid) {
        Log.i("*******",aVoid);

//##### om aVoid inneholder en int, kjøres denne if'en
        if (aVoid.matches("\\d+")){
//##### Starter koble til, sender bruker ID
            Intent intent = new Intent(this.context , KobleTil.class);
            intent.putExtra("Bruker_ID",aVoid);
            context.startActivity(intent);
        }

//##### Ved ugyldig innlogging
        else if(aVoid.equals("Kunne ikke logge inn")){
//##### Viser dialogbopksen med en melding til brukeren
            alertDialog.setMessage(aVoid+". Venligst prøv igjen");
            Toast.makeText(context, aVoid, Toast.LENGTH_SHORT).show();
        }

//##### Ved opprettelse av bruker vellykket eller feilet
        else if(aVoid.equals("Brukernavnet er allerede i bruk") || aVoid.equals("Ny bruker opprettett!")){
//##### Viser dialogboks med melding til brukeren
            alertDialog.setMessage(aVoid);
            alertDialog.show();
//##### Ved vellykket opprettelse av bruker, går til innlogging
            if(aVoid.equals("Ny bruker opprettett!"))
            {
                context.startActivity(new Intent(this.context, MainActivity.class));
            }
        }

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
