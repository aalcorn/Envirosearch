package com.example.envirosearch;

import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

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
import java.util.ArrayList;

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

    TextView epaInfo;
    TextView epaRegion;

    RadioButton CAAButton;
    RadioButton CWAButton;
    RadioButton RCRAButton;
    RadioButton SDWAButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facil);

        AdView adView = findViewById(R.id.facilAdView);

        MobileAds.initialize(this, "ca-app-pub-1127915110935547~5457208872");



        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        adView.loadAd(adRequest);


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
        epaInfo = findViewById(R.id.epaInfoTextView);
        epaInfo.setText("https://echo.epa.gov/");
        epaInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://echo.epa.gov/detailed-facility-report?fid=" + id));
                startActivity(browserIntent);
            }
        });

        epaRegion = findViewById(R.id.epaRegionTextView);

        //Make GET request
        getFacilJson();

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

                        setEPAinfo();

                        //Enable only buttons with statute records
                        for(int i = 0; i < summaries.length(); i++) {
                            JSONObject sum = new JSONObject(summaries.get(i).toString());
                            if(sum.getString("Statute").equals("CAA")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CAAButton.setEnabled(true);
                                    }
                                });
                            }
                            else if(sum.getString("Statute").equals("CWA")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CWAButton.setEnabled(true);
                                    }
                                });
                            }
                            else if(sum.getString("Statute").equals("RCRA")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        RCRAButton.setEnabled(true);
                                    }
                                });
                            }
                            else if(sum.getString("Statute").equals("SDWA")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SDWAButton.setEnabled(true);
                                    }
                                });
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

                                            changeNull();
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

                                            changeNull();
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

                                            changeNull();
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

                                            changeNull();
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

    private void setEPAinfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] stateList = facState.getText().toString().split(" ");
                String state = stateList[0];
                System.out.println(state);
                if(state.equals("CT") || state.equals("ME") || state.equals("MA") || state.equals("NH") || state.equals("RI") || state.equals("VT")) {
                    //Region 1 - Boston
                    epaRegion.setText("EPA Region 1: Boston - (617) 918-1010");
                }
                else if (state.equals("NJ") || state.equals("NY") || state.equals("Puerto Rico")) {
                    //Region 2 - New York City
                    epaRegion.setText("EPA Region 2: New York City - (212) 637-5000");
                }
                else if (state.equals("DE") || state.equals("DC") || state.equals("MD") || state.equals("PA") || state.equals("VA") || state.equals("WV")) {
                    //Region 3 - Philadelphia
                    epaRegion.setText("EPA Region 3: Philadelphia - (215) 814-5000");
                }
                else if (state.equals("AL") || state.equals("FL") || state.equals("GA") || state.equals("KY") || state.equals("MS") || state.equals("NC") || state.equals("SC") || state.equals("TN")) {
                    //Region 4 - Atlanta
                    epaRegion.setText("EPA Region 4: Atlanta - (404) 562-9900");
                }
                else if (state.equals("IL") || state.equals("IN") || state.equals("MI") || state.equals("MN") || state.equals("OH") || state.equals("WI")) {
                    //Region 5 - Chicago
                    epaRegion.setText("EPA Region 5: Chicago - (312) 886-3000");
                }
                else if (state.equals("AR") || state.equals("LA") || state.equals("NM") || state.equals("OK") || state.equals("TX")) {
                    //Region 6 - Dallas
                    epaRegion.setText("EPA Region 6: Dallas - (214) 665-2200");
                }
                else if (state.equals("IA") || state.equals("KS") || state.equals("MO") || state.equals("NE")) {
                    //Region 7 - Kansas City
                    epaRegion.setText("EPA Region 7: Kansas City - (913) 551-7003");
                }
                else if (state.equals("CO") || state.equals("MT") || state.equals("ND") || state.equals("SD") || state.equals("UT") || state.equals("WY")) {
                    //Region 8 - Denver
                    epaRegion.setText("EPA Region 8: Denver - (303) 312-6312");
                }
                else if (state.equals("AZ") || state.equals("CA") || state.equals("HI") || state.equals("NV")) {
                    //Region 9 - San Francisco
                    epaRegion.setText("EPA Region 9: San Francisco - (415) 947-8700");
                }
                else if (state.equals("AK") || state.equals("ID") || state.equals("OR") || state.equals("WA")) {
                    //Region 10 - Seattle
                    epaRegion.setText("EPA Region 10: Seattle - (206) 553-1200");
                }
                else {
                    epaRegion.setText("EPA Region not found");
                }
            }
        });
    }

    //Changes all instances of "null" to dashes for better design
    private void changeNull() {
        final ArrayList<TextView> textList = new ArrayList<>();
        textList.add(inspText);
        textList.add(lastInspText);
        textList.add(compStatusText);
        textList.add(QtrsNCText);
        textList.add(QtrsSNCText);
        textList.add(infActText);
        textList.add(fActText);
        textList.add(penaltyText);
        textList.add(epaText);
        textList.add(epaPenText);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < textList.size();i++) {
                    if(textList.get(i).getText().equals("null")) {
                        textList.get(i).setText("-");
                    }
                }
            }
        });


    }
}
