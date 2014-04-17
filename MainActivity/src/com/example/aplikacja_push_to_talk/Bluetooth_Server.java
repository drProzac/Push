package com.example.aplikacja_push_to_talk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class Bluetooth_Server extends Thread {

	private final static String TAG = "Bluetooth_Server";
	private BluetoothServerSocket mServerSocket;
	private BluetoothSocket mSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Handler mmHandler;
	private boolean polaczone;
	public Bluetooth_Server bluetooth_Server;

	public ByteBuffer tempByte;

	public Bluetooth_Server(UUID myUUID, BluetoothAdapter mBluetoothAdapter,
			Handler mHandler) {
		// TODO Auto-generated constructor stub
		mmHandler = mHandler;
		this.mBluetoothAdapter = mBluetoothAdapter;
		BluetoothServerSocket tmp = null;
		;
		try {
			mServerSocket = mBluetoothAdapter
					.listenUsingRfcommWithServiceRecord(
							"MainBluetoothActivity", myUUID);
		} catch (IOException e) {
			Log.d(TAG, "problem z utworzeniem socketa" + e);
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			Log.d(TAG, "run ");
			mSocket = mServerSocket.accept();

			inputStream = mSocket.getInputStream();
			outputStream = mSocket.getOutputStream();
			polaczone = true;
			byte[] buffer = new byte[100000];
			int bytes;

			mmHandler.obtainMessage(MainBluetoothActivity.MESSAGE_WRITE, -1,
					-1, buffer).sendToTarget();

			while (true) {
				try {
					bytes = inputStream.read(buffer);
					mmHandler.obtainMessage(MainBluetoothActivity.MESSAGE_READ,
							bytes, -1, buffer).sendToTarget();

				} catch (IOException ex) {
					Log.d(TAG, "problem z wczytaniem bufora wejsciowego " + ex);
					break;
				}

			}
			mmHandler.obtainMessage(MainBluetoothActivity.CONNECTION_PROBLEM,
					-1, -1, buffer).sendToTarget();
			try {
				mSocket.close();
			} catch (IOException e) {
				Log.d(TAG, "zamkniecie " + e);
				e.printStackTrace();
			}
		} catch (Exception e) {
			Log.d(TAG, "problem z run " + e);
		}
	}

	public void write(byte[] buffer) {
		Log.d(TAG, "zapisywanie wiadomosci z MainBluetoothActivity " + buffer.length);
		try {

			outputStream.write(buffer);

		} catch (IOException e) {
			Log.d(TAG, "problem z zapisem" + e);
		}
	}

	public void cancel() {
		try {
			mServerSocket.close();
			polaczone = false;
		} catch (Exception ex) {
			Log.d(TAG, "problem z zamknieciem socketa");
		}
	}

	public boolean getConnected() {
		return polaczone;
	}

	
}
