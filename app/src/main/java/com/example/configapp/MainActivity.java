package com.example.configapp; // <-- change to your own package name

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;

    // Emergency contacts (UK numbers - change for your country)
    private final String[] emergencyNames = {
            "Ambulance", "Police", "Fire Brigade", "NHS Non-Emergency (111)"
    };
    private final String[] emergencyNumbers = {
            "999", "999", "999", "111"
    };

    // Service / activity list
    private final String[] services = {
            "Appointment", "Medication", "Doctor Visit", "Pharmacy", "Transport", "Meal Support"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // IDs must match the ones in activity_main.xml
        Button buttonSOS = findViewById(R.id.buttonSOS);
        Button buttonFindServices = findViewById(R.id.buttonFindServices);
        Button buttonLocation = findViewById(R.id.buttonLocation);
        Button buttonContact = findViewById(R.id.buttonContact);

        buttonSOS.setOnClickListener(v -> showEmergencyContacts());
        buttonFindServices.setOnClickListener(v -> showServicesList());

        buttonLocation.setOnClickListener(v -> openGoogleMaps());
        buttonContact.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ContactActivity.class)));
    }

    // ---------------- SOS: emergency contacts ----------------
    private void showEmergencyContacts() {
        new AlertDialog.Builder(this)
                .setTitle("Emergency Support")
                .setItems(emergencyNames, (dialog, which) -> {
                    // ACTION_DIAL opens the dialer with the number filled in (no permission needed)
                    Intent dial = new Intent(Intent.ACTION_DIAL);
                    dial.setData(Uri.parse("tel:" + emergencyNumbers[which]));
                    startActivity(dial);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------- Find Services: list of activities ----------------
    private void showServicesList() {
        new AlertDialog.Builder(this)
                .setTitle("Find Services")
                .setItems(services, (dialog, which) -> {
                    // Replace this Toast with startActivity(...) to open a real screen
                    Toast.makeText(this, services[which] + " selected", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    // ---------------- Location: show map ----------------
    private void openGoogleMaps() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/"));
        intent.setPackage("com.google.android.apps.maps"); // force the Google Maps app
        try {
            startActivity(intent);
        } catch (Exception e) {
            // open google Maps in a browser because it is not installed
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/")));
        }
    }

    private void showCurrentLocationOnMap() {
        Uri mapUri;
        Location best = null;

        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            for (String provider : lm.getProviders(true)) {
                Location l = lm.getLastKnownLocation(provider);
                if (l != null && (best == null || l.getAccuracy() < best.getAccuracy())) {
                    best = l;
                }
            }
        } catch (SecurityException ignored) { }

        if (best != null) {
            mapUri = Uri.parse("geo:" + best.getLatitude() + "," + best.getLongitude()
                    + "?q=" + best.getLatitude() + "," + best.getLongitude() + "(You are here)");
        } else {
            // No location yet (common on emulator) - show nearby hospitals instead
            mapUri = Uri.parse("geo:0,0?q=hospitals near me");
            Toast.makeText(this, "Location unavailable, showing nearby hospitals",
                    Toast.LENGTH_SHORT).show();
        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            // No maps app installed - open in the browser
            Intent web = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=hospitals+near+me"));
            startActivity(web);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocationOnMap();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}