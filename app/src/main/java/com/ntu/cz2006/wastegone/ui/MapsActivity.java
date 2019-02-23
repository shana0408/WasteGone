package com.ntu.cz2006.wastegone.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ntu.cz2006.wastegone.R;
import com.ntu.cz2006.wastegone.models.User;
import com.ntu.cz2006.wastegone.models.WasteLocation;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static com.ntu.cz2006.wastegone.Constants.REQUEST_CODE_IMAGE_OPEN;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MapsActivity";
    private static final int DEFAULT_ZOOM = 17;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private User user;

    private GoogleMap mMap;
    private Location mLastLocation;
    private BottomSheetBehavior mBottomSheetBehavior;
    private FloatingActionButton myLocationButton;
    private FloatingActionButton toggleBottomSheetButton;
    private LinearLayout bottomSheet;
    private Button submitRequestButton;
    private Spinner categorySpinner;
    private EditText remarksInput;
    private ImageButton uploadImageButton;
    private ImageView uploadImagePreview;
    private TextView uploadImageTextView;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private TextView userRewardsTextView;
    private ImageView userProfileImageView;

    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationProviderClient = new FusedLocationProviderClient(this);
        user = getUser(FirebaseAuth.getInstance());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initUI();
    }

    private void initUI() {
        findViews();
        initButtonListener();
        loadCategoryIntoSpinner();
        loadUserIntoNavigation();
    }

    private void findViews() {
        myLocationButton = findViewById(R.id.myLocationButton);
        toggleBottomSheetButton = findViewById(R.id.toggleBottomSheetButton);
        bottomSheet = findViewById(R.id.bottomSheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        submitRequestButton = findViewById(R.id.submitRequestButton);
        categorySpinner = findViewById(R.id.categorySpinner);
        remarksInput = findViewById(R.id.remarksInput);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImagePreview = findViewById(R.id.uploadImagePreview);
        uploadImageTextView = findViewById(R.id.uploadImageTextView);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawer_layout);

        View hView = navigationView.getHeaderView(0);
        userNameTextView = hView.findViewById(R.id.userNameTextView);
        userEmailTextView = hView.findViewById(R.id.userEmailTextView);
        userRewardsTextView = hView.findViewById(R.id.userRewardsTextView);
        userProfileImageView = hView.findViewById(R.id.userProfileImageView);
    }

    private void loadUserIntoNavigation() {
        userNameTextView.setText(user.getName());
        userEmailTextView.setText(user.getEmail());
        //userRewardsTextView.setText(user.getRewards());
        Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(userProfileImageView);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastLocation = task.getResult();
                    LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM);
                    mMap.moveCamera(cameraUpdate);
                }
            }
        });

        showWasteOnMap();
    }

    private void initButtonListener() {
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateCameraToCurrentLocation();
            }
        });

        toggleBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        submitRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitWasteRequest();
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                remarksInput.setText("");
            }
        });

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View c) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_IMAGE_OPEN);
            }
        });
    }

    private void showWasteOnMap() {
        final CollectionReference wasteLocationCollection = db.collection("WasteLocation");

        wasteLocationCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        WasteLocation wasteLocation = document.toObject(WasteLocation.class);
                        LatLng latlng = new LatLng(wasteLocation.getGeo_point().getLatitude(), wasteLocation.getGeo_point().getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latlng).title(wasteLocation.getCategory()).icon(BitmapDescriptorFactory.fromResource(customMarker(wasteLocation.getCategory()))));
                    }
                }
            }
        });

        wasteLocationCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    WasteLocation wasteLocation = dc.getDocument().toObject(WasteLocation.class);
                                    LatLng latlng = new LatLng(wasteLocation.getGeo_point().getLatitude(), wasteLocation.getGeo_point().getLongitude());
                                    mMap.addMarker(new MarkerOptions().position(latlng).title(wasteLocation.getCategory()).icon(BitmapDescriptorFactory.fromResource(customMarker(wasteLocation.getCategory()))));
                                case REMOVED:
                                    return;
                            }
                        }
                    }
                });
    }

    private void loadCategoryIntoSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    @SuppressLint("MissingPermission")
    private void animateCameraToCurrentLocation() {
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM);
                    mMap.animateCamera(cameraUpdate);
                    myLocationButton.setColorFilter(Color.argb(255,88,150,228));
                }
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                Rect outRect = new Rect();
                Rect buttonRect = new Rect();
                bottomSheet.getGlobalVisibleRect(outRect);
                toggleBottomSheetButton.getGlobalVisibleRect(buttonRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())
                        && !buttonRect.contains((int) event.getRawX(), (int) event.getRawY()))
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
        myLocationButton.setColorFilter(Color.argb(255,0,0,0));
        return super.dispatchTouchEvent(event);
    }

    private void submitWasteRequest() {
        final StorageReference fileReference = mStorageRef.child("images/" + System.currentTimeMillis() + "." + getFileExtension(selectedImage));
        UploadTask uploadTask = fileReference.putFile(selectedImage);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return fileReference.getDownloadUrl();
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Map<String, Object> wasteLocationDocument = new HashMap<String, Object>();
                GeoPoint geoPoint = new GeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                wasteLocationDocument.put("geo_point", geoPoint);
                wasteLocationDocument.put("category", categorySpinner.getSelectedItem().toString());
                wasteLocationDocument.put("remarks", remarksInput.getText().toString());
                wasteLocationDocument.put("images", uri.toString());

                db.collection("WasteLocation").document().set(wasteLocationDocument)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MapsActivity.this, "WasteLocation added", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MapsActivity.this, "Unable to add", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private int customMarker(String category)
    {
        if(category == "Paper")
        {
            return R.mipmap.paper_pin_foreground;
        }
        else if(category == "Aluminium")
        {
            return R.mipmap.aluminium_pin_foreground;
        }
        else if(category == "Plastic")
        {
            return R.mipmap.plastic_pin_pin_foreground;
        }
        else
        {
            return R.mipmap.aluminium_pin_foreground;
        }
    }

    private User getUser(final FirebaseAuth mAuth)
    {
        DocumentReference docRef = db.collection("User").document(mAuth.getUid());
        user = new User();
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                Toast.makeText(getApplicationContext(), user.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        return user;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.side_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Toast.makeText(getApplicationContext(), "Camera is clicked", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_gallery) {

        }
        else if (id == R.id.nav_slideshow) {

        }
        else if (id == R.id.nav_manage) {

        }
        else if (id == R.id.nav_share) {

        }
        else if (id == R.id.nav_send) {

        }
        else if (id == R.id.nav_logout) {
            Intent i = new Intent(getApplicationContext(),LogoutActivity.class);
            i.putExtra("FROM_ACTIVITY", "MapsActivity");
            startActivity(i);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @android.support.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_IMAGE_OPEN: {
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    selectedImage = data.getData();

                    Picasso.get().load(selectedImage).into(uploadImagePreview);

                }
            }
        }
    }
}