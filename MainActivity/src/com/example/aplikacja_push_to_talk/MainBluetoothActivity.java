package com.example.aplikacja_push_to_talk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import com.example.aplikacja_push_to_talk.R.string;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainBluetoothActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothServerSocket mServerSocket;

	public AudioRecord audiorecord;
	public AudioTrack audiotrack;
	private Thread Rthread;

	private int SAMPLERATE = 8000;
	private int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
	private int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
	private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

	private static UUID myUUID;
	private static final String myyUUID = "31cf3b50-bca4-11e3-b1b6-0800200c9a66"; // z
																					// generatora
																					// UUID
																					// wziête
																					// http://www.famkruithof.net/uuid/uuidgen
	private static final String mac_adres = "20:54:76:0F:B9:FE"; // Sony jako serwer
	protected static final int MESSAGE_READ = 1;
	protected static final int MESSAGE_WRITE = 2;
	private static final int REQUEST_ENABLE_BT = 0;
	public static final int CONNECTION_PROBLEM = 3;

	public Bluetooth_klient bluetooth_klient;
	public Bluetooth_Server bluetooth_Server;

	boolean czy_bluetooth_server;
	boolean czy_bluetooth_klient;
	boolean start;
	boolean recordStarted = true;
	public boolean status = true;
	private Button polacz;
	private ImageButton wyslij;
	private TextView statuss;
	private TextView wynik;

	private static String nag = "nagrywanie";


	ByteBuffer messageBuffer;
	int audioLength;
	int audioLength1;
	int packetLength;
	int bufferSize ;
	boolean recordAudio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		bufferSize = 5*AudioRecord.getMinBufferSize(SAMPLERATE, CHANNEL_IN,
				AUDIO_FORMAT)+Integer.SIZE/8;


		polacz = (Button) findViewById(R.id.polacz);
		wyslij = (ImageButton) findViewById(R.id.wyslij);
		statuss = (TextView) findViewById(R.id.statuss);
		statuss.setText(string.laczenie);

		wynik = (TextView) findViewById(R.id.wynik);
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Toast.makeText(getApplicationContext(),
						" nie obsluguje bluetooth ", Toast.LENGTH_LONG).show();
			}

			if (mBluetoothAdapter.isEnabled()) {
				wynik.setText("Adres: " + mBluetoothAdapter.getAddress() + "\n"
						+ " Nazwa: " + mBluetoothAdapter.getName());
				polaczenie();
			} else {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		audiorecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				SAMPLERATE, CHANNEL_IN, AUDIO_FORMAT,
				bufferSize);

		audiotrack = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION,
				SAMPLERATE, CHANNEL_OUT,AUDIO_FORMAT,
				bufferSize, AudioTrack.MODE_STREAM);
		audiotrack.play();

	}



	 byte[] buffer = new byte[bufferSize];
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				polaczenie();
			} else {
				Toast.makeText(getApplicationContext(),
						"bluetooth nie jest dostêpny ", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private void polaczenie() {
		// TODO Auto-generated method stub
		myUUID = UUID.fromString(myyUUID);

		if (mac_adres.equals(mBluetoothAdapter.getAddress().toString())) {
			czy_bluetooth_server = true;
		} else  {
			czy_bluetooth_server = false;
		}

		polacz.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				polacz.setEnabled(false);

				statuss.setText(string.oczekiwanie);
				try {
					if (czy_bluetooth_server == true) {
						bluetooth_Server = new Bluetooth_Server(myUUID,
								mBluetoothAdapter, mHandler);
						bluetooth_Server.start();

					} else /*if (czy_bluetooth_klient == false)*/ {
						BluetoothDevice device = mBluetoothAdapter
								.getRemoteDevice(mac_adres);
						bluetooth_klient = new Bluetooth_klient(device, myUUID,
								mBluetoothAdapter, mHandler);
						bluetooth_klient.start();

					}

				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							"nie mozna ustanowic polaczenia " + e,
							Toast.LENGTH_LONG).show();
				}
			}

		});
		
		wyslij.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					final byte[] bufferAudio= new byte[bufferSize];
					audiorecord.startRecording();
					Log.i("info", "nagrywanie dzwieku rozpoczete");
					Toast.makeText(getApplicationContext(),
							"rejestrownie dŸwiêku rozpoczete ... ",
							Toast.LENGTH_LONG).show();

					recordAudio=true;
					Rthread = new Thread(new Runnable() {
						public void run() {
							while (recordAudio) {
								try {

									audioLength = audiorecord.read(bufferAudio,0,bufferSize);
									if (start == false)
									{
										sendAudio(bufferAudio,audioLength);
									}
									
									
								} catch (Throwable t) {
									Log.e(nag,
											"nagrywanie blad " + t.toString());
									t.printStackTrace();
								}
							}
						}

				

					
					});
					Rthread.start();

				} else if (event.getAction() == MotionEvent.ACTION_UP) {

					if (!Thread.currentThread().isInterrupted()) {
						recordAudio=false;
						audiorecord.stop();
						Rthread.interrupt();
						
						Log.d(nag, "nie nagrywa ");
						


					}


				}
				return false;
			}

		});

	}

	
	private void sendAudio(byte[] buffer,int audioLength) {

		if (bluetooth_klient == null && bluetooth_Server == null) {
			Toast.makeText(getApplicationContext(),
					"nacisnij przycisk 'laczenie urzadzen' ", Toast.LENGTH_LONG)
					.show();
		}

		else if (czy_bluetooth_server == true && (bluetooth_Server != null)) {
			bluetooth_Server.write(buffer);
		} 
		
		
		else if (czy_bluetooth_server == false && (bluetooth_klient != null)) {
			bluetooth_klient.write(buffer);
		} 
		
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		polacz.setEnabled(true);
		statuss.setText(string.laczenie);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		if (bluetooth_klient != null) {
			bluetooth_klient.cancel();
			bluetooth_klient = null;
		}

		if (bluetooth_Server != null) {
			bluetooth_Server.cancel();
			bluetooth_Server = null;
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (bluetooth_klient != null) {
			bluetooth_klient.cancel();
			bluetooth_klient = null;
		}

		if (bluetooth_Server != null) {
			bluetooth_Server.cancel();
			bluetooth_Server = null;
		}

	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
			case MESSAGE_READ:
				ByteBuffer messageBuffer = ByteBuffer.allocate(msg.arg1);
				messageBuffer.put((byte[])msg.obj,0,msg.arg1);
				messageBuffer.rewind();

				audiotrack.play();
				audiotrack.write(messageBuffer.array(), 0, msg.arg1);
				Log.v("meeasge_read", "otrzymalem audio");
				audiotrack.stop();
				
				
				break;

			case MESSAGE_WRITE:
				statuss.setText(string.rozpocznij);
				break;

			case CONNECTION_PROBLEM:
				statuss.setText(string.problem_polaczenie);
				polacz.setEnabled(true);
				break;
			}
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
