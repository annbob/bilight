package com.jaspergoes.bilight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jaspergoes.bilight.helpers.Constants;
import com.jaspergoes.bilight.milight.Controller;
import com.jaspergoes.bilight.milight.objects.Device;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DeviceListAdapter adapter;

    private class DeviceListAdapter extends ArrayAdapter<Device> {

        private ArrayList<Device> mDevices;

        private class DeviceListViewHolder {

            final TextView address;
            final TextView macaddress;

            DeviceListViewHolder(TextView name, TextView time) {

                this.address = name;
                this.macaddress = time;

            }

        }

        DeviceListAdapter(Context context, int resource, ArrayList<Device> objects) {
            super(context, resource, objects);
            mDevices = objects;
        }

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Device getItem(int position) {
            return mDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0L;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DeviceListViewHolder vh;

            if (convertView == null) {

                convertView = getLayoutInflater().inflate(R.layout.device_list_item, parent, false);

                vh = new DeviceListViewHolder((TextView) convertView.findViewById(R.id.text_adr), (TextView) convertView.findViewById(R.id.text_mac));

                convertView.setTag(vh);

            } else {

                vh = (DeviceListViewHolder) convertView.getTag();

            }

            Device device = mDevices.get(position);

            vh.address.setText(device.addrIP);
            vh.macaddress.setText(device.addrMAC);

            return convertView;

        }

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (Constants.BILIGHT_DISCOVERED_DEVICES_CHANGED.equals(action)) {

                adapter.notifyDataSetChanged();

            } else if (Constants.BILIGHT_DEVICE_CONNECTED.equals(action)) {

                startActivity(new Intent(context, ControlActivity.class));

            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter((adapter = new DeviceListAdapter(getApplicationContext(), 0, Controller.milightDevices)));
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!Controller.isConnecting) {
                    final Device selected = (Device) listView.getItemAtPosition(position);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Controller.INSTANCE.setDevice(selected.addrIP, getApplicationContext());
                        }
                    }).start();
                }
            }
        });

    }

    @Override
    protected void onResume() {

        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BILIGHT_DISCOVERED_DEVICES_CHANGED);
        filter.addAction(Constants.BILIGHT_DEVICE_CONNECTED);
        registerReceiver(receiver, filter);

        /* Start discovery */
        final Context mContext = getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Controller(mContext);
            }
        }).start();

    }

    @Override
    protected void onPause() {

        super.onPause();

        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
        }

    }

}