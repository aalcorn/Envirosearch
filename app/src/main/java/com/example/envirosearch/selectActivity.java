package com.example.envirosearch;

import android.content.Intent;
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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class selectActivity extends AppCompatActivity {

    private TextView milesView;
    private SeekBar seekBar;
    private Button searchButton;
    private CheckBox checkBox;
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

        milesView = findViewById(R.id.milesView);
        seekBar = findViewById(R.id.seekBar);
        searchButton = findViewById(R.id.searchButton);
        checkBox = findViewById(R.id.checkBox);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(selectActivity.this, MapsActivity.class);
                intent.putExtra("radius", Double.toString(radius));
                intent.putExtra("checked", checkBox.isChecked());
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

}
