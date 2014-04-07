package com.example.aplikacja_push_to_talk;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.http.message.BufferedHeader;

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
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.renderscript.Byte3;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainBluetoothActivity extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;

    public AudioTrack audiotrack;
    public AudioRecord audiorecord;
    private Thread Rthread_receive;
    
    private int SAMPLERATE = 8000;
    private int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static UUID myUUID;
    private static final String myyUUID = "31cf3b50-bca4-11e3-b1b6-0800200c9a66"; // z generatora UUID wzi�te http://www.famkruithof.net/uuid/uuidgen
    private static final String mac_adres = "20:54:76:0F:B9:FE"; // Nexus 7

    protected static final int MESSAGE_READ = 1;
    protected static final int MESSAGE_WRITE = 2;
    private static final int REQUEST_ENABLE_BT = 0;
    public static final int CONNECTION_PROBLEM = 3;

    public Bluetooth_klient blue_klient;
    public Bluetooth_Server blue_server;

    boolean czy_blue_server;
    boolean start;
    public boolean statuss = true;
    private Button polacz;
    private ImageButton wyslij;
    private TextView status;
    private TextView wynik;
    private static String TAG = "Wysylanie";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_bluetooth);

	polacz = (Button) findViewById(R.id.polacz);
	wyslij = (ImageButton) findViewById(R.id.wyslij);
	status = (TextView) findViewById(R.id.status);
	status.setText(string.laczenie);

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
	
	audiotrack = new AudioTrack(AudioManager.STREAM_MUSIC,
		SAMPLERATE, CHANNEL_OUT, AUDIO_FORMAT,
		bufferSize, AudioTrack.MODE_STREAM);
	
	    
	audiorecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
		    SAMPLERATE, CHANNEL_IN, AUDIO_FORMAT, bufferSize);
    }

    int bufferSize = AudioRecord.getMinBufferSize(SAMPLERATE,
	    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    byte[] buffer = new byte[bufferSize];

	    Thread rejestrowanie = new Thread(new Runnable() {
		    
		    @Override
		    public void run() {
			// TODO Auto-generated method stub
			 Log.d(TAG, "watek nagrywania rozpoczety");
			Looper.prepare();
			try
			{
			    
			    byte[] buffer = new byte[bufferSize];
			    Toast.makeText(getApplicationContext(), "rejestrowanie ... ", Toast.LENGTH_LONG).show();
			    audiorecord.startRecording();
			    while (statuss == true)
			    {
				bufferSize = audiorecord.read(buffer, 0, buffer.length);
				
			    }
			}
			catch (Exception e)
			{
			    e.printStackTrace();
			}
		    }
		});
	
	
	
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// TODO Auto-generated method stub
	super.onActivityResult(requestCode, resultCode, data);

	if (requestCode == REQUEST_ENABLE_BT) {
	    if (resultCode == RESULT_OK)
	    {
		polaczenie();
	    }
	    else
	    {
		Toast.makeText(getApplicationContext(), "bluetooth nie jest dost�pny ", Toast.LENGTH_LONG).show();
	    }
	} 
    }

    
    public void odtwarzanie()
    {
	
	Log.i("info", "odtwarzanie rozpoczete");
	
	Rthread_receive = new Thread(new Runnable() {
	    
	    @Override
	    public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();
		audiotrack.play();
		while (true)
		{
		    try
		    {
			audiotrack.write(buffer, 0, buffer.length);
			
		    }
		    catch (Exception e)
		    {
			 Log.e("Error", "odczytywanie danych  blad " + e.toString());
			e.printStackTrace();
		    }
		}
	    }
	});
	Rthread_receive.start();
    }

    private void polaczenie() {
	// TODO Auto-generated method stub
	myUUID = UUID.fromString(myyUUID);

	if (mac_adres.equals(mBluetoothAdapter.getAddress().toString())) {
	    czy_blue_server = true;
	} else {
	    czy_blue_server = false;
	}
	

	polacz.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// TODO Auto-generated method stub
		polacz.setEnabled(false);

		status.setText(string.oczekiwanie);
		try {
		    if (czy_blue_server == true) {
			blue_server = new Bluetooth_Server(myUUID,
				mBluetoothAdapter, mHandler);
			blue_server.start();

		    } else {
			BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(mac_adres);
			blue_klient = new Bluetooth_klient(device, myUUID,
				mBluetoothAdapter, mHandler);
			blue_klient.start();

		    }

		} catch (Exception e) {
		    Toast.makeText(getApplicationContext(),
			    "nie mozna ustanowic polaczenia " + e,
			    Toast.LENGTH_LONG).show();
		}
	    }

	});
	
	wyslij.setOnLongClickListener(new OnLongClickListener() {
	    
	    @Override
	    public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		rejestrowanie.start();
		//
		return false;
	    }
	});
	
	wyslij.setOnTouchListener(new OnTouchListener() {
	    
	    @Override
	    public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case MotionEvent.ACTION_DOWN:
		    rejestrowanie.start();
		    
		    break;
		case MotionEvent.ACTION_UP:
		    if (rejestrowanie.isAlive())
		    {
	    		Log.d(TAG, "watek zatrzymany");
		    }
		    if (start == false)
			{
			    wyslij_wiadomosc(buffer);
			}
		    
		}
		return false;
	    }
	});
	


    }

    
    private void wyslij_wiadomosc(byte[] buffer) {
	if (blue_klient == null && blue_server == null) {
	    Toast.makeText(getApplicationContext(),
		    "nacisnij polacz na poczatku ", Toast.LENGTH_LONG).show();
	}

	if (czy_blue_server == true && (blue_server != null)) {
	    blue_server.write(buffer);
	}
	if (czy_blue_server == false && (blue_klient != null)) {
	    blue_klient.write(buffer);
	}
    }

    // odbieranie

    /*private void wyslij_wiadomosc(String wiadomosc) {
	if (blue_klient == null && blue_server == null) {
	    Toast.makeText(getApplicationContext(),
		    "nacisnij laczenie urzadzen ", Toast.LENGTH_LONG).show();
	}

	if (czy_blue_server == true && (blue_server != null)) {
	    blue_server.write((wiadomosc).getBytes());
	}
	if (czy_blue_server == false && (blue_klient != null)) {
	    blue_klient.write((wiadomosc).getBytes());
	}
    }
*/
    @Override
    protected void onResume() {
	// TODO Auto-generated method stub
	super.onResume();

	polacz.setEnabled(true);
	status.setText(string.laczenie);
    }

    @Override
    protected void onPause() {
	// TODO Auto-generated method stub
	super.onPause();

	if (blue_klient != null) {
	    blue_klient.cancel();
	    blue_klient = null;
	}

	if (blue_server != null) {
	    blue_server.cancel();
	    blue_server = null;
	}

    }

    @Override
    protected void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();

	if (blue_klient != null) {
	    blue_klient.cancel();
	    blue_klient = null;
	}

	if (blue_server != null) {
	    blue_server.cancel();
	    blue_server = null;
	}
    }

    private Handler mHandler = new Handler() {

	@Override
	public void handleMessage(Message msg) {
	    // TODO Auto-generated method stub
	    super.handleMessage(msg);

	    switch (msg.what) {
	    case MESSAGE_READ:
		 //byte[] odczytaj = (byte[]) msg.obj;
		// String odczytaj_wiadomosc = new String(odczytaj, 0,
		// msg.arg1);
		// status.setText(odczytaj_wiadomosc);
		
		 odtwarzanie();
		break;

	    case MESSAGE_WRITE:
		status.setText(string.rozpocznij);
		//strumieniowanie();
		break;

	    case CONNECTION_PROBLEM:
		status.setText(string.problem_polaczenie);
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
