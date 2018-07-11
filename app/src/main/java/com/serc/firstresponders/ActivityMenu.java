package com.serc.firstresponders;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ActivityMenu extends AppCompatActivity {

    private Button btnend, sendloca, sendvital, reqbackup, reqcrimedata;
    private Spinner spinner1;
    private boolean checked1 = false;
    private boolean checked2 = false;

    private DateFormat df;

    private SharedPreferences prefs;



    private String ac, activity, ipaddr, port, uid;

    //use the default shared preference file
    //private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        df  = DateFormat.getDateTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("EST"));


        ac = "None";


        prefs = getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS);
        ipaddr = prefs.getString("ipaddr","");
        port = prefs.getString("port","");
        uid = prefs.getString("uID","");

        Thread thread = new Thread() {
            @Override
            public void run() {

                while(true) {
                    while(true) {

                        prefs = getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("activity", ac);
                        editor.commit();
                    }
                }

            }
        };
        thread.start();



        //Toast.makeText(this, port, Toast.LENGTH_SHORT).show();


        btnend = (Button) findViewById(R.id.btnEnd);
        btnend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getBaseContext(), CollectBandService.class);
                stopService(intent);
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(0);
                finish();

            }
        });

        reqcrimedata = (Button) findViewById(R.id.button5);
        reqcrimedata.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Toast.makeText(getApplicationContext(), "Crime Data Requested", Toast.LENGTH_SHORT).show();
                String messageStr= "Crime data requested from user: " + uid + " at time: " + df.format(new Date());
                if (android.os.Build.VERSION.SDK_INT > 9)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }

                try {
                    DatagramSocket s = new DatagramSocket();
                    //InetAddress local = InetAddress.getByName("192.168.0.9");

                    InetAddress local = InetAddress.getByName(ipaddr);
                    int msg_length=messageStr.length();
                    byte[] message = messageStr.getBytes();
                    DatagramPacket p = new DatagramPacket(message, msg_length,local, Integer.parseInt(port));
                    s.send(p);
                }
                catch (SocketException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e){
                    e.printStackTrace();
                }


            }
        });





        sendloca = (Button) findViewById(R.id.button);
        sendloca.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Toast.makeText(getApplicationContext(), "Location Sent", Toast.LENGTH_SHORT).show();
                String messageStr= "Location sent from user: " + uid + " at time: " + df.format(new Date());
                if (android.os.Build.VERSION.SDK_INT > 9)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }

                try {
                    DatagramSocket s = new DatagramSocket();
                    //InetAddress local = InetAddress.getByName("192.168.0.9");

                    InetAddress local = InetAddress.getByName(ipaddr);
                    int msg_length=messageStr.length();
                    byte[] message = messageStr.getBytes();
                    DatagramPacket p = new DatagramPacket(message, msg_length,local, Integer.parseInt(port));
                    s.send(p);
                }
                catch (SocketException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e){
                    e.printStackTrace();
                }


            }
        });

        reqbackup = (Button) findViewById(R.id.button4);
        reqbackup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Toast.makeText(getApplicationContext(), "Requesting Backup", Toast.LENGTH_SHORT).show();
                String messageStr="Requesting Backup from user: " + uid + " at time: " + df.format(new Date());
                if (android.os.Build.VERSION.SDK_INT > 9)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }

                try {
                    DatagramSocket s = new DatagramSocket();
                    //InetAddress local = InetAddress.getByName("192.168.0.9");

                    InetAddress local = InetAddress.getByName(ipaddr);
                    int msg_length=messageStr.length();
                    byte[] message = messageStr.getBytes();
                    DatagramPacket p = new DatagramPacket(message, msg_length,local, Integer.parseInt(port));
                    s.send(p);
                }
                catch (SocketException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e){
                    e.printStackTrace();
                }


            }
        });

        sendvital = (Button) findViewById(R.id.button3);
        sendvital.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                Toast.makeText(getApplicationContext(), "Vitals Sent", Toast.LENGTH_SHORT).show();
                String messageStr="Vitals Sent from user: " + uid + " at time: " + df.format(new Date());
                if (android.os.Build.VERSION.SDK_INT > 9)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }

                try {
                    DatagramSocket s = new DatagramSocket();
                    //InetAddress local = InetAddress.getByName("192.168.0.9");

                    InetAddress local = InetAddress.getByName(ipaddr);
                    int msg_length=messageStr.length();
                    byte[] message = messageStr.getBytes();
                    DatagramPacket p = new DatagramPacket(message, msg_length,local, Integer.parseInt(port));
                    s.send(p);
                }
                catch (SocketException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e){
                    e.printStackTrace();
                }


            }
        });




        spinner1 = (Spinner) findViewById(R.id.spinner);


        final Context context = this;


        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                ac = parent.getItemAtPosition(position).toString();

                //Toast.makeText(getApplicationContext(), ac, Toast.LENGTH_LONG).show();


                if (!ac.matches("Other")) {
                    broadcastIntent(null);
                }
                else
                {
                    final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);
                    inputAlert.setTitle("Activity");
                    inputAlert.setCancelable(false);
                    inputAlert.setMessage("Please enter the activity name");
                    final EditText userInput = new EditText(context);
                    inputAlert.setView(userInput);
                    inputAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ac = userInput.getText().toString();

                            if(ac.matches(""))
                            {
                                ac = "Other";
                            }
                            broadcastIntent(null);

                        }
                    });
                    /*inputAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ac = "None";
                            prefs = getSharedPreferences("MyPrefs",Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("activity",ac);
                            editor.commit();
                            //dialog.dismiss();
                        }
                    });*/
                    AlertDialog alertDialog = inputAlert.create();
                    alertDialog.show();

                }



            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });



    }

    public void broadcastIntent(View view){
        Intent intent = new Intent();
        intent.setAction("com.tutorialspoint.CUSTOM_INTENT");
        intent.putExtra("activity", ac);
        sendBroadcast(intent);
    }




    @Override
    public void onBackPressed() {
    }
}
