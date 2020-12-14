package com.example.interactivemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
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
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
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
    private LineManager lineManager;
    private SymbolManager symbolManager;

    private Button optionsMenu;
    private Button gpsLocation;
    private Button openDrawer;
    private Button endLine;
    private FloatingActionButton addLineButton;
    private FloatingActionButton addPointButton;
    private FloatingActionButton addIconButton;
    private FloatingActionButton showAnnotMenu;
    private NavigationView navbar;
    private boolean fabClicked = false;
    private boolean newLine = false;
    private boolean guest = true;

    private FloatingActionButton showAnnotEdit;
    private FloatingActionButton deleteButton;
    private FloatingActionButton moveButton;
    private FloatingActionButton deleteAllButton;
    private boolean fabEditClicked = false;

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private Animation toBottomEdit;
    private Animation rotateCloseEdit;
    private Animation fromBottomEdit;
    private Animation rotateOpenEdit;

    private Intent intent;
    private SlidrInterface slidr;
    private DrawerLayout drawer;
    protected SharedPreferences preferences;
    private Member member;

    private CameraPosition position;
    private List<CircleOptions> circleOptionsList;
    private List<LatLng> actualLineCoords;
    private List<LineOptions> lineOptionsList;
    private List<SymbolOptions> symbolOptionsList;

    private static final String ICON = "point_icon";

    private boolean pointsSelected = false;
    private boolean lineSelected = false;
    private boolean iconSelected = false;
    private boolean iconMove = false;
    private boolean iconDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.map_screen);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_blue));

        SlidrConfig config = new SlidrConfig.Builder().position(SlidrPosition.BOTTOM).edge(true).edgeSize(0.12f).build();
        slidr = Slidr.attach(this, config);

        map = findViewById(R.id.map);
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);

        optionsMenu = findViewById(R.id.mapScreen_optionsMenu);
        optionsMenu.setOnClickListener(this);

        gpsLocation = findViewById(R.id.mapScreen_gpsLocationButton);
        gpsLocation.setOnClickListener(this);

        openDrawer = findViewById(R.id.mapScreen_sideNavDrawer);
        openDrawer.setOnClickListener(this);
        drawer = findViewById(R.id.map_screen);

        navbar = findViewById(R.id.mapScreen_sideNav);
        navbar.setNavigationItemSelectedListener(this);

        preferences = getPreferences(MODE_PRIVATE);

        addLineButton = findViewById(R.id.mapScreen_addLineButton);
        addPointButton = findViewById(R.id.mapScreen_addPointButton);
        addIconButton = findViewById(R.id.mapScreen_addIconButton);
        showAnnotMenu = findViewById(R.id.mapScreen_floatButton);
        endLine = findViewById(R.id.mapScreen_endLineButton);
        addLineButton.setOnClickListener(this);
        addPointButton.setOnClickListener(this);
        addIconButton.setOnClickListener(this);
        showAnnotMenu.setOnClickListener(this);

        showAnnotEdit = findViewById(R.id.mapScreen_floatButtonEdit);
        moveButton = findViewById(R.id.mapScreen_moveButton);
        deleteButton = findViewById(R.id.mapScreen_deleteButton);
        deleteAllButton = findViewById(R.id.mapScreen_deleteAll);
        showAnnotEdit.setOnClickListener(this);
        moveButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        deleteAllButton.setOnClickListener(this);

        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
        toBottomEdit = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim_edit);
        rotateCloseEdit = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim_edit);
        fromBottomEdit = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim_edit);
        rotateOpenEdit = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim_edit);

        intent = getIntent();
        if (intent.getBooleanExtra("auth_result", false)) {
            initMemberInteraction(intent);
        } else {
            initGuestInteraction();
        }
    }

    private void showOptionsMenu(View v) {
        Context wrapper = new ContextThemeWrapper(this, R.style.Map_OptionsMenu);
        PopupMenu options = new PopupMenu(wrapper, v);
        options.setOnMenuItemClickListener(this);
        MenuInflater inflater = options.getMenuInflater();
        inflater.inflate(R.menu.mapcreen_optionsmenu, options.getMenu());
        options.show();
    }

    private void initGuestInteraction() {
        Menu menu = navbar.getMenu();
        menu.findItem(R.id.modifyNav_shareMap).setVisible(false);
        menu.findItem(R.id.modifyNav_saveMap).setVisible(false);
        menu.findItem(R.id.modifyNav_loadMap).setVisible(false);

        optionsMenu.setVisibility(View.GONE);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        openDrawer.setVisibility(View.GONE);
    }

    private void initMemberInteraction(Intent intent) {
        Menu menu = navbar.getMenu();
        menu.findItem(R.id.modifyNav_shareMap).setVisible(true);
        menu.findItem(R.id.modifyNav_saveMap).setVisible(true);
        menu.findItem(R.id.modifyNav_loadMap).setVisible(true);
        guest = false;

        MenuItem userName = menu.findItem(R.id.mapScreen_userName);
        MenuItem userFocus = menu.findItem(R.id.mapScreen_userFocus);
        MenuItem userId = menu.findItem(R.id.mapScreen_userId);

        optionsMenu.setVisibility(View.VISIBLE);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        openDrawer.setVisibility(View.VISIBLE);
        Gson gson = new Gson();
        String user = intent.getStringExtra("user");
        member = new Member();

        try {
            member = gson.fromJson(user, Member.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        userName.setTitle(member.getName());
        userFocus.setTitle(member.getFocus());
        userId.setTitle(member.getId().toString());
        navbar.setNavigationItemSelectedListener(this);
    }

    private void initCamera() {
        Gson gson = new Gson();
        String jsonPosition = preferences.getString("position", null);
        if (jsonPosition == null) {
            position = new CameraPosition.Builder()
                    .target(new LatLng(51.50550, -0.07520))
                    .zoom(12)
                    .build();
        } else {
            try {
                position = gson.fromJson(jsonPosition, CameraPosition.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void initMapPoint(CircleManager circleManager) {
        Gson gson = new Gson();
        String json = preferences.getString("points", "");
        try {
            circleOptionsList = gson.fromJson(json, new TypeToken<List<CircleOptions>>(){}.getType());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (circleOptionsList != null) {
            circleManager.create(circleOptionsList);
        }
    }

    private void initMapLine(LineManager lineManager) {
        Gson gson = new Gson();
        String json = preferences.getString("lines", "");
        try {
            lineOptionsList = gson.fromJson(json, new TypeToken<List<LineOptions>>(){}.getType());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (lineOptionsList != null) {
            lineManager.create(lineOptionsList);
        }
    }

    private void initMapSymbols(SymbolManager symbolManager) {
        Gson gson = new Gson();
        String json = preferences.getString("symbols", "");
        try {
            symbolOptionsList = gson.fromJson(json, new TypeToken<List<SymbolOptions>>(){}.getType());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (symbolOptionsList != null) {
            symbolManager.create(symbolOptionsList);
        }
    }

    private void showAnnotButtons() {
        setAnimation(fabClicked);
        setClickable(fabClicked);
        fabClicked = !fabClicked;
    }

    private void showAnnotEdit() {
        setAnimationEdit(fabEditClicked);
        setClickableEdit(fabEditClicked);
        fabEditClicked = !fabEditClicked;
    }

    private void setClickable(boolean fabClicked) {
        if (!fabClicked) {
            addPointButton.setClickable(true);
            addIconButton.setClickable(true);
            addLineButton.setClickable(true);
        } else {
            addPointButton.setClickable(false);
            addIconButton.setClickable(false);
            addLineButton.setClickable(false);
        }
    }

    private void setAnimation(boolean fabClicked) {
        if (!fabClicked) {
            addLineButton.startAnimation(fromBottom);
            addIconButton.startAnimation(fromBottom);
            addPointButton.startAnimation(fromBottom);
            showAnnotMenu.startAnimation(rotateOpen);
        } else {
            addLineButton.startAnimation(toBottom);
            addIconButton.startAnimation(toBottom);
            addPointButton.startAnimation(toBottom);
            showAnnotMenu.startAnimation(rotateClose);
        }
    }

    private void setAnimationEdit(boolean fabClicked) {
        if (!fabClicked) {
            moveButton.startAnimation(fromBottomEdit);
            deleteButton.startAnimation(fromBottomEdit);
            deleteAllButton.startAnimation(fromBottomEdit);
            showAnnotEdit.startAnimation(rotateOpenEdit);
        } else {
            moveButton.startAnimation(toBottomEdit);
            deleteButton.startAnimation(toBottomEdit);
            deleteAllButton.startAnimation(toBottomEdit);
            showAnnotEdit.startAnimation(rotateCloseEdit);
        }
    }

    private void setClickableEdit(boolean fabClicked) {
        if (!fabClicked) {
            moveButton.setClickable(true);
            deleteButton.setClickable(true);
            deleteAllButton.setClickable(true);
        } else {
            moveButton.setClickable(false);
            deleteButton.setClickable(false);
            deleteAllButton.setClickable(false);
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
                mapBox.getStyle(style -> {
                    enableLocation(style);
                });
            } break;
            case R.id.mapScreen_addIconButton: {
                pointsSelected = false;
                lineSelected = false;
                iconSelected = !iconDelete;
                iconMove = false;
                iconDelete = false;
            } break;
            case R.id.mapScreen_addPointButton: {
                pointsSelected = !pointsSelected;
                lineSelected = false;
                iconSelected = false;
                iconMove = false;
                iconDelete = false;
            } break;
            case R.id.mapScreen_addLineButton: {
                pointsSelected = false;
                lineSelected = !lineSelected;
                iconSelected = false;
                iconMove = false;
                iconDelete = false;
            } break;
            case R.id.mapScreen_floatButton: {
                pointsSelected = false;
                lineSelected = false;
                iconSelected = false;
                showAnnotButtons();
            } break;
            case R.id.mapScreen_deleteButton: {
                pointsSelected = false;
                lineSelected = false;
                iconSelected = false;
                iconMove = false;
                iconDelete = !iconDelete;
            } break;
            case R.id.mapScreen_moveButton: {
                pointsSelected = false;
                lineSelected = false;
                iconSelected = false;
                iconMove = !iconMove;
                iconDelete = false;
            } break;
            case R.id.mapScreen_deleteAll: {
                circleManager.deleteAll();
                circleOptionsList.clear();
                lineManager.deleteAll();
                lineOptionsList.clear();
                symbolManager.deleteAll();
                symbolOptionsList.clear();
                SharedPreferences.Editor prefEditor = preferences.edit();
                prefEditor.remove("points");
                prefEditor.remove("lines");
                prefEditor.remove("symbols");
                prefEditor.commit();
            } break;
            case R.id.mapScreen_floatButtonEdit: {
                iconMove = false;
                iconDelete = false;
                showAnnotEdit();
            } break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

        }
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapScreen_logOutButton: {
                finish();
            } break;
        }
        return false;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapBox) {
        this.mapBox = mapBox;
        mapBox.setStyle(Style.DARK, style -> {
            circleManager = new CircleManager(map, mapBox, style);
            circleManager.addLongClickListener(circle -> {
                if (iconDelete) {
                    circleManager.delete(circle);
                }
                if (iconMove) {
                    circle.setDraggable(true);
                } else {
                    circle.setDraggable(false);
                }
                return false;
            });
            initMapPoint(circleManager);

            lineManager = new LineManager(map, mapBox, style);
            lineManager.addLongClickListener(line -> {
                if (iconDelete) {
                    lineManager.delete(line);
                }
                if (iconMove) {
                    line.setDraggable(true);
                } else {
                    line.setDraggable(false);
                }
                return false;
            });
            initMapLine(lineManager);

            style.addImage(ICON,
                    BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_location_mod)),
                    true);

            GeoJsonOptions geoJsonOptions = new GeoJsonOptions().withTolerance(0.4f);
            symbolManager = new SymbolManager(map, mapBox, style, null, geoJsonOptions);
            symbolManager.addLongClickListener(symbol -> {
                if (iconDelete) {
                    symbolManager.delete(symbol);
                }
                if (iconMove) {
                    symbol.setDraggable(true);
                } else {
                    symbol.setDraggable(false);
                }
                return false;
            });
            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);
            initMapSymbols(symbolManager);
        });

        initCamera();
        mapBox.addOnCameraMoveListener(() -> position = mapBox.getCameraPosition());
        mapBox.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        mapBox.addOnMapClickListener(point -> {
            if (circleOptionsList == null) {
                circleOptionsList = new ArrayList<>();
            }
            if (actualLineCoords == null) {
                actualLineCoords = new ArrayList<>();
            }
            if (lineOptionsList == null) {
                lineOptionsList = new ArrayList<>();
            }
            if (symbolOptionsList == null) {
                symbolOptionsList = new ArrayList<>();
            }
            if (pointsSelected) {
                CircleOptions circleOptions = new CircleOptions()
                        .withLatLng(point)
                        .withCircleColor(ColorUtils.colorToRgbaString(Color.YELLOW))
                        .withCircleRadius(12f)
                        .withDraggable(false);
                circleManager.create(circleOptions);
                circleOptionsList.add(circleOptions);
            } else if (lineSelected) {
                actualLineCoords.add(point);
                if (actualLineCoords.size() >= 2) {
                    if (!newLine) {
                        if (lineManager.getAnnotations().size() >= 1) {
                            LongSparseArray lines = lineManager.getAnnotations();
                            long key = lines.keyAt(lines.size() - 1);
                            Line lastLine = lineManager.getAnnotations().get(key);
                            lineManager.delete(lastLine);
                        }
                    }
                    LineOptions lineOptions = new LineOptions()
                            .withLatLngs(actualLineCoords)
                            .withLineColor(ColorUtils.colorToRgbaString(Color.RED))
                            .withLineWidth(6.0f);
                    lineManager.create(lineOptions);
                    newLine = false;
                    endLine.setClickable(true);
                    endLine.setVisibility(View.VISIBLE);
                    endLine.setOnClickListener(v -> {
                        endLine.setVisibility(View.INVISIBLE);
                        endLine.setClickable(false);
                        actualLineCoords.clear();
                        newLine = true;
                        lineOptionsList.add(lineOptions);
                    });
                }
            } else if (iconSelected) {
                SymbolOptions symbolOptions = new SymbolOptions()
                        .withLatLng(point)
                        .withIconImage(ICON)
                        .withIconSize(1.3f)
                        .withIconColor("green")
                        .withSymbolSortKey(10.0f)
                        .withDraggable(false);
                symbolManager.create(symbolOptions);
                symbolOptionsList.add(symbolOptions);
            }
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
        String jsonPoints = gson.toJson(circleOptionsList);
        prefEditor.putString("points", jsonPoints);
        String jsonLines = gson.toJson(lineOptionsList);
        prefEditor.putString("lines", jsonLines);
        String jsonSymbols = gson.toJson(symbolOptionsList);
        prefEditor.putString("symbols", jsonSymbols);
        String jsonPosition = gson.toJson(position);
        prefEditor.putString("position", jsonPosition);
        prefEditor.commit();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (guest) {
            super.onBackPressed();
        }
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