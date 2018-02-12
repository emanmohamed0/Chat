package com.app.emaneraky.chat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    Button saveStatus;
    EditText changeStatus;
    private DatabaseReference mStatusDatabaseReference;
    private FirebaseUser currentuser;
    private ProgressDialog mDialog;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        String status_value = getIntent().getStringExtra("status_value");
        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentuser.getUid();
        mStatusDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        toolbar = (Toolbar)findViewById(R.id.status_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update Status");

        saveStatus = (Button) findViewById(R.id.save_statusbtn);
        changeStatus = (EditText) findViewById(R.id.status_input);

        changeStatus.setText(status_value);

        saveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog = new ProgressDialog(StatusActivity.this);
                mDialog.setTitle("Update Status");
                mDialog.setMessage("Please Wait....");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                String updateStatus = changeStatus.getText().toString();
                mStatusDatabaseReference.child("status").setValue(updateStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                        } else {
                            Toast.makeText(StatusActivity.this, "There Some error to save change", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

    }
}
