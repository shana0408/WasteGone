package com.ntu.wastegone.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ntu.wastegone.R;

import java.util.HashMap;
import java.util.Map;

import static com.ntu.wastegone.Constants.REQUEST_CODE_SIGN_IN;

/**
 LoginActivity class handles user login
 @author ILoveNTU
 @version 2.1
 @since 2019-01-15
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private SignInButton signInButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                Log.d(TAG, "onActivityResult: GoogleSignInFailed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential: success");
                            saveGoogleUserToFirebase(task.getResult());
                            Toast.makeText(getApplicationContext(),"User Logged in ", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "signInWithCredential: fail");
                            Toast.makeText(getApplicationContext(),"Couldn't log in ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveGoogleUserToFirebase(final AuthResult result)
    {
        DocumentReference docRef = db.collection("User").document(result.getUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Intent i = new Intent(getApplicationContext(), com.ntu.wastegone.ui.MainActivity.class);
                        startActivity(i);
                        finish();
                        Toast.makeText(getApplicationContext(), "User exist", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> userDocument = new HashMap<String, Object>();
                        String email = result.getUser().getEmail();
                        String username = result.getUser().getDisplayName();
                        userDocument.put("email", email);
                        userDocument.put("name", username);
                        userDocument.put("rewards", 0);

                        db.collection("User").document(result.getUser().getUid()).set(userDocument)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent i = new Intent(getApplicationContext(), com.ntu.wastegone.ui.MainActivity.class);
                                        startActivity(i);
                                        finish();
                                        Toast.makeText(getApplicationContext(), "User added", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Unable to add", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to verify", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
