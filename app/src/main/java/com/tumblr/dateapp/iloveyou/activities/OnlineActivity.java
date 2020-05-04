package com.tumblr.dateapp.iloveyou.activities;



import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.tumblr.dateapp.iloveyou.MainActivity;
import com.tumblr.dateapp.iloveyou.R;
import com.tumblr.dateapp.iloveyou.adapters.ActiveConversationsAdapter;
import com.tumblr.dateapp.iloveyou.adapters.OnlineFragmentPagerAdapter;
import com.tumblr.dateapp.iloveyou.adapters.OnlineUsersAdapter;
import com.tumblr.dateapp.iloveyou.constants.Constant;
import com.tumblr.dateapp.iloveyou.constants.Database;
import com.tumblr.dateapp.iloveyou.fragments.MapFragment;
import com.tumblr.dateapp.iloveyou.models.UserProfile;
import com.tumblr.dateapp.iloveyou.utils.DatabaseUtils;
import com.tumblr.dateapp.iloveyou.utils.NetworkUtils;

import java.util.HashMap;
import java.util.Map;

import static com.tumblr.dateapp.iloveyou.constants.Constant.FIREBASE_STORAGE_REFERENCE;
import static com.tumblr.dateapp.iloveyou.constants.Constant.LOCATION_SERVICES;
import static com.tumblr.dateapp.iloveyou.constants.Constant.NEARBY_CHAT;

public class OnlineActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnlineUsersAdapter.OnAdapterInteractionListener,
        ActiveConversationsAdapter.OnAdapterInteractionListener,
        MapFragment.OnFragmentInteractionListener {

    public static final int INTERVAL = 60000;
    private UserProfile userProfile;
    private ViewPager viewPager;
    private DrawerLayout drawer;
    private String userId;
    private final ValueEventListener userProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(NEARBY_CHAT, "onDataChange: dataSnapshot = [" + dataSnapshot + "]");
            UserProfile userProfileLocal = dataSnapshot.getValue(UserProfile.class);

            if (userProfileLocal != null) {
                userProfile = userProfileLocal;
                Log.w(NEARBY_CHAT, "Online profile loaded for id " + OnlineActivity.this.userId);

                initProfileView(drawer);
            } else {
                Log.w(NEARBY_CHAT, "Error while loading the online sharedPreferences");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(NEARBY_CHAT, "Error database");
        }
    };
    private ProgressBar progressBar;
    private OnlineFragmentPagerAdapter onlineFragmentPagerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private FirebaseStorage firebaseStorage;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private LocationCallback mLocationCallback;
    private int REQUEST_CHECK_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_online);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        AppBarLayout.LayoutParams toolParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        toolParams.setScrollFlags(0);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            // set the firebaseUser sharedPreferences info when the drawer is opening
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    // opening
                    if (!drawer.isDrawerOpen(Gravity.START)) {
                        fillDrawerUserProfile(drawer);
                    }
                }
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_online);

        onlineFragmentPagerAdapter = new OnlineFragmentPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container_online);
        viewPager.setAdapter(onlineFragmentPagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance(FIREBASE_STORAGE_REFERENCE);

        database = FirebaseDatabase.getInstance().getReference();

        firebaseUser = firebaseAuth.getCurrentUser();

        userProfile = new UserProfile();
        userId = DatabaseUtils.getCurrentUUID();

        initLocation();

        passPushTokenToServer();
    }

    // activity logic

    public void requestLogout() {
        Log.d(NEARBY_CHAT, "로그아웃");
        //remove last location from geofire
        DatabaseUtils.getNewLocationDatabase().removeLocation(userId);

        firebaseAuth.signOut();
    }


    public void mountMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    // view logic

    /**
     * Fills the firebaseUser information in the drawer panel
     *
     * @param drawerView the drawer panel
     */
    public void fillDrawerUserProfile(View drawerView) {

        progressBar = (ProgressBar) drawerView.findViewById(R.id.drawer_spinner);

        progressBar.setVisibility(View.VISIBLE);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (NetworkUtils.isAvailable(connectivityManager)) {
            loadProfileOnline();
        } else {
            initProfileView(drawerView);
        }

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.w(Constant.NEARBY_CHAT, "OPTIONS");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {

            requestLogout();
            mountMainActivity();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else {
            return false;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void mountChatActivity(UserProfile userProfile) {
        Intent intent = new Intent(this, ChatActivity.class);
        userProfile.setAvatar(null);
        intent.putExtra(ChatActivity.PARTNER_USER_PROFILE, userProfile);

        startActivity(intent);
    }


    /**
     * Load userProfile for the current firebaseUser with the online version
     * Async task
     */
    private void loadProfileOnline() {
        Log.d(NEARBY_CHAT, "Load profile online and add listener for id " + firebaseUser.getUid());
        database.child(Database.userProfiles).child(firebaseUser.getUid()).addListenerForSingleValueEvent(userProfileListener);
        loadProfileImage();

    }


    private void initProfileView(View drawerView) {

        TextView drawerUserNameView = (TextView) drawerView.findViewById(R.id.drawer_user_name);
        TextView drawerUserGenderView = (TextView) drawerView.findViewById(R.id.drawer_user_gender);
        ImageView drawerUserAvatarView = (ImageView) drawerView.findViewById(R.id.drawer_user_avatar);

        drawerUserNameView.setText(userProfile.getUserName());
        drawerUserGenderView.setText(userProfile.getGender());

        if (userProfile.getAvatar() != null) {
            drawerUserAvatarView.setImageBitmap(userProfile.getAvatar());
        }
    }

    @NonNull
    private StorageReference getStorageReference() {
        return firebaseStorage.getReference("profile/" + firebaseUser.getUid() + ".jpeg");
    }

    private void loadProfileImage() {
        ImageView drawerUserAvatarView = (ImageView) drawer.findViewById(R.id.drawer_user_avatar);
        StorageReference reference = getStorageReference();

        final long ONE_MEGABYTE = 1024 * 1024;
        reference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Data for "profile" is returns, use this as needed
            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            userProfile.setAvatar(avatar);
            drawerUserAvatarView.setImageBitmap(avatar);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.w(NEARBY_CHAT, "loadProfileImage: ", exception);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.child(Database.userProfiles).child(firebaseUser.getUid()).removeEventListener(userProfileListener);
    }

    @Override
    public void addLocationCallback(LocationCallback locationCallback) {
        Log.d(LOCATION_SERVICES, "addLocationCallback: ");
        mLocationCallback = locationCallback;

        //check if permissions not ok
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permission, 0);
            }
        } else {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, null/*looper*/);

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                //call request location updates
                addLocationCallback(mLocationCallback);
                break;
        }
    }

    @Override
    public void removeLocationCallback(LocationCallback locationCallback) {
        Log.d(LOCATION_SERVICES, "removeLocationCallback: ");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    public void initLocation() {
        createLocationRequest();
        //location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        initLocationSettings();

    }

    private void initLocationSettings() {
        //Builder for the location settings provider
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        //add a location request to the lsr
        builder.addLocationRequest(mLocationRequest);
        //we retrieve the client settings
        SettingsClient client = LocationServices.getSettingsClient(this);
        //we check the location settings with the builder
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> Log.d(Constant.LOCATION_SERVICES, "initLocation: ok"));

        task.addOnFailureListener(this, e -> {
            Log.d(Constant.LOCATION_SERVICES, "initLocation: ko");
            // code from the google sample
            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case CommonStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(OnlineActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    break;
            }
        });
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//test 4g power balance
    }
    void passPushTokenToServer(){


        String uid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String,Object> map = new HashMap<>();
        map.put("pushToken",token);


        FirebaseDatabase.getInstance().getReference().child("userProfiles").child(uid).updateChildren(map);


    }

}