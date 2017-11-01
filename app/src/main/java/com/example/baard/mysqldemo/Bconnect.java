package com.example.baard.mysqldemo;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import android.Manifest;
import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import static com.empatica.empalink.config.EmpaStatus.*;

/**
 * Created by arnar on 01.11.2017.
 */

public abstract class Bconnect extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate{
    private EmpaDeviceManager deviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
        deviceManager.authenticateWithAPIKey("234acf07689e4d2aacfe46bf5b6a816c");


    }
}
