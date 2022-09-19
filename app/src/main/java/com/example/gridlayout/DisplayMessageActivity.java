package com.example.gridlayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Got to the Display Message Class");
        setContentView(R.layout.display_result);
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        TextView resultView = (TextView) findViewById(R.id.gameResult);
        resultView.setText(result);
        System.out.println("Finished the Display Message Class");
    }

    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
