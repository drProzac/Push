package com.example.aplikacja_push_to_talk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.TimerTask;
import java.util.UUID;

import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.apache.http.message.BufferedHeader;

import com.example.aplikacja_push_to_talk.R.string;

import android.R.bool;
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

    public AudioRecord audiorecord;
    public AudioTrack audiotrack;
    private Thread Rthread;
    
    private int SAMPLERATE = 8000;
    private int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    
    private static final String rec = "Nagrywanie";
    private static String play = "Odtwarzanie";

    private static UUID myUUID;
    private static final String myyUUID = "31cf3b50-bca4-11e3-b1b6-0800200c9a66"; // z generatora UUID wziête http://www.famkruithof.net/uuid/uuidgen
    private static final String mac_adres = "20:54:76:0F:B9:FE"; // Nexus 7

    protected static final int MESSAGE_READ = 1;
    protected static final int MESSAGE_WRITE = 2;
    private static final int REQUEST_ENABLE_BT = 0;
    public static final int CONNECTION_PROBLEM = 3;

    public Bluetooth_klient bluetooth_klient;
    public Bluetooth_Server bluetooth_Server;

    boolean czy_bluetooth_server;
    boolean start;
    boolean recordStarted=true;
    public boolean status = true;
    private Button polacz;
    private ImageButton wyslij;
    private TextView statuss;
    private TextView wynik;
    private static String TAG = "Wysylanie";
    
    private static boolean completePacket=false;
    private static boolean receiveLeght=false;
    private static int currentBufferPosition;
	ByteBuffer packetBuffer;

	int packetLength;
	
	private volatile bool stopThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_bluetooth);
	
	packetBuffer=ByteBuffer.allocate(100000);
	
	polacz = (Button) findViewById(R.id.polacz);
	wyslij = (ImageButton) findViewById(R.id.wyslij);
	statuss = (TextView) findViewById(R.id.statuss);
	statuss.setText(string.laczenie);

	wynik = (TextView) findViewById(R.id.wynik);
	try 
	{
	    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    if (mBluetoothAdapter == null) 
	    {
		Toast.makeText(getApplicationContext(),
			" nie obsluguje bluetooth ", Toast.LENGTH_LONG).show();
	    }

	    if (mBluetoothAdapter.isEnabled()) 
	    {
		wynik.setText("Adres: " + mBluetoothAdapter.getAddress() + "\n"
			+ " Nazwa: " + mBluetoothAdapter.getName());
		polaczenie();
	    } 
	    else 
	    {
		Intent enableBtIntent = new Intent(
			BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	 audiorecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLERATE,
             CHANNEL_IN,
             MediaRecorder.AudioEncoder.AMR_NB, bufferSize);

     audiotrack = new AudioTrack(AudioManager.ROUTE_HEADSET, SAMPLERATE,
             CHANNEL_OUT,
             MediaRecorder.AudioEncoder.AMR_NB, bufferSize,
             AudioTrack.MODE_STREAM);
	
    }
    int bufferSize = AudioRecord.getMinBufferSize(SAMPLERATE,
    		CHANNEL_IN, AudioFormat.ENCODING_PCM_16BIT);
	
    byte[] buffer = new byte[bufferSize];
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
		Toast.makeText(getApplicationContext(), "bluetooth nie jest dostêpny ", Toast.LENGTH_LONG).show();
	    }
	} 
    }


    private void polaczenie() {
	// TODO Auto-generated method stub
	myUUID = UUID.fromString(myyUUID);

	if (mac_adres.equals(mBluetoothAdapter.getAddress().toString())) {
	    czy_bluetooth_server = true;
	} else {
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

		    } else {
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
		
	    	if (event.getAction() == MotionEvent.ACTION_DOWN){
	            
	    			audiorecord.startRecording();
			        Log.i("info", "Audio Recording started");
			      // audiotrack.play();
			        Log.i("info", "Audio Playing started");
			        Rthread = new Thread(new Runnable() {
			            public void run() {
			                while (true) {
			                    try {
			                        audiorecord.read(buffer, 0, bufferSize);                                    
			                        //audiotrack.write(buffer, 0, buffer.length);

			                    } catch (Throwable t) {
			                        Log.e("Error", "wpisywa");
			                        t.printStackTrace();
			                    }
			                }
			            }
			        });
			        Rthread.start();
	    		
	    	}
	    	else if (event.getAction() == MotionEvent.ACTION_UP)
	    	{
	    		 if (!Thread.currentThread().isInterrupted()) 
	    		 {
	    			 audiorecord.stop();
	    			 audiorecord.release();
	    			 audiotrack.stop();
	    			 audiotrack.release();
	   
	    			 Rthread.interrupt();
	    			 
	    			 wyslij_wiadomosc(buffer);
	    		 }
	    	}
		return false;
	    }

	});
	


    }

  

    private void wyslij_wiadomosc(byte[] buffer) {
	if (bluetooth_klient == null && bluetooth_Server == null) {
	    Toast.makeText(getApplicationContext(),
		    "nacisnij polacz na poczatku ", Toast.LENGTH_LONG).show();
	}

	if (czy_bluetooth_server == true && (bluetooth_Server != null)) {
		bluetooth_Server.write(buffer);
	}
	if (czy_bluetooth_server == false && (bluetooth_klient != null)) {
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
	    	ByteBuffer messageBuffer=ByteBuffer.allocate(msg.arg1);
	    	
	    	packetBuffer.put(messageBuffer);
	    	currentBufferPosition=packetBuffer.position();

	    	if (currentBufferPosition > Integer.SIZE)
	    	{
	    		packetLength=packetBuffer.getInt(0);
	    		if (packetLength< currentBufferPosition)
	    		{
	    			completePacket=false;
	    		}
	    		else
	    		{
	    			completePacket=true;
	    			currentBufferPosition=0;
	    			packetBuffer.rewind();
	    		}
	    	}
	    		
	    	if (completePacket)
	    	{
	    		//odtworz dzwiek
	    		Thread odtworz = new Thread(new Runnable() {
	    			
	    			@Override
	    			public void run() {
	    				// TODO Auto-generated method stub
	    				try
	    				{
	    					audiotrack = new AudioTrack(AudioManager.STREAM_SYSTEM,
	    							SAMPLERATE, CHANNEL_OUT, AUDIO_FORMAT,
	    							2*bufferSize, AudioTrack.MODE_STREAM);
	    					audiotrack.play();
	    					Log.e(play, "trwa odtwarzanie ... ");
	    					int i=0;
	    					while(i<bufferSize) {
	    					audiotrack.write(buffer, i++, 1);
	    				
	    					}
	    				}
	    				catch (Exception e)
	    				{
	    					e.printStackTrace();
	    					Log.e("Odtwarzanie: ", "blad " + e.toString());
	    				}
	    			}
	    		});
	    		odtworz.start();
	    	}
	    	else
	    	{
	    		
	    	}
	    	
	    		
		 //byte[] odczytaj = (byte[]) msg.obj;
		// String odczytaj_wiadomosc = new String(odczytaj, 0,
		// msg.arg1);
		// status.setText(odczytaj_wiadomosc);
		break;

	    case MESSAGE_WRITE:
		statuss.setText(string.rozpocznij);
		//strumieniowanie();
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

