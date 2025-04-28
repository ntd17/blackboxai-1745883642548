package com.example.bluetoothscanner.model;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceModel {
    private final BluetoothDevice device;
    private int rssi;
    private boolean isPaired;

    public enum SignalStrength {
        HIGH,
        MEDIUM,
        LOW
    }

    public BluetoothDeviceModel(BluetoothDevice device, int rssi) {
        this.device = device;
        this.rssi = rssi;
        this.isPaired = device.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    public String getName() {
        String name = device.getName();
        return name != null ? name : "Unknown Device";
    }

    public String getAddress() {
        return device.getAddress();
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public boolean isPaired() {
        return isPaired;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    // Get signal strength category based on RSSI value
    public SignalStrength getSignalStrength() {
        if (rssi >= -60) return SignalStrength.HIGH;
        else if (rssi >= -70) return SignalStrength.MEDIUM;
        else return SignalStrength.LOW;
    }

    // Format RSSI value for display
    public String getFormattedRssi() {
        return rssi + " dBm";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BluetoothDeviceModel that = (BluetoothDeviceModel) o;
        return device.getAddress().equals(that.device.getAddress());
    }

    @Override
    public int hashCode() {
        return device.getAddress().hashCode();
    }
}
