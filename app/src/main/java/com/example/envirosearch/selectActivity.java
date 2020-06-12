package com.example.envirosearch;

//TODO BEFORE PUBLISHING APP: USE REAL ADS AND CHANGE THE AD ID TO AN HGS ADMOB ACCOUNT

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class selectActivity extends AppCompatActivity {

    private TextView milesView;
    private SeekBar seekBar;
    private Button searchButton;
    private Button questButton;
    private CheckBox checkBox;
    private CheckBox CAABox;
    private CheckBox CWABox;
    private CheckBox RCRABox;
    private CheckBox SDWABox;
    private AdView adView;
    private double radius = 0.5;

    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(0xFF0f0054);
        //toolbar.getBackground().setColorFilter("#");
        setSupportActionBar(toolbar);
        requestPermission();


        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if(firstStart) {
            firstStartDia();
        }


        AdView adView = findViewById(R.id.selectAdView);

        MobileAds.initialize(this, "ca-app-pub-1127915110935547~5457208872");



        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        adView.loadAd(adRequest);

        milesView = findViewById(R.id.milesView);
        seekBar = findViewById(R.id.seekBar);
        searchButton = findViewById(R.id.searchButton);
        questButton = findViewById(R.id.questionButton);
        checkBox = findViewById(R.id.checkBox);
        CAABox = findViewById(R.id.CAABox);
        CWABox = findViewById(R.id.CWABox);
        RCRABox = findViewById(R.id.RCRABox);
        SDWABox = findViewById(R.id.SDWABox);

        questButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreInfo();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(selectActivity.this, MapsActivity.class);
                intent.putExtra("radius", Double.toString(radius));
                intent.putExtra("checked", checkBox.isChecked());
                intent.putExtra("CAAChecked", CAABox.isChecked());
                intent.putExtra("CWAChecked", CWABox.isChecked());
                intent.putExtra("RCRAChecked", RCRABox.isChecked());
                intent.putExtra("SDWAChecked", SDWABox.isChecked());

                mInterstitialAd = new InterstitialAd(selectActivity.this);
                mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

                mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());

                mInterstitialAd.setAdListener(new AdListener() {
                    public void onAdLoaded() {
                        mInterstitialAd.show();
                    }
                });

                startActivity(intent);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double prog = Double.parseDouble(Integer.toString(progress))/10;
                if (prog == 1) {
                    milesView.setText(prog + " Mile");
                }
                else {
                    milesView.setText(prog + " Miles");
                }
                radius = prog;
                System.out.println(radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }

    private void firstStartDia() {
        new AlertDialog.Builder(selectActivity.this)
                .setTitle("")
                .setMessage("I acknowledge that all information presented by this application is retrieved from publicly available records.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("firstStart", false);
                        editor.apply();
                    }
                })

                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = android.os.Process.myPid();
                        android.os.Process.killProcess(id);
                    }
                }).create().show();
    }

    private void showMoreInfo() {
        new AlertDialog.Builder(selectActivity.this)
                .setTitle("More Info:")
                .setMessage("CAA: Clean Air Act\nCWA: Clean Water Act\nRCRA: Resource Conservation and Recovery Act \nSDWA: Safe Drinking Water Act ")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
