package com.example.envirosearch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class facilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facil);

        //Get facility ID from intent
        //Make GET request
        //Fill in necessary fields with git request info

        System.out.println(getIntent().getExtras().get("id"));
        String text = getIntent().getExtras().getString("id");
        System.out.println(text);

        TextView textView = findViewById(R.id.textView);
        textView.setText(text);
    }
}
