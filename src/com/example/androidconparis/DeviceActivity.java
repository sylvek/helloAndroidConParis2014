package com.example.androidconparis;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;

public class DeviceActivity extends Activity {

    private static final UUID DESCRIPTOR_TEMP_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final UUID TEMP_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff4");

    private static final UUID UNIT_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff1");

    private static final UUID SERVICE_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff0");

    public static final String ADDRESS = "address";

    private DeviceFragment fragment;

    private BluetoothGatt bluetoothGatt;

    private BluetoothGattCharacteristic temperature, unit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        if (savedInstanceState == null) {
            String address = getIntent().getStringExtra(ADDRESS);
            this.fragment = DeviceFragment.instance(address);
            this.getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
            this.setTitle(address);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        String address = getIntent().getStringExtra(ADDRESS);
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        this.bluetoothGatt = device.connectGatt(this, true, new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState)
            {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status)
            {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    unit = service.getCharacteristic(UNIT_UUID);
                    temperature = service.getCharacteristic(TEMP_UUID);
                    gatt.readCharacteristic(unit);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status)
            {
                if (characteristic.getUuid().equals(UNIT_UUID)) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run()
                        {
                            fragment.setUnit(characteristic.getStringValue(0));
                            listenTemperature(true);
                        }
                    });
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
            {
                if (characteristic.getUuid().equals(TEMP_UUID)) {
                    final float temperature = ByteBuffer.wrap(characteristic.getValue())
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .getFloat();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run()
                        {
                            fragment.setTemperature(String.valueOf(temperature));
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (this.bluetoothGatt != null) {
            if (this.temperature != null) {
                this.listenTemperature(false);
            }

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run()
                {
                    bluetoothGatt.disconnect();
                }
            }, 2000L);
        }
    }

    /**
     * @param gatt
     * @param characteristic
     */
    private void listenTemperature(final boolean listen)
    {
        if (this.bluetoothGatt.setCharacteristicNotification(temperature, listen)) {
            BluetoothGattDescriptor desc = temperature.getDescriptor(DESCRIPTOR_TEMP_UUID);

            if (listen) {
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                desc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }

            this.bluetoothGatt.writeDescriptor(desc);
        }
    }
}
