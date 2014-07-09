package com.example.androidconparis;

import com.example.androidconparis.ScanListFragment.OnScanListListener;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

/**
 * 
 * @author sylvek
 * 
 */
public class MainActivity extends Activity implements OnScanListListener {

    private final static int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private BluetoothAdapter mBluetoothAdapter;

    private Handler mHandler;

    private LeScanCallback mLeScanCallback = new LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            ScanListFragment f = (ScanListFragment) getFragmentManager().findFragmentById(R.id.container);
            if (f != null) {
                f.addScannedDevice(device.getAddress(), device.getName());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Create activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // detect Bluetooth LE support
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Attach fragment
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, ScanListFragment.instance()).commit();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mHandler = new Handler();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // detect Bluetooth enabled
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
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
                    onScanChangeStatus(false);
                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
            onScanChangeStatus(true);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            onScanChangeStatus(false);
        }
    }

    private void onScanChangeStatus(boolean b)
    {
        ScanListFragment f = (ScanListFragment) getFragmentManager().findFragmentById(R.id.container);
        if (f != null) {
            f.scanChangeStatus(b);
        }
    }

    @Override
    public void onScanStart()
    {
        scanLeDevice(true);
    }

    @Override
    public void onScanStop()
    {
        scanLeDevice(false);
    }

    @Override
    public void onDisplayDevice(String address)
    {
        Intent intent = new Intent(this, DeviceActivity.class);
        intent.putExtra(DeviceActivity.ADDRESS, address);
        startActivity(intent);
    }
}
