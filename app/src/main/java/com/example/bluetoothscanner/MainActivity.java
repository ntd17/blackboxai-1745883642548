package com.example.bluetoothscanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bluetoothscanner.adapter.DeviceListAdapter;
import com.example.bluetoothscanner.databinding.ActivityMainBinding;
import com.example.bluetoothscanner.model.BluetoothDeviceModel;
import com.example.bluetoothscanner.utils.BluetoothUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DeviceListAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private final List<BluetoothDeviceModel> devicesList = new ArrayList<>();
    private boolean isScanning = false;
    private final Handler scanTimeoutHandler = new Handler();
    private static final long SCAN_TIMEOUT = 15000; // 15 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Bluetooth adapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        setupRecyclerView();
        setupScanButton();
        registerBluetoothReceiver();

        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            showError(getString(R.string.error_bluetooth_unavailable));
            binding.scanButton.setEnabled(false);
            return;
        }
    }

    private void setupRecyclerView() {
        adapter = new DeviceListAdapter();
        binding.deviceList.setLayoutManager(new LinearLayoutManager(this));
        binding.deviceList.setAdapter(adapter);
    }

    private void setupScanButton() {
        binding.scanButton.setOnClickListener(v -> {
            if (!isScanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });
    }

    private void startScanning() {
        // Check permissions and Bluetooth state
        if (!BluetoothUtils.isBluetoothEnabled(this)) {
            BluetoothUtils.requestEnableBluetooth(this);
            return;
        }

        if (!BluetoothUtils.checkAndRequestPermissions(this)) {
            return;
        }

        // Clear previous results
        devicesList.clear();
        adapter.clearList();

        // Update UI
        isScanning = true;
        binding.scanButton.setText(R.string.status_scanning);
        binding.scanningProgress.setVisibility(View.VISIBLE);
        binding.statusText.setText(R.string.status_scanning);

        // Start discovery
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        // Set scan timeout
        scanTimeoutHandler.postDelayed(this::stopScanning, SCAN_TIMEOUT);
    }

    private void stopScanning() {
        if (!isScanning) return;

        isScanning = false;
        binding.scanButton.setText(R.string.scan_devices);
        binding.scanningProgress.setVisibility(View.GONE);
        
        if (devicesList.isEmpty()) {
            binding.statusText.setText(R.string.status_no_devices);
        } else {
            binding.statusText.setText(getString(R.string.status_ready));
        }

        bluetoothAdapter.cancelDiscovery();
        scanTimeoutHandler.removeCallbacksAndMessages(null);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                
                if (device != null) {
                    BluetoothDeviceModel deviceModel = new BluetoothDeviceModel(device, rssi);
                    if (!devicesList.contains(deviceModel)) {
                        devicesList.add(deviceModel);
                        adapter.submitList(new ArrayList<>(devicesList));
                    }
                }
            }
        }
    };

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
        stopScanning();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BluetoothUtils.REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                showError(getString(R.string.status_permission_required));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothUtils.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startScanning();
            } else {
                showError(getString(R.string.status_bluetooth_disabled));
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        binding.statusText.setText(message);
        binding.statusText.setTextColor(getColor(R.color.status_error));
    }
}
