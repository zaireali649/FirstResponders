package com.serc.firstresponders;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MainMenuConsent extends AppCompatActivity {

    private Button start;
    private String test;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String format;
    private EditText et, role, ipadd, port;
    private Button btnConsent;
    private BandClient client = null;
    private String value;
    private int c;
    final WeakReference<Activity> reference = new WeakReference<Activity>(this);

    private PendingIntent pendingIntent;

    private static final int RESULT_SETTINGS = 1;

    private RadioButton rbp, rbr;

    private String activity, rolestr, ipaddstr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_consent);

        activity = "None";
        rolestr = "None";
        ipaddstr = "None";

        test = null;

        c = 0;

        if (isMyServiceRunning(CollectBandService.class))
        {

            Intent intent = new Intent(MainMenuConsent.this, ActivityMenu.class);
            startActivity(intent);
            finish();

        }

        //showUserSettings();

        et = (EditText) findViewById(R.id.editText);
        ipadd = (EditText) findViewById(R.id.editText2);
        role = (EditText) findViewById(R.id.editText3);
        port = (EditText) findViewById(R.id.editText5);

        rbp = (RadioButton) findViewById(R.id.radioButton4);
        rbr = (RadioButton) findViewById(R.id.radioButton5);



        //////////////////////////////////////////////////////////////////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},23
                );
            }
        }

        /*if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");

            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");

        }*/

        //////////////////////////////////////////////////////////////////////////////////////////////////////



        sp =
                getSharedPreferences("MyPrefs",
                        Context.MODE_PRIVATE);





        start = (Button) findViewById(R.id.button2);
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new HeartRateConsentTask().execute(reference);





            }
        });

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            //Find paired Bands
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {

                return false;
            }

            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        return ConnectionState.CONNECTED == client.connect().await();
    }

    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {

            try {

                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                                if(consentGiven== true){



                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("uID", String.valueOf(et.getText()));
                                    editor.putString("studyrole", String.valueOf(role.getText()));
                                    editor.putString("ipaddr", String.valueOf(ipadd.getText()));
                                    editor.putString("port", String.valueOf(port.getText()));
                                    editor.putString("activity", "");
                                    editor.commit();








                                    value = sp.getString("uID", "");


                                    Intent intent = new Intent(MainMenuConsent.this, CollectBandService.class);

                                    intent.putExtra("uID", value);


                                    if(rbp.isChecked())
                                    {
                                        //Toast.makeText(MainMenuConsent.this, "Participant", Toast.LENGTH_LONG).show();
                                        editor = sp.edit();
                                        editor.putString("role", "Participant");
                                        editor.commit();

                                    }
                                    else if (rbr.isChecked())
                                    {
                                        //Toast.makeText(MainMenuConsent.this, "Researcher", Toast.LENGTH_LONG).show();
                                        editor = sp.edit();
                                        editor.putString("role", "Researcher");
                                        editor.commit();
                                    }








                                    startService(intent);


                                    finish();
                                }
                                else {
                                    Toast.makeText(MainMenuConsent.this, "Consent is Needed", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    }
                } else  {
                }
            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                //Toast.makeText(getApplicationContext(),exceptionMessage, Toast.LENGTH_LONG).show();
            } catch (Exception e) {

            }
            return null;
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent i = new Intent(this, UserSettingActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                finish();
                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                showUserSettings();
                break;

        }

    }

    private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        StringBuilder builder = new StringBuilder();

        builder.append("\n Username: "
                + sharedPrefs.getString("prefUsername", "NULL"));

        builder.append("\n Send report:"
                + sharedPrefs.getBoolean("prefSendReport", false));

        builder.append("\n Sync Frequency: "
                + sharedPrefs.getString("prefSyncFrequency", "NULL"));

    }*/
}
