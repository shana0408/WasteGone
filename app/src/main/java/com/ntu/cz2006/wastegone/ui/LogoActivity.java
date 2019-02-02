package com.ntu.cz2006.wastegone.ui;

import android.content.Intent;
import android.os.Handler;
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
        setContentView(R.layout.activity_logo);
        GoogleSignInAccount lastSignIn = GoogleSignIn.getLastSignedInAccount(this);

        if(lastSignIn == null)
        {
            Intent i = new Intent(getApplicationContext(),LoginActivity.class);
            i.putExtra("FROM_ACTIVITY", "LogoActivity");
            startActivity(i);
        }
        else {
            Intent i = new Intent(this,MapsActivity.class);
            i.putExtra("FROM_ACTIVITY", "LogoActivity");
            startActivity(i);
        }
    }
}
