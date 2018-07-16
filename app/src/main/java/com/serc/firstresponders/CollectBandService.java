package com.serc.firstresponders;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.SampleRate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ziggy on 3/31/2017.
 */

public class CollectBandService extends Service {

    private int a,c;
    private float gyroX, gyroY, gyroZ;
    private BandClient client = null;



    private File theFile;
    private File theFile2;
    private File theFile3;


    private PrintWriter theWriter;
    private long time;


    private String name;

    private String collector, role;


    private PowerManager.WakeLock wakeLock;
    private PowerManager mgr;

    private String package_name = "com.microsoft.kapp";
    private String package_name2 = "com.serc.firstresponders";

    private String activity = "None";

    private SharedPreferences prefs;

    private int level;
    @Override
    public IBinder onBind(Intent arg0) {
        //TODO Auto-generated method stub
        return null;
    }

    public void setActivity(String a )
    {
        activity = a;
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {



        PendingIntent pendingIntent =
                PendingIntent.getActivity(CollectBandService.this, 0, new Intent(this, ActivityMenu.class), 0);

        Notification notification =
                new Notification.Builder(CollectBandService.this)
                        .setContentTitle("First Responders")
                        .setContentText("Data is being collected.")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        //.setTicker(getText(R.string.ticker_text))
                        .build();

        startForeground(493, notification);




        //this service will run until we stop it
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        name = intent.getStringExtra("uID");

        

        prefs = getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("activity","None");
        editor.commit();

        collector = prefs.getString("role","");
        role = prefs.getString("studyrole","");


        batteryLevel();
        mgr = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();

        int a = 0;
        int c = 0;
        gyroX = 0;
        gyroY = 0;
        gyroZ = 0;


        time = System.currentTimeMillis();


        Timestamp ts = new Timestamp(time);


        theFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "firstR");
        theFile2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "firstR/" + ts + ".csv");
        theFile3=theFile2;




        try {
            theFile.mkdir();
            theFile2.createNewFile();


            theWriter = new PrintWriter(theFile2);
            theWriter.println("Time, Acceler X, Acceler Y, Acceler Z, Gyro X, Gyro Y, Gyro Z, GSR, HR, user, collector, role, activity, Battery ");
            theWriter.close();

        } catch (IOException x) {
            x.printStackTrace();
        }



        IntentFilter filter = new IntentFilter();
        filter.addAction("com.tutorialspoint.CUSTOM_INTENT");
        registerReceiver(receiver, filter);



        new AccelerometerSubscriptionTask().execute();
        new GsrSubscriptionTask().execute();
        new HeartRateSubscriptionTask().execute();
        new GyroscopeSubscriptionTask().execute();



        //launchActivity(package_name);
        launchActivity(package_name2);

        return START_STICKY;

    }


    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
                batteryLevel();



                //prefs = getSharedPreferences ("MyPrefs", Context.MODE_MULTI_PROCESS);
                //activity = prefs.getString("activity","");


                try{
                    FileWriter fileWritter = new FileWriter(theFile3,true);
                    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                    bufferWritter.write(System.currentTimeMillis()+", " + String.valueOf(event.getAccelerationX()) + ", " +
                            String.valueOf(event.getAccelerationY()) + ", " + String.valueOf(event.getAccelerationZ()) + ", " +
                            String.valueOf(gyroX) + ", " +  String.valueOf(gyroY) + ", " +  String.valueOf(gyroZ) + ", " +
                            String.valueOf(a) + ", " + String.valueOf(c) +", "+name+ ", " + collector + ", " + role + ", " + activity +", " + level + "\n");

                    bufferWritter.close();
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (android.os.Build.VERSION.SDK_INT > 9)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }


            }
        }
    };

    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {

        @Override
        public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
            if (bandGyroscopeEvent != null)
            {
                gyroX = bandGyroscopeEvent.getAccelerationX();
                gyroY = bandGyroscopeEvent.getAccelerationY();
                gyroZ = bandGyroscopeEvent.getAccelerationZ();
            }

        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            activity = intent.getStringExtra("activity");
        }
    };



    private BandGsrEventListener mGsrEventListener = new BandGsrEventListener() {
        @Override
        public void onBandGsrChanged(final BandGsrEvent event) {
            if (event != null) {


                a = event.getResistance();


            }
        }
    };

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {

                c = event.getHeartRate();

            }
        }
    };



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


    private void batteryLevel() {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }



            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }



    @Override
    public void onDestroy() {

        theWriter.close();
        unregisterReceiver(receiver);


        try {
            client.getSensorManager().unregisterAccelerometerEventListener(mAccelerometerEventListener);

        } catch (BandIOException e) {
            e.printStackTrace();
        }
        try {
            client.getSensorManager().unregisterGyroscopeEventListener(mGyroscopeEventListener);

        } catch (BandIOException e) {
            e.printStackTrace();
        }
        try {
            client.getSensorManager().unregisterGsrEventListener(mGsrEventListener);

        } catch (BandIOException e) {
            e.printStackTrace();
        }
        try {
            client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);

        } catch (BandIOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
        //stopForeground(493);
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }




    private class AccelerometerSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {

                if (getConnectedBandClient()) {
                    //Toast.makeText(getApplicationContext(),"Band is connected.", Toast.LENGTH_LONG).show();
                    client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS16);
                } else {
                    //Toast.makeText(getApplicationContext(),"Band isn't connected. Please make sure bluetooth is on and the band is in range.", Toast.LENGTH_LONG).show();
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


    private class GyroscopeSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {

                if (getConnectedBandClient()) {
                    //Toast.makeText(getApplicationContext(),"Band is connected.", Toast.LENGTH_LONG).show();

                    client.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS16);
                } else {
                    //Toast.makeText(getApplicationContext(),"Band isn't connected. Please make sure bluetooth is on and the band is in range.", Toast.LENGTH_LONG).show();
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

    private class GsrSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {

                if (getConnectedBandClient()) {
                    //Toast.makeText(getApplicationContext(),"Band is connected.", Toast.LENGTH_LONG).show();
                    client.getSensorManager().registerGsrEventListener(mGsrEventListener);
                } else {
                    //Toast.makeText(getApplicationContext(),"Band isn't connected. Please make sure bluetooth is on and the band is in range.", Toast.LENGTH_LONG).show();
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
    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    } else {
                    }
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

    public void launchActivity (String package_name) {
        Intent mIntent = getPackageManager().getLaunchIntentForPackage(package_name);
        if (mIntent != null) {
            try {
                startActivity(mIntent);

            } catch (ActivityNotFoundException err) {
                Toast t = Toast.makeText(getApplicationContext(),
                        "app not found", Toast.LENGTH_SHORT);
                t.show();
            }

        }
    }




}
