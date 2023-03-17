package com.klsoft.custom.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.klsoft.custom.R;
import androidx.appcompat.app.AppCompatActivity;

public class LogoutActivity extends AppCompatActivity {
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        button = (Button) findViewById(R.id.logout);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                LogInActivity();
                finish();
            }
        });
    }
    public void LogInActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
