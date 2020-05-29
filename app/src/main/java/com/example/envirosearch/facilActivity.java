package com.example.envirosearch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facil);

        facName = findViewById(R.id.facNameTextView);
        facStreet = findViewById(R.id.FacStreetTextView);
        facState = findViewById(R.id.FacStateTextView);

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

                        //System.out.println(responseContent.toString());
                        //parse the content
                        JSONObject facilityInfo = new JSONObject(responseContent.toString());
                        JSONObject results = new JSONObject(facilityInfo.get("Results").toString());
                        JSONArray permits = new JSONArray(results.get("Permits").toString());
                        final JSONObject permitObject = new JSONObject(permits.get(0).toString());
                        //System.out.println(permits.get(0));


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
