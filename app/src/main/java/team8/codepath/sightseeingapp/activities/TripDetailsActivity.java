package team8.codepath.sightseeingapp.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import team8.codepath.sightseeingapp.R;
import team8.codepath.sightseeingapp.adapters.PlacesArrayAdapter;
import team8.codepath.sightseeingapp.models.PlaceModel;
import team8.codepath.sightseeingapp.models.TripModel;

public class TripDetailsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    String name;
    String distance;
    String time;
    @BindView(R.id.tvPlacesNumber)
    TextView tvPlacesNumber;

    private ArrayList<String> places;
    private PlacesArrayAdapter aPlaces;
    private ListView lvPlaces;
    private GoogleMap mMap;

    public static final String TAG = "PLACES API";
    protected GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        TripModel trip = (TripModel) Parcels.unwrap(getIntent().getParcelableExtra("trip"));
        name = trip.getName();
        distance = trip.getDistance();
        time = trip.getHumanReadableTotalLength();

        TextView tripName = (TextView) findViewById(R.id.tvTitle);
        tripName.setText(name);

        TextView tripDistance = (TextView) findViewById(R.id.tvDistance);
        tripDistance.setText(distance);

        TextView tripTime = (TextView) findViewById(R.id.tvTime);
        tripTime.setText("Lenght: " + time);

        //ImageView tripImage = (ImageView) findViewById(R.id.ivMap);
        //Picasso.with(getApplicationContext()).load(trip.getBannerPhoto()).into(tripImage);

        lvPlaces = (ListView) findViewById(R.id.lvPlaces);

        final DatabaseReference dataPlaces = FirebaseDatabase.getInstance().getReference("places").child(trip.getId().toString());

        final FirebaseListAdapter<PlaceModel> mAdapter = new FirebaseListAdapter<PlaceModel>(this, PlaceModel.class, R.layout.item_place, dataPlaces) {
            @Override
            protected void populateView(View view, final PlaceModel place, int position) {
                TextView tvPlaceName = (TextView) view.findViewById(R.id.tvPlaceName);
                tvPlaceName.setText(place.getName());
                Log.d("DEBUG", place.getPlaceId());

                Places.GeoDataApi.getPlaceById(mGoogleApiClient, place.getPlaceId())
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {

                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                    final Place myPlace = places.get(0);
                                    Log.i(TAG, "Place found: " + myPlace.getName());

                                    LatLng placeLatLng = myPlace.getLatLng();
                                    mMap.addMarker(new MarkerOptions()
                                            .position(placeLatLng)
                                            .title(myPlace.getName().toString()));

                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));


                                } else {
                                    Log.e(TAG, "Place not found");
                                }
                                places.release();
                            }
                        });
            }
        };

        dataPlaces.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long numChildren = dataSnapshot.getChildrenCount();
                tvPlacesNumber.setText(String.valueOf(numChildren) + " Places");


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        lvPlaces.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 14.0f ) );*/

    }

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
