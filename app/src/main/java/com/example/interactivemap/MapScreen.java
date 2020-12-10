package com.example.interactivemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener,
        View.OnClickListener, PopupMenu.OnMenuItemClickListener, NavigationView.OnNavigationItemSelectedListener {

    private PermissionsManager permissions;
    protected MapboxMap mapBox;
    private MapView map;
    private CircleManager circleManager;

    private Button optionsMenu;
    private Button gpsLocation;
    private Button openDrawer;
    private NavigationView navbar;

    private Intent intent;
    private SlidrInterface slidr;
    private DrawerLayout drawer;
    protected SharedPreferences preferences;

    private static CameraPosition position = new CameraPosition.Builder()
            .target(new LatLng(51.50550, -0.07520))
            .zoom(12)
            .build();
    private static boolean guest = true;
    private static List<CircleOptions> circleOptionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.map_screen);

        SlidrConfig config = new SlidrConfig.Builder().position(SlidrPosition.BOTTOM).edge(true).edgeSize(0.12f).build();
        slidr = Slidr.attach(this, config);

        map = findViewById(R.id.map);
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);

        optionsMenu = findViewById(R.id.mapScreen_optionsMenu);
        optionsMenu.setOnClickListener(this);
        if (guest) {
            optionsMenu.setVisibility(View.GONE);
        }

        gpsLocation = findViewById(R.id.mapScreen_gpsLocationButton);
        gpsLocation.setOnClickListener(this);

        openDrawer = findViewById(R.id.mapScreen_sideNavDrawer);
        openDrawer.setOnClickListener(this);
        drawer = findViewById(R.id.map_screen);

        navbar = findViewById(R.id.mapScreen_sideNav);
        navbar.setNavigationItemSelectedListener(this);

        preferences = getPreferences(MODE_PRIVATE);
    }

    private void showOptionsMenu(View v) {
        Context wrapper = new ContextThemeWrapper(this, R.style.Map_OptionsMenu);
        PopupMenu options = new PopupMenu(wrapper, v);
        options.setOnMenuItemClickListener(this);
        MenuInflater inflater = options.getMenuInflater();
        inflater.inflate(R.menu.mapcreen_optionsmenu, options.getMenu());
        options.show();
    }

    private void initPersistanceStorage(CircleManager circleManager) {
        Gson gson = new Gson();
        String json = preferences.getString("points", "");
        circleOptionsList = gson.fromJson(json, new TypeToken<List<CircleOptions>>(){}.getType());
        if (circleOptionsList != null) {
            circleManager.create(circleOptionsList);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mapScreen_optionsMenu: {
                showOptionsMenu(v);
            } break;
            case R.id.mapScreen_sideNavDrawer: {
                drawer.openDrawer(Gravity.LEFT);
            } break;
            case R.id.mapScreen_gpsLocationButton: {
                mapBox.getStyle(style -> enableLocation(style));
            } break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.modifyNav_addPoint: {

            } break;
            case R.id.modifyNav_addLine: {

            } break;
            case R.id.modifyNav_addIcon: {

            } break;
        }
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapScreen_logOutButton: {

            } break;
        }
        return false;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapBox) {
        this.mapBox = mapBox;
        mapBox.setStyle(Style.DARK, style -> {
            circleManager = new CircleManager(map, mapBox, style);
            circleManager.addClickListener(circle -> {
                Toast.makeText(MapScreen.this,
                        String.format("Circle clicked %s", circle.getId()),
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            });
            circleManager.addLongClickListener(circle -> {
                Toast.makeText(MapScreen.this,
                        String.format("Circle long clicked %s", circle.getId()),
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            });
            initPersistanceStorage(circleManager);
        });

        mapBox.addOnCameraMoveListener(() -> {
            position = mapBox.getCameraPosition();
        });

        mapBox.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        mapBox.addOnMapClickListener(point -> {
            if (circleOptionsList == null) {
                circleOptionsList = new ArrayList<>();
            }
            CircleOptions circleOptions = new CircleOptions()
                    .withLatLng(point)
                    .withCircleColor(ColorUtils.colorToRgbaString(Color.YELLOW))
                    .withCircleRadius(12f)
                    .withDraggable(true);
            circleManager.create(circleOptions);
            circleOptionsList.add(circleOptions);
            return true;
        });
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
        SharedPreferences.Editor prefEditor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(circleOptionsList);
        prefEditor.putString("points", json);
        prefEditor.commit();
        super.finish();
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }

    @Override
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }
}