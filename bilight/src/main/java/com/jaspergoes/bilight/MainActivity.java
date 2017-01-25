package com.jaspergoes.bilight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.jaspergoes.bilight.helpers.Constants;
import com.jaspergoes.bilight.helpers.PreferenceActivityCompat;
import com.jaspergoes.bilight.helpers.PreferenceCategoryCompat;
import com.jaspergoes.bilight.helpers.PreferenceCompat;
import com.jaspergoes.bilight.milight.Controller;
import com.jaspergoes.bilight.milight.objects.Device;

public class MainActivity extends PreferenceActivityCompat {

    private class DevicePreference extends PreferenceCompat {

        public DevicePreference(Context context, String title, String summary) {
            super(context);
            this.setTitle(title);
            this.setSummary(summary);
        }

        @Override
        protected void onClick() {

            if (this.isEnabled() && !Controller.isConnecting) {

                final String addressIP = (String) this.getTitle();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Controller.INSTANCE.setDevice(addressIP, getApplicationContext());
                    }
                }).start();

            }

        }

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (Constants.BILIGHT_DISCOVERED_DEVICES_CHANGED.equals(action)) {

                updateList();

            } else if (Constants.BILIGHT_DEVICE_CONNECTED.equals(action)) {

                startActivity(new Intent(context, ControlActivity.class));

            }

        }

    };

    private PreferenceCategoryCompat deviceList;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);

        new Controller();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Controller.INSTANCE.discoverNetworks(getApplicationContext());
            }
        }).start();

        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        // Networks Category
        deviceList = new PreferenceCategoryCompat(this);
        root.addPreference(deviceList);

        setPreferenceScreen(root);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

    }

    @Override
    protected void onResume() {

        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BILIGHT_DISCOVERED_DEVICES_CHANGED);
        filter.addAction(Constants.BILIGHT_DEVICE_CONNECTED);
        registerReceiver(receiver, filter);

        updateList();

    }

    @Override
    protected void onPause() {

        super.onPause();

        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
        }

    }

    void updateList() {

        /* Remove all discovered devices from the PreferenceGroup */
        deviceList.removeAll();

        deviceList.setTitle(Controller.networkInterfaceName);

        boolean empty = true;

		/* Re-populate the list with discovered devices */
        for (Device device : Controller.milightDevices) {
            DevicePreference pref = new DevicePreference(this, device.addrIP, device.addrMAC);
            pref.setIcon(getResources().getDrawable(R.drawable.ic_bulb));
            deviceList.addPreference(pref);
            empty = false;
        }

        if (empty) {
            PreferenceCompat pref = new PreferenceCompat(this);
            pref.setTitle(getString(R.string.no_milight_devices_found_yet));
            pref.setIcon(getResources().getDrawable(R.drawable.ic_bulb_grey));
            pref.setEnabled(false);
            deviceList.addPreference(pref);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_help:
                //new MaterialDialog.Builder(MainActivity.this).title(getString(R.string.screen_wifi_pause)).content(getString(R.string.wifi_preferences_help_dialog)).positiveText(R.string.word_ok).show();
                return true;
        }
        return false;
    }
}