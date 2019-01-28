package com.ntu.cz2006.wastegone.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.ntu.cz2006.wastegone.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        TextView t1 = (TextView) findViewById(R.id.textView);
        TextView t2 = (TextView) findViewById(R.id.textView2);
        TextView t3 = (TextView) findViewById(R.id.textView3);

        t1.setText(acct.getDisplayName());
   //     t2.setText(acct.getEmail());
   //     t3.setText(acct.getPhotoUrl().toString());
    }
}

