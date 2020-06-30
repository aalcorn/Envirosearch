package com.example.envirosearch;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class moreInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);

        AdView adView = findViewById(R.id.moreInfoAdView);

        MobileAds.initialize(this, "ca-app-pub-1127915110935547~5457208872");

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        adView.loadAd(adRequest);

        final TextView SDWAText = findViewById(R.id.SDWAlinkText);
        final TextView RCRAText = findViewById(R.id.RCRAlinkText);
        final TextView CAAText = findViewById(R.id.CAAlinkText);
        final TextView CWAText = findViewById(R.id.CWAlinkText);

        SDWAText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String website = SDWAText.getText().toString();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                startActivity(browserIntent);
            }
        });
        RCRAText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String website = RCRAText.getText().toString();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                startActivity(browserIntent);
            }
        });
        CAAText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String website = CAAText.getText().toString();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                startActivity(browserIntent);
            }
        });
        CWAText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String website = CWAText.getText().toString();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                startActivity(browserIntent);
            }
        });

    }
}
