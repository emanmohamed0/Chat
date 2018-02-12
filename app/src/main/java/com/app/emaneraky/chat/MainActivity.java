package com.app.emaneraky.chat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private DatabaseReference userDatabaseRef;
    private ViewPager mViewpage;
    private SectionsPagerAdapter sectionsPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
//        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        toolbar.setTitle("Chat");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //tabs
        mViewpage = (ViewPager) findViewById(R.id.tabPage);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewpage.setAdapter(sectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewpage);

        if (mAuth.getCurrentUser() != null) {
            userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startIntent);
            finish();
        } else {
            userDatabaseRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //userDatabaseRef.child("online").setValue(false); replace false with timestamp it will directy set online to time
            //TIMESTAMP if it true mean is online
            userDatabaseRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startIntent);
        } else if (item.getItemId() == R.id.main_setting) {
            Intent startIntent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(startIntent);
        } else if (item.getItemId() == R.id.main_all_users) {
            Intent startIntent = new Intent(MainActivity.this, AllUserActivity.class);
            startActivity(startIntent);
        }

        return true;
    }
}
