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
 */

//##### TO DO:  #####
//#####         Implemntere sessions                    #####
//#####         Returnere BrukerID fra innlogging       #####
//#####         Implementere sanntids overvåkning       #####
//#####         Fikse bugs i registrering/innlogging    #####
//#####         kvalitetssikre varselboks               #####

public class BackgroundWorker extends AsyncTask <String, Void, String> {
    Context context;
    AlertDialog alertDialog;
    BackgroundWorker(Context ctx) {
        context = ctx;
    }


    @Override
    protected String doInBackground(String... params) {
        Log.i("**********"," BACKGROUNDWORKER STARTER *************");
        String type = params[0];
        String login_url = "http://stressapp.no/login.php";
        String reg_url = "http://stressapp.no/registrering.php";
        String logge_url = "http://stressapp.no/logge.php";
        String sesjon_slutt_url = "http://stressapp.no/sesjon.php";
        if (type.equals("login")) {
            try {
                String brukernavn = params[1];
                String passord = params[2];
                URL url= new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                String postData = URLEncoder.encode("brukernavn","UTF-8")+"="+URLEncoder.encode(brukernavn,"UTF-8")+"&"+
                        URLEncoder.encode("passord","UTF-8")+"="+URLEncoder.encode(passord,"UTF-8");

                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputstream.close();

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
                return result;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (type.equals("reg")){
            try {
                String fornavn = params[1];
                String etternavn = params[2];
                String brukernavn = params[3];
                String passord = params[4];

                URL url= new URL(reg_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                String postData = URLEncoder.encode("Fornavn","UTF-8")+"="+URLEncoder.encode(fornavn,"UTF-8")+"&"+
                        URLEncoder.encode("Etternavn","UTF-8")+"="+URLEncoder.encode(etternavn,"UTF-8")+"&"+
                        URLEncoder.encode("Brukernavn","UTF-8")+"="+URLEncoder.encode(brukernavn,"UTF-8")+"&"+
                        URLEncoder.encode("Passord","UTF-8")+"="+URLEncoder.encode(passord,"UTF-8");

                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputstream.close();

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


                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        else if(type.equals("logge")){
            Log.i("**********"," BACKGROUNDWORKER LOGGE *************");
            try {
                String EDR=params[1];
                String HR= params[2];
                String BVP= params[3];
                String aks_x= params[4];
                String aks_y= params[5];
                String aks_z= params[6];
                String ID= params[7];
                String forste = params[8];
                String bruker_ID = params[9];

                URL url= new URL(logge_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputstream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputstream, "UTF-8"));

                String postData = URLEncoder.encode("EDR","UTF-8")+"="+URLEncoder.encode(EDR,"UTF-8")+"&"+
                        URLEncoder.encode("HR","UTF-8")+"="+URLEncoder.encode(HR,"UTF-8")+"&"+
                        URLEncoder.encode("BVP","UTF-8")+"="+URLEncoder.encode(BVP,"UTF-8")+"&"+
                        URLEncoder.encode("aks_x","UTF-8")+"="+URLEncoder.encode(aks_x,"UTF-8")+"&"+
                        URLEncoder.encode("aks_y","UTF-8")+"="+URLEncoder.encode(aks_y,"UTF-8")+"&"+
                        URLEncoder.encode("aks_z","UTF-8")+"="+URLEncoder.encode(aks_z,"UTF-8")+"&"+
                        URLEncoder.encode("ID","UTF-8")+"="+URLEncoder.encode(ID,"UTF-8")+"&"+
                        URLEncoder.encode("Forste","UTF-8")+"="+URLEncoder.encode(forste,"UTF-8")+"&"+
                        URLEncoder.encode("Bruker_ID","UTF-8")+"="+URLEncoder.encode(bruker_ID,"UTF-8");

                if (forste=="true")
                {
                    Log.i("*******","BKGRNDWRKER FORSTE == TRUE");
                }

                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputstream.close();

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

                //##### OPPRETTER FØRSTE DEL AV SESJON I PHP#####

                Log.i("*******","BAKGRNDWRKR KJØRT LOGGE, RESULT: "+result);

                return "Logg ok";


        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        alertDialog= new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Status");

    }

    @Override
    protected void onPostExecute(String aVoid) {

        if (aVoid.matches("\\d+")){ //##### om aVoid inneholder en int, kjøres denne if'en

            //##### Starter koble til, sender bruker ID #####  !(aVoid).equals("Kunne ikke logge inn")  !(aVoid).equals("Data Registrert")

            Log.i("*********","LOGIN GODKJENT --- bruker ID: "+ aVoid.toString());
            Intent intent = new Intent(this.context , KobleTil.class);
            intent.putExtra("Bruker_ID",aVoid.toString());
            context.startActivity(intent);

        }


        if(aVoid == "Logg ok"){
            Log.i("*******","BCKGRNDWRKR I RIKTIG IF, aVoid: "+aVoid);
        }

        else if(!(aVoid).equals("Data Registrert")){
            alertDialog.setMessage(aVoid);
            alertDialog.show();
        }

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
