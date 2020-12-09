package com.example.interactivemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private PermissionsManager permissions;
    protected MapboxMap mapBox;
    private MapView map;

    private Button optionsMenu;
    private Button gpsLocation;

    private SlidrInterface slidr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.map_screen);

        SlidrConfig config = new SlidrConfig.Builder().position(SlidrPosition.BOTTOM).build();
        slidr = Slidr.attach(this, config);

        map = findViewById(R.id.map);
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);

        optionsMenu = findViewById(R.id.mapScreen_optionsMenu);
        optionsMenu.setOnClickListener(this);

        gpsLocation = findViewById(R.id.mapScreen_gpsLocationButton);
        gpsLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mapScreen_optionsMenu: {
                showOptionsMenu(v);
            } break;
            case R.id.mapScreen_gpsLocationButton: {
                mapBox.getStyle(style -> enableLocation(style));
            } break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapScreen_myLocationMenuItem: {
                mapBox.getStyle(style -> enableLocation(style));
            } break;
        }
        return false;
    }

    private void showOptionsMenu(View v) {
        Context wrapper = new ContextThemeWrapper(this, R.style.Map_OptionsMenu);
        PopupMenu options = new PopupMenu(wrapper, v);
        options.setOnMenuItemClickListener(this);
        MenuInflater inflater = options.getMenuInflater();
        inflater.inflate(R.menu.mapcreen_optionsmenu, options.getMenu());
        options.show();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapBox) {
        this.mapBox = mapBox;
        mapBox.setStyle(Style.DARK);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocation(@NonNull Style loadedStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapBox.getLocationComponent();

            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissions = new PermissionsManager(this);
            permissions.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        this.permissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "You need to grant location permission to this app", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean result) {
        if (result) {
            mapBox.getStyle(style -> enableLocation(style));
        } else {
            Toast.makeText(this, "You can not see your location without granted permission", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        map.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        map.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
}