package com.example.bluetoothscanner.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class BluetoothUtils {
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_PERMISSIONS = 2;

    // Check if Bluetooth is supported and enabled
    public static boolean isBluetoothEnabled(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    // Request to enable Bluetooth
    public static void requestEnableBluetooth(Activity activity) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    // Check and request required permissions
    public static boolean checkAndRequestPermissions(Activity activity) {
        List<String> permissions = new ArrayList<>();

        // Add basic Bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            permissions.add(Manifest.permission.BLUETOOTH);
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        // Add location permissions (required for Bluetooth scanning)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toArray(new String[0]),
                REQUEST_PERMISSIONS
            );
            return false;
        }
        return true;
    }

    // Format RSSI value to signal strength description
    public static String getRssiDescription(int rssi) {
        if (rssi >= -60) return "Excellent";
        else if (rssi >= -70) return "Good";
        else if (rssi >= -80) return "Fair";
        else return "Poor";
    }

    // Get resource ID for signal strength icon based on RSSI value
    public static int getSignalStrengthIcon(int rssi) {
        if (rssi >= -60) return android.R.drawable.ic_signal_wifi_4_bar;
        else if (rssi >= -70) return android.R.drawable.ic_signal_wifi_3_bar;
        else if (rssi >= -80) return android.R.drawable.ic_signal_wifi_2_bar;
        else return android.R.drawable.ic_signal_wifi_1_bar;
    }
}
