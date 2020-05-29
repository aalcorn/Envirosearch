package com.example.envirosearch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class selectActivity extends AppCompatActivity {

    private TextView milesView;
    private SeekBar seekBar;
    private Button searchButton;
    private CheckBox checkBox;
    private CheckBox CAABox;
    private CheckBox CWABox;
    private CheckBox RCRABox;
    private CheckBox SDWABox;
    private double radius = 5;


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

        milesView = findViewById(R.id.milesView);
        seekBar = findViewById(R.id.seekBar);
        searchButton = findViewById(R.id.searchButton);
        checkBox = findViewById(R.id.checkBox);
        CAABox = findViewById(R.id.CAABox);
        CWABox = findViewById(R.id.CWABox);
        RCRABox = findViewById(R.id.RCRABox);
        SDWABox = findViewById(R.id.SDWABox);

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

}
