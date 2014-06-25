package com.example.helloibeacon;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final static int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 10000;

    private static BluetoothAdapter mBluetoothAdapter;

    private Handler mHandler;

    private boolean mScanEnabled = true;

    private static ArrayAdapter<String> mAdapter;

    private static final List<String> macAddress = new ArrayList<String>();

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    if (!macAddress.contains(device.getAddress())) {
                        macAddress.add(device.getAddress());
                        mAdapter.add(device.getName());
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mHandler = new Handler();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mAdapter.clear();
        macAddress.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_scan) {
            mAdapter.clear();
            macAddress.clear();
            scanLeDevice(mScanEnabled);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanLeDevice(final boolean enable)
    {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanEnabled = true;
                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mScanEnabled = false;
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanEnabled = true;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
            setListAdapter(mAdapter);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);
            final ListView list = getListView();
            list.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    final String address = macAddress.get(position);
                    Toast.makeText(getActivity(), address, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
