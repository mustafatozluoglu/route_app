package prj_2.stu_1737879;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;


public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback, View.OnClickListener {

    private FetchURL fetchURL;

    private Button bt_listView;
    private ListView entries_listView;
    private List<String> entries_list;

    GoogleMap map;

    private String startAddress;
    private String destinationAddress;

    private LatLng startCoordinate;
    private LatLng destinationCoordinate;

    private MarkerOptions startLocationMarker;
    private MarkerOptions destinationLocationMarker;

    private Polyline currentPolyline;

    MapFragment mapFragment;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        bt_listView = (Button) findViewById(R.id.bt_listView);
        bt_listView.setOnClickListener(this);

        entries_listView = (ListView) findViewById(R.id.entries_listView);
        entries_listView.setVisibility(View.INVISIBLE);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();


        Intent intent = getIntent();
        startAddress = intent.getStringExtra("startAddress");
        destinationAddress = intent.getStringExtra("destinationAddress");
        Log.d("Start Address:", startAddress);
        Log.d("Destination Address:", destinationAddress);


        startCoordinate = findCoordinateGivenAddress(startAddress);
        destinationCoordinate = findCoordinateGivenAddress(destinationAddress);

    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(NavigationActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions myLocationMarker = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("You are here.");
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        map.addMarker(myLocationMarker);

        startLocationMarker = new MarkerOptions()
                .position(startCoordinate)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title("Start Address");
        map.addMarker(startLocationMarker);

        destinationLocationMarker = new MarkerOptions()
                .position(destinationCoordinate)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title("Destination Address");
        map.addMarker(destinationLocationMarker);


        String url = getUrl(startCoordinate, destinationCoordinate, "driving");
        fetchURL = (FetchURL) new FetchURL(NavigationActivity.this).execute(url, "driving");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                }
                break;
        }
    }

    public LatLng findCoordinateGivenAddress(String address) {
        double latitude = 0;
        double longitude = 0;

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            latitude = addresses.get(0).getLatitude();
            longitude = addresses.get(0).getLongitude();
        }

        LatLng coordinate = new LatLng(latitude, longitude);

        return coordinate;
    }

    private String getUrl(LatLng start, LatLng destination, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + start.latitude + "," + start.longitude;
        // Destination of route
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.map_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = map.addPolyline((PolylineOptions) values[0]);

        entries_list = fetchURL.getEntries_list();
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, entries_list);
        entries_listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_listView:
                if (mapFragment.isVisible()) {
                    mapFragment.getView().setVisibility(View.INVISIBLE);
                    entries_listView.setVisibility(View.VISIBLE);
                    bt_listView.setText("Show Map");
                } else {
                    mapFragment.getView().setVisibility(View.VISIBLE);
                    entries_listView.setVisibility(View.INVISIBLE);
                    bt_listView.setText("Show Entries List View");
                }

                break;
        }
    }
}