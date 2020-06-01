package com.example.envirosearch;

import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class facilActivity extends AppCompatActivity {

    private HttpURLConnection connection;
    private String id;

    TextView facName;
    TextView facStreet;
    TextView facState;

    TextView inspText;
    TextView lastInspText;
    TextView compStatusText;
    TextView QtrsNCText;
    TextView QtrsSNCText;
    TextView infActText;
    TextView fActText;
    TextView penaltyText;
    TextView epaText;
    TextView epaPenText;

    RadioButton CAAButton;
    RadioButton CWAButton;
    RadioButton RCRAButton;
    RadioButton SDWAButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facil);

        CAAButton = findViewById(R.id.CAAButton);
        CWAButton = findViewById(R.id.CWAButton);
        RCRAButton = findViewById(R.id.RCRAButton);
        SDWAButton = findViewById(R.id.SDWAButton);

        CAAButton.setEnabled(false);
        CWAButton.setEnabled(false);
        RCRAButton.setEnabled(false);
        SDWAButton.setEnabled(false);

        facName = findViewById(R.id.facNameTextView);
        facStreet = findViewById(R.id.FacStreetTextView);
        facState = findViewById(R.id.FacStateTextView);

        inspText = findViewById(R.id.InspTextView);
        lastInspText = findViewById(R.id.LastInspTextView);
        compStatusText = findViewById(R.id.VioTextView);
        QtrsNCText = findViewById(R.id.NCTextView);
        QtrsSNCText = findViewById(R.id.SNCTextView);
        infActText = findViewById(R.id.InformalTextView);
        fActText = findViewById(R.id.FormalTextView);
        penaltyText = findViewById(R.id.penaltyTextView);
        epaText = findViewById(R.id.casesTextView);
        epaPenText = findViewById(R.id.epaPenaltiesTextView);

        //Get facility ID from intent
        id = getIntent().getExtras().getString("id");
        //TextView textView = findViewById(R.id.textView);
        //textView.setText(id);

        //Make GET request
        getFacilJson();

        //Fill in fields with info

    }

    private void getFacilJson() {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    BufferedReader reader;
                    String line;
                    StringBuffer responseContent = new StringBuffer();

                    try {

                        // Set up and perform get request
                        URL url = new URL("https://ofmpub.epa.gov/echo/dfr_rest_services.get_dfr?output=JSON&p_id=" + id);
                        connection = (HttpURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);

                        int status = connection.getResponseCode();

                        if (status> 299) { // Error code
                            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                            while((line = reader.readLine()) != null) {
                                responseContent.append(line);
                            }
                            reader.close();
                        }
                        else { // Populate responseContent with info from get request
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            while((line = reader.readLine()) != null) {
                                responseContent.append(line);
                            }
                            System.out.println("Success!");
                            reader.close();
                        }

                        //parse the content
                        JSONObject facilityInfo = new JSONObject(responseContent.toString());
                        JSONObject results = new JSONObject(facilityInfo.get("Results").toString());
                        final JSONArray permits = new JSONArray(results.get("Permits").toString());
                        final JSONObject enforcementSummaries = new JSONObject(results.get("EnforcementComplianceSummaries").toString());
                        final JSONArray summaries = new JSONArray(enforcementSummaries.get("Summaries").toString());
                        final JSONObject permitObject = new JSONObject(permits.get(0).toString());
                        //System.out.println(permits.get(0));
                        System.out.println(summaries.toString());


                        //set Facility Address
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    facName.setText(permitObject.getString("FacilityName"));
                                    facStreet.setText(permitObject.getString("FacilityStreet") + " " + permitObject.getString("FacilityCity"));
                                    facState.setText(permitObject.getString("FacilityState") + " " + permitObject.getString("FacilityZip"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                        for(int i = 0; i < summaries.length(); i++) {
                            JSONObject sum = new JSONObject(summaries.get(i).toString());
                            if(sum.getString("Statute").equals("CAA")) {
                                CAAButton.setEnabled(true);
                            }
                            else if(sum.getString("Statute").equals("CWA")) {
                                CWAButton.setEnabled(true);
                            }
                            else if(sum.getString("Statute").equals("RCRA")) {
                                RCRAButton.setEnabled(true);
                            }
                            else if(sum.getString("Statute").equals("SDWA")) {
                                SDWAButton.setEnabled(true);
                            }
                        }

                        //ON CLICK LISTNERS FOR BUTTONS: EACH POPULATES TABLE WITH DATA RELATED TO STATUTE
                        CAAButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //System.out.println("CAA");
                                for(int i = 0; i < summaries.length(); i++) {
                                    JSONObject sum;
                                    try {
                                        sum = new JSONObject(summaries.get(i).toString());
                                        System.out.println(sum.getString("Statute"));
                                        if(sum.getString("Statute").equals("CAA")) {
                                            System.out.println("Got it!");
                                            inspText.setText(sum.getString("Inspections"));
                                            lastInspText.setText(sum.getString("LastInspection"));
                                            compStatusText.setText(sum.getString("CurrentStatus"));
                                            QtrsNCText.setText(sum.getString("QtrsInNC"));
                                            QtrsSNCText.setText(sum.getString("QtrsInSNC"));
                                            infActText.setText(sum.getString("InformalActions"));
                                            fActText.setText(sum.getString("FormalActions"));
                                            penaltyText.setText(sum.getString("TotalPenalties"));
                                            epaText.setText(sum.getString("Cases"));
                                            epaPenText.setText(sum.getString("TotalCasePenalties"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        CWAButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for(int i = 0; i < summaries.length(); i++) {
                                    JSONObject sum;
                                    try {
                                        sum = new JSONObject(summaries.get(i).toString());
                                        System.out.println(sum.getString("Statute"));
                                        if(sum.getString("Statute").equals("CWA")) {
                                            System.out.println("Got it!");
                                            inspText.setText(sum.getString("Inspections"));
                                            lastInspText.setText(sum.getString("LastInspection"));
                                            compStatusText.setText(sum.getString("CurrentStatus"));
                                            QtrsNCText.setText(sum.getString("QtrsInNC"));
                                            QtrsSNCText.setText(sum.getString("QtrsInSNC"));
                                            infActText.setText(sum.getString("InformalActions"));
                                            fActText.setText(sum.getString("FormalActions"));
                                            penaltyText.setText(sum.getString("TotalPenalties"));
                                            epaText.setText(sum.getString("Cases"));
                                            epaPenText.setText(sum.getString("TotalCasePenalties"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        RCRAButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for(int i = 0; i < summaries.length(); i++) {
                                    JSONObject sum;
                                    try {
                                        sum = new JSONObject(summaries.get(i).toString());
                                        System.out.println(sum.getString("Statute"));
                                        if(sum.getString("Statute").equals("RCRA")) {
                                            System.out.println("Got it!");
                                            inspText.setText(sum.getString("Inspections"));
                                            lastInspText.setText(sum.getString("LastInspection"));
                                            compStatusText.setText(sum.getString("CurrentStatus"));
                                            QtrsNCText.setText(sum.getString("QtrsInNC"));
                                            QtrsSNCText.setText(sum.getString("QtrsInSNC"));
                                            infActText.setText(sum.getString("InformalActions"));
                                            fActText.setText(sum.getString("FormalActions"));
                                            penaltyText.setText(sum.getString("TotalPenalties"));
                                            epaText.setText(sum.getString("Cases"));
                                            epaPenText.setText(sum.getString("TotalCasePenalties"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        SDWAButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for(int i = 0; i < summaries.length(); i++) {
                                    JSONObject sum;
                                    try {
                                        sum = new JSONObject(summaries.get(i).toString());
                                        System.out.println(sum.getString("Statute"));
                                        if(sum.getString("Statute").equals("SDWA")) {
                                            System.out.println("Got it!");
                                            inspText.setText(sum.getString("Inspections"));
                                            lastInspText.setText(sum.getString("LastInspection"));
                                            compStatusText.setText(sum.getString("CurrentStatus"));
                                            QtrsNCText.setText(sum.getString("QtrsInNC"));
                                            QtrsSNCText.setText(sum.getString("QtrsInSNC"));
                                            infActText.setText(sum.getString("InformalActions"));
                                            fActText.setText(sum.getString("FormalActions"));
                                            penaltyText.setText(sum.getString("TotalPenalties"));
                                            epaText.setText(sum.getString("Cases"));
                                            epaPenText.setText(sum.getString("TotalCasePenalties"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                }
            }
        });
        thread.start();
    }
}
