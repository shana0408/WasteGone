package com.ntu.cz2006.wastegone.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.ntu.cz2006.wastegone.R;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_logo);
        GoogleSignInAccount lastSignIn = GoogleSignIn.getLastSignedInAccount(this);
        if(lastSignIn == null)
        {
            Intent i = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(i);
        }
        else {
            Intent i = new Intent(getApplicationContext(),MapsActivity.class);
            startActivity(i);
        }
    }
}
