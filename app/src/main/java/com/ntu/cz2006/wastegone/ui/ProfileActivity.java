package com.ntu.cz2006.wastegone.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ntu.cz2006.wastegone.R;
import com.ntu.cz2006.wastegone.models.User;

import android.widget.Toast;


public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        userInfo();

        TextView t2 = (TextView) findViewById(R.id.textView2);
        TextView t3 = (TextView) findViewById(R.id.textView3);

        //t2.setText(acct.getDisplayName());
        t3.setText(String.valueOf(user.getRewards()));

    }

        private void userInfo() {
            db.collection("User")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                                user = document.toObject(User.class);
                            //Toast.makeText(getApplicationContext(),String.valueOf(document.toObject(User.class).getRewards()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

}

