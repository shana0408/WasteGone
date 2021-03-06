package com.ntu.wastegone.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ntu.wastegone.R;
import com.ntu.wastegone.models.RecycleCenter;
import com.ntu.wastegone.models.User;
import com.ntu.wastegone.models.WasteLocation;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.ntu.wastegone.Constants.DATE_FORMAT;
import static com.ntu.wastegone.Constants.REQUEST_CODE_IMAGE_OPEN;
import static com.ntu.wastegone.Constants.WASTE_LOCATION_STATUS_COLLECTED;
import static com.ntu.wastegone.Constants.WASTE_LOCATION_STATUS_OPEN;
import static com.ntu.wastegone.Constants.WASTE_LOCATION_STATUS_RESERVED;

/**
 MapActivity class displays map , waste location and handling submit request, make
 reservation activity.
 @author ILoveNTU
 @version 2.1
 @since 2019-01-15
 */

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback, OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MapsActivity";
    private static final int DEFAULT_ZOOM = 17;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FirebaseUser firebaseUser;
    private DocumentReference dbUser;
    private User localUser;
    private GoogleMap mMap;
    private Location mLastLocation;

    private FloatingActionButton myLocationButton;
    private FloatingActionButton toggleSubmitBottomSheetButton;
    private FloatingActionButton directionButton;
    private BottomSheetBehavior submitFormBottomSheetBehavior;
    private BottomSheetBehavior wasteLocationDetailBottomSheetBehavior;
    private LinearLayout submitFormBottomSheet;
    private LinearLayout wasteLocationDetailBottomSheet;

    //Submit Form BottomSheet
    private Button submitRequestButton;
    private Spinner categorySpinner;
    private EditText remarksInput;
    private Uri selectedImage;
    private ImageView uploadImageButton;
    private ImageView showImage;
    private ProgressBar submitProgressBar;
    private TextView submitAddressTextView;

    //Waste Location Detail BottomSheet
    private TextView titleTextView;
    private TextView remarksTextView;
    private TextView requesterNameTextView;
    private ImageView wasteImageView;
    private Button reserveCollectButton;
    private ProgressBar reserveProgressBar;
    private TextView detailAddressTextView;
    private TextView submitDateView;

    //Navigation Drawer
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private MenuItem userRewardsTextView;
    private ImageView userProfileImageView;

    private HashMap<String, Marker> mapMarkerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationProviderClient = new FusedLocationProviderClient(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mapMarkerManager = new HashMap<String, Marker>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initUI();
    }

    private void initUI() {
        findViews();
        initButtonListener();
        loadCategoryIntoSpinner();
        loadUserIntoNavigation();

        wasteLocationDetailBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {

            }

            @Override
            public void onSlide(@NonNull View view, float slideOffset) {
                GeoPoint geoPoint = (GeoPoint) wasteLocationDetailBottomSheet.getTag();
                LatLng latlng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                switch (wasteLocationDetailBottomSheetBehavior.getState()) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        setMapPaddingBotttom(slideOffset * wasteLocationDetailBottomSheet.getHeight());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        setMapPaddingBotttom(slideOffset * wasteLocationDetailBottomSheet.getHeight());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                }
            }
        });
    }

    private void findViews() {
        myLocationButton = findViewById(R.id.myLocationButton);
        toggleSubmitBottomSheetButton = findViewById(R.id.toggleBottomSheetButton);
        directionButton = findViewById(R.id.directionButton);
        submitFormBottomSheet = findViewById(R.id.submitFormBottomSheet);
        wasteLocationDetailBottomSheet = findViewById(R.id.wasteLocationDetailBottomSheet);
        submitFormBottomSheetBehavior = BottomSheetBehavior.from(submitFormBottomSheet);
        wasteLocationDetailBottomSheetBehavior = BottomSheetBehavior.from(wasteLocationDetailBottomSheet);

        submitRequestButton = submitFormBottomSheet.findViewById(R.id.submitRequestButton);
        categorySpinner = submitFormBottomSheet.findViewById(R.id.categorySpinner);
        remarksInput = submitFormBottomSheet.findViewById(R.id.remarksInput);
        uploadImageButton = submitFormBottomSheet.findViewById(R.id.uploadImageButton);
        showImage = submitFormBottomSheet.findViewById(R.id.showImage);
        submitProgressBar = submitFormBottomSheet.findViewById(R.id.submitRequestProgressBar);
        submitAddressTextView = submitFormBottomSheet.findViewById(R.id.addressTextView);

        titleTextView = wasteLocationDetailBottomSheet.findViewById(R.id.titleTextView);
        remarksTextView = wasteLocationDetailBottomSheet.findViewById(R.id.remarksTextView);
        requesterNameTextView = wasteLocationDetailBottomSheet.findViewById(R.id.requesterNameTextView);
        wasteImageView = wasteLocationDetailBottomSheet.findViewById(R.id.wasteImageView);
        reserveCollectButton = wasteLocationDetailBottomSheet.findViewById(R.id.reserveCollectButton);
        reserveProgressBar = wasteLocationDetailBottomSheet.findViewById(R.id.reserveRequestProgressBar);
        detailAddressTextView = wasteLocationDetailBottomSheet.findViewById(R.id.addressTextView);
        submitDateView = wasteLocationDetailBottomSheet.findViewById(R.id.submitDateView);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawer_layout);

        View hView = navigationView.getHeaderView(0);
        userNameTextView = hView.findViewById(R.id.userNameTextView);
        userEmailTextView = hView.findViewById(R.id.userEmailTextView);
        userProfileImageView = hView.findViewById(R.id.userProfileImageView);

        userRewardsTextView = navigationView.getMenu().findItem(R.id.userRewardsTextView);
    }

    private void loadUserIntoNavigation() {
        dbUser = db.collection("User").document(firebaseUser.getUid());

        dbUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                localUser = documentSnapshot.toObject(User.class);
                userRewardsTextView.setTitle(localUser.getRewards() + " Points  •  Rewards");
            }
        });

        dbUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                localUser = documentSnapshot.toObject(User.class);
                userRewardsTextView.setTitle(localUser.getRewards() + " Points  •  Rewards");
            }
        });

        userNameTextView.setText(firebaseUser.getDisplayName());
        userEmailTextView.setText(firebaseUser.getEmail());
        Picasso.get().load(firebaseUser.getPhotoUrl()).into(userProfileImageView);
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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.setOnMarkerClickListener(this);

        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastLocation = task.getResult();
                    LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM);
                    GeoPoint geoPoint = new GeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.moveCamera(cameraUpdate);
                }
            }
        });

        subscribeWasteLocation();
        subscribeRecycleCenter();
    }


    private void initButtonListener() {
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateCameraToCurrentLocation();
            }
        });

        toggleSubmitBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (submitFormBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    submitFormBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            mLastLocation = task.getResult();
                            GeoPoint geoPoint = new GeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            submitAddressTextView.setText(getAddressName(geoPoint));
                        }
                    });

                } else {
                    submitFormBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        submitRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRequestButton.setEnabled(false);
                submitProgressBar.setVisibility(View.VISIBLE);
                submitProgressBar.bringToFront();
                submitRequestButton.setText("");
                submitWasteRequest();
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

        reserveCollectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserveCollectButton.setEnabled(false);
                reserveProgressBar.setVisibility(View.VISIBLE);
                reserveProgressBar.bringToFront();
                reserveCollectButton.setText("");
                reserveCollectRequest(v);
            }
        });
    }

    private void subscribeWasteLocation() {
        final CollectionReference wasteLocationCollection = db.collection("WasteLocation");

        wasteLocationCollection
                .whereEqualTo("status", WASTE_LOCATION_STATUS_OPEN).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    WasteLocation wasteLocation = document.toObject(WasteLocation.class);
                    wasteLocation.setId(document.getId());

                    if (mapMarkerManager.get(wasteLocation.getId()) == null) {
                        LatLng latlng = new LatLng(wasteLocation.getGeo_point().getLatitude(), wasteLocation.getGeo_point().getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions().position(latlng).title(wasteLocation.getCategory()));
                        marker.setTag(wasteLocation);

                        mapMarkerManager.put(wasteLocation.getId(), marker);
                    }
                }
            }
            }
        });

        wasteLocationCollection
                .whereEqualTo("status", WASTE_LOCATION_STATUS_RESERVED)
                .whereEqualTo("collectorUid", firebaseUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        WasteLocation wasteLocation = document.toObject(WasteLocation.class);
                        wasteLocation.setId(document.getId());

                        if (mapMarkerManager.get(wasteLocation.getId()) == null) {
                            LatLng latlng = new LatLng(wasteLocation.getGeo_point().getLatitude(), wasteLocation.getGeo_point().getLongitude());
                            Marker marker = mMap.addMarker(new MarkerOptions().position(latlng).title(wasteLocation.getCategory()));
                            marker.setTag(wasteLocation);

                            mapMarkerManager.put(wasteLocation.getId(), marker);
                        }
                    }
                }
            }
        });

        wasteLocationCollection
                .whereEqualTo("status", WASTE_LOCATION_STATUS_OPEN)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    WasteLocation wasteLocation = dc.getDocument().toObject(WasteLocation.class);
                    wasteLocation.setId(dc.getDocument().getId());
                    LatLng latlng = new LatLng(wasteLocation.getGeo_point().getLatitude(), wasteLocation.getGeo_point().getLongitude());

                    Marker existingMarker = mapMarkerManager.get(wasteLocation.getId());

                    switch (dc.getType()) {
                        case ADDED:
                            if (existingMarker == null) {
                                Marker marker = mMap.addMarker(new MarkerOptions().position(latlng).title(wasteLocation.getCategory()));
                                marker.setTag(wasteLocation);
                                mapMarkerManager.put(wasteLocation.getId(), marker);
                            }

                            break;
                        case REMOVED:
                            if (existingMarker != null) {
                                existingMarker.remove();
                                mapMarkerManager.remove(wasteLocation.getId());
                            }
                            break;
                        case MODIFIED:
                            if (wasteLocation.getStatus().equals(WASTE_LOCATION_STATUS_OPEN) || (wasteLocation.getStatus().equals(WASTE_LOCATION_STATUS_RESERVED) && wasteLocation.getCollectorUid().equals(firebaseUser.getUid()))) {
                                if (existingMarker != null) {
                                    existingMarker.setPosition(latlng);
                                    existingMarker.setTitle(wasteLocation.getCategory());
                                    existingMarker.setTag(wasteLocation);

                                    mapMarkerManager.put(wasteLocation.getId(), existingMarker);
                                }
                                else {
                                    Marker marker = mMap.addMarker(new MarkerOptions().position(latlng).title(wasteLocation.getCategory()));
                                    marker.setTag(wasteLocation);
                                    mapMarkerManager.put(wasteLocation.getId(), marker);
                                }
                            }
                            else {
                                if (existingMarker != null) {
                                    existingMarker.remove();
                                    mapMarkerManager.remove(wasteLocation.getId());
                                }
                            }
                            break;
                    }
                }
            }
        });

        wasteLocationCollection
                .whereEqualTo("status", WASTE_LOCATION_STATUS_RESERVED)
                .whereEqualTo("collectorUid", firebaseUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            WasteLocation wasteLocation = dc.getDocument().toObject(WasteLocation.class);
                            wasteLocation.setId(dc.getDocument().getId());
                            LatLng latlng = new LatLng(wasteLocation.getGeo_point().getLatitude(), wasteLocation.getGeo_point().getLongitude());

                            Marker existingMarker = mapMarkerManager.get(wasteLocation.getId());

                            switch (dc.getType()) {
                                case ADDED:
                                    if (existingMarker == null) {
                                        Marker marker = mMap.addMarker(new MarkerOptions().position(latlng).title(wasteLocation.getCategory()));
                                        marker.setTag(wasteLocation);
                                        mapMarkerManager.put(wasteLocation.getId(), marker);
                                    }

                                    break;
                                case REMOVED:
                                    if (existingMarker != null) {
                                        existingMarker.remove();
                                        mapMarkerManager.remove(wasteLocation.getId());
                                    }
                                    break;
                                case MODIFIED:
                                    if ((wasteLocation.getStatus().equals(WASTE_LOCATION_STATUS_RESERVED) &&
                                        wasteLocation.getCollectorUid().equals(firebaseUser.getUid())) ||
                                        wasteLocation.getStatus().equals(WASTE_LOCATION_STATUS_OPEN)) {
                                        if (existingMarker != null) {
                                            existingMarker.setPosition(latlng);
                                            existingMarker.setTitle(wasteLocation.getCategory());
                                            existingMarker.setTag(wasteLocation);

                                            mapMarkerManager.put(wasteLocation.getId(), existingMarker);
                                        }
                                        else {
                                            Marker marker = mMap.addMarker(new MarkerOptions().position(latlng).title(wasteLocation.getCategory()));
                                            marker.setTag(wasteLocation);
                                            mapMarkerManager.put(wasteLocation.getId(), marker);
                                        }
                                    }
                                    else {
                                        if (existingMarker != null) {
                                            existingMarker.remove();
                                            mapMarkerManager.remove(wasteLocation.getId());
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                });
    }

    private void subscribeRecycleCenter() {
        final CollectionReference recycleCenterCollection = db.collection("RecycleCenter");

        recycleCenterCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        RecycleCenter recycleCenter = document.toObject(RecycleCenter.class);
                        recycleCenter.setId(document.getId());

                        LatLng latlng = new LatLng(recycleCenter.getGeoPoint().getLatitude(), recycleCenter.getGeoPoint().getLongitude());

                        Marker marker = mMap.addMarker(new MarkerOptions().position(latlng).title(recycleCenter.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        marker.setTag(recycleCenter);
                    }
                }
            }
        });
    }

    private void loadCategoryIntoSpinner() {
        String[] categories = new String[]{"Aluminium", "E-Waste", "Plastic", "Paper"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextSize(16);
                return v;
            }

            public View getDropDownView(int position, View convertView,ViewGroup parent) {
                View v = super.getDropDownView(position, convertView,parent);
                ((TextView) v).setGravity(Gravity.CENTER);
                return v;
            }
        };
        categorySpinner.setAdapter(adapter);
    }

    @SuppressLint("MissingPermission")
    private void animateCameraToCurrentLocation() {
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
            if (task.isSuccessful()) {
                mLastLocation = task.getResult();
                LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
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
            if (submitFormBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                Rect outRect = new Rect();
                Rect buttonRect = new Rect();
                submitFormBottomSheet.getGlobalVisibleRect(outRect);
                toggleSubmitBottomSheetButton.getGlobalVisibleRect(buttonRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()) && !buttonRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    submitFormBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
            else if (wasteLocationDetailBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                Rect outRect = new Rect();
                wasteLocationDetailBottomSheet.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    wasteLocationDetailBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
            else if (directionButton.getVisibility() == View.VISIBLE) {
                Rect outRect = new Rect();
                directionButton.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    directionButton.hide();
                    directionButton.setOnClickListener(null);
                }
            }
        }
        myLocationButton.setColorFilter(Color.argb(255,0,0,0));
        return super.dispatchTouchEvent(event);
    }

    private void submitWasteRequest() {
        if (selectedImage == null) {
            Toast.makeText(this, "Please select photo", Toast.LENGTH_SHORT).show();
            return;
        }

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
            final GeoPoint geoPoint = new GeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            Date submitDate = Calendar.getInstance().getTime();

            WasteLocation wasteLocation = new WasteLocation(firebaseUser.getUid(), null, geoPoint,
                categorySpinner.getSelectedItem().toString(), remarksInput.getText().toString(),
                uri.toString(), WASTE_LOCATION_STATUS_OPEN, submitAddressTextView.getText().toString(), submitDate, null);

            db.collection("WasteLocation").document().set(wasteLocation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MapsActivity.this, "WasteLocation added", Toast.LENGTH_SHORT).show();
                        submitFormBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                        submitProgressBar.setVisibility(View.INVISIBLE);
                        submitRequestButton.setEnabled(true);
                        submitRequestButton.setText("SUBMIT REQUEST");

                        remarksInput.setText("");
                        selectedImage = null;
                        showImage.setImageResource(0);
                        userGetRewards(25);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity.this, "Unable to add", Toast.LENGTH_SHORT).show();
                        submitProgressBar.setVisibility(View.INVISIBLE);
                        submitRequestButton.setEnabled(true);
                        submitRequestButton.setText("SUBMIT REQUEST");
                    }
                });
            }
        });
    }

    private String getAddressName(GeoPoint geoPoint)
    {
        String myAddress = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(),geoPoint.getLongitude(),1);
            myAddress = addresses.get(0).getAddressLine(0);
        }catch (IOException e){
            e.printStackTrace();
        }
        return myAddress;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else if (submitFormBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            submitFormBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else if (wasteLocationDetailBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            wasteLocationDetailBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        Intent i = null;

        if (id == R.id.userRewardsTextView)
        {
            i = new Intent(getApplicationContext(), com.ntu.wastegone.ui.RewardActivity.class);
        }
        else if (id == R.id.nav_request)
        {
            i = new Intent(getApplicationContext(), com.ntu.wastegone.ui.RequestActivity.class);
        }
        else if (id == R.id.nav_reservation)
        {
            i = new Intent(getApplicationContext(), com.ntu.wastegone.ui.ReservationActivity.class);
        }
        else if (id == R.id.nav_logout)
        {
            i = new Intent(getApplicationContext(),LogoutActivity.class);
        }
        else
        {
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        i.putExtra("FROM_ACTIVITY", "MapsActivity");
        startActivity(i);
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTag() instanceof WasteLocation) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            WasteLocation wasteLocation = (WasteLocation) marker.getTag();

            mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    mLastLocation = task.getResult();
                }
            });

            if (wasteLocationDetailBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                titleTextView.setText(wasteLocation.getCategory());
                detailAddressTextView.setText(wasteLocation.getAddress());
                remarksTextView.setText(wasteLocation.getRemarks());
                submitDateView.setText(dateFormat.format(wasteLocation.getSubmitDate()));

                if (wasteLocation.getStatus().equals(WASTE_LOCATION_STATUS_OPEN)) {
                    reserveCollectButton.setText("Reserve");
                    reserveCollectButton.setTag(wasteLocation);
                }
                else if (wasteLocation.getStatus().equals(WASTE_LOCATION_STATUS_RESERVED) && firebaseUser.getUid().equals(wasteLocation.getCollectorUid())) {
                    reserveCollectButton.setText("Collect");
                    reserveCollectButton.setTag(wasteLocation);
                }
                else {
                    reserveCollectButton.setText("Collected, Closed");
                    reserveCollectButton.setEnabled(false);
                    reserveCollectButton.setBackgroundColor(Color.parseColor("gray"));
                }

                db.collection("User").document(wasteLocation.getRequesterUid()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user = documentSnapshot.toObject(User.class);
                                requesterNameTextView.setText(user.getName());
                            }
                        });
                Picasso.get().load(wasteLocation.getImageUri()).into(wasteImageView);
                wasteLocationDetailBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                wasteLocationDetailBottomSheet.setTag(wasteLocation.getGeo_point());
            }
            else {
                wasteLocationDetailBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
        else if (marker.getTag() instanceof RecycleCenter) {
            RecycleCenter recycleCenter = (RecycleCenter) marker.getTag();
            final GeoPoint geoPoint = recycleCenter.getGeoPoint();
            directionButton.show();
            directionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + geoPoint.getLatitude() + "," + geoPoint.getLongitude()));
                    startActivity(intent);
                }
            });
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_IMAGE_OPEN: {
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    selectedImage = data.getData();
                    Toast.makeText(MapsActivity.this, selectedImage.toString(), Toast.LENGTH_SHORT).show();
                    Picasso.get().load(selectedImage).into(showImage);
                }
            }
        }
    }

    private void reserveCollectRequest(View v) {
        final Button button = (Button) v;
        WasteLocation wasteLocation = (WasteLocation) button.getTag();
        button.setEnabled(false);

        if (wasteLocation.getStatus().equals(WASTE_LOCATION_STATUS_OPEN)) {
            wasteLocation.setCollectorUid(firebaseUser.getUid());
            wasteLocation.setStatus(WASTE_LOCATION_STATUS_RESERVED);
        }
        else if (wasteLocation.getStatus().equals(WASTE_LOCATION_STATUS_RESERVED)) {
            Location waste = new Location("");
            waste.setLatitude(wasteLocation.getGeo_point().getLatitude());
            waste.setLongitude(wasteLocation.getGeo_point().getLongitude());

            int distance = (int) waste.distanceTo(mLastLocation);

            if (distance < 10) {
                wasteLocation.setCollectorUid(firebaseUser.getUid());
                wasteLocation.setStatus(WASTE_LOCATION_STATUS_COLLECTED);
                wasteLocation.setCollectDate(new Date());
            }
            else {
                Toast.makeText(this, "Please get closer, distance " + distance, Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
                return;
            }
        }

        db.collection("WasteLocation").document(wasteLocation.getId()).set(wasteLocation)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                reserveProgressBar.setVisibility(View.INVISIBLE);
                wasteLocationDetailBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                button.setEnabled(true);

                localUser.setRewards(localUser.getRewards() + 50);
                dbUser.set(localUser);
            }
        });
    }

    private void userGetRewards(int point) {
        localUser.setRewards(localUser.getRewards() + point);
        dbUser.set(localUser);
    }

    private void setMapPaddingBotttom(Float offset) {
        mMap.setPadding(0, 0, 0, Math.round(offset));
    }
}

