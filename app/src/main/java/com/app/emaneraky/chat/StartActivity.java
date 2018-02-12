package com.app.emaneraky.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
Button mRegBtn;
Button malreay_account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mRegBtn = (Button)findViewById(R.id.start_reg_btn);
        malreay_account = (Button)findViewById(R.id.alreay_account);


        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent(StartActivity.this,RegisterActivity.class));
                finish();
            }
        });
        malreay_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent(StartActivity.this,LoginActivity.class));
                finish();
            }
        });
    }
}
