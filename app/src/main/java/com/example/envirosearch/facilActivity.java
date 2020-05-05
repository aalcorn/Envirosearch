package com.example.envirosearch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facil);

        //Get facility ID from intent
        //Make GET request
        //Fill in necessary fields with git request info

        id = getIntent().getExtras().getString("id");

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

                        //parse(responseContent.toString());

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
