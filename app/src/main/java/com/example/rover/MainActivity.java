package com.example.rover;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Stack;

import io.rover.ExperienceActivity;
import io.rover.GoogleApiConnection;
import io.rover.RemoteScreenActivity;
import io.rover.RoverObserver;
import io.rover.model.Action;
import io.rover.model.Alignment;
import io.rover.model.Appearance;
import io.rover.model.Block;
import io.rover.model.ButtonBlock;
import io.rover.model.Font;
import io.rover.model.Image;
import io.rover.model.ImageBlock;
import io.rover.model.Message;
import io.rover.Rover;
import io.rover.model.Offset;
import io.rover.model.PercentageUnit;
import io.rover.model.PointsUnit;
import io.rover.model.Row;
import io.rover.model.Screen;
import io.rover.model.TextBlock;

public class MainActivity extends AppCompatActivity implements RegionFragment.OnRegionFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    public void onUpdateLocationClicked(View view) {
        //Rover.updateLocation();
        GoogleApiConnection connection = new GoogleApiConnection(getApplicationContext());

        final LocationRequest locationRequest = new LocationRequest()
                .setInterval(1)
                .setFastestInterval(1)
                .setSmallestDisplacement(0)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        final Context context = getApplicationContext();

        connection.setCallbacks(new GoogleApiConnection.Callbacks() {
            @Override
            public int onConnected(final GoogleApiClient client) {

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            client.disconnect();
                            //Rover.updateLocation(location);
                        }
                    });
                }
                return GoogleApiConnection.KEEP_ALIVE;
            }
        });
        connection.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Switch monitoringSwitch = (Switch)menu.findItem(R.id.action_monitoring).getActionView().findViewById(R.id.monitoringSwitch);
        if (monitoringSwitch != null) {
            //monitoringSwitch.setChecked(Rover.isMonitoring());
            monitoringSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        startMonitoring();
                    } else {
                        Rover.stopMonitoring();
                    }
                }
            });
        }
        return true;
    }



    private void startMonitoring() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            } else {
                Rover.startMonitoring();
            }
        } else {
            // Pre-Marshmallow
            Rover.startMonitoring();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Rover.startMonitoring();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onRegionFragmentEnterClick(String id) {
        Rover.simulateGeofenceEnter(id);
    }

    @Override
    public void onRegionFragmentExitClick(String id) {
        Rover.simulateGeofenceExit(id);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return MessageFragment.newInstance(1);
                case 0:
                    return RegionFragment.newInstance(1);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Monitoring";
                case 1:
                    return "Inbox";
            }
            return null;
        }
    }
}
