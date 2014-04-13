package com.example.aplikacja_push_to_talk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Obsluga_dzwiek_wifi extends Activity implements 
	OnTouchListener, OnLongClickListener {


    private AudioTrack audiotrack;
    public AudioRecord recorder;
    
    private EditText wprowadz;
    private int SAMPLERATE = 8000;
    private int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    
    public boolean status = true;
    public static final int SERVERPORT = 55984;

    private TextView adress_ip;
    private ImageButton nagraj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_dzwiek1);
	
	nagraj = (ImageButton) findViewById(R.id.nagraj);
	nagraj.setOnLongClickListener(this);
	

	wprowadz = (EditText) findViewById(R.id.wprowadz);

	adress_ip = (TextView) findViewById(R.id.adress_ip);

	getWifiIpAddress();
	
	odbieranie();
	 
	 recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
		    SAMPLERATE, CHANNEL_IN, AUDIO_FORMAT, minBufSize);
	
	    }
 
    int minBufSize = AudioRecord.getMinBufferSize(SAMPLERATE,
	    CHANNEL_IN, AUDIO_FORMAT);
    

    public void odbieranie() {

	receiveThread.start();
 	Toast.makeText(getApplicationContext(), "odbieranie ",
 		Toast.LENGTH_LONG).show();
 	
     }

    Thread receiveThread = new Thread(new Runnable() {

	    @Override
	    public void run() {

		try {

		    DatagramSocket socket = new DatagramSocket(55984);
		    Log.d("VR", "Socket utworzony ");

		    int bufferSize = 50* AudioTrack.getMinBufferSize(
			    SAMPLERATE, CHANNEL_OUT, AUDIO_FORMAT);
		    byte[] buffer = new byte[bufferSize];

		    audiotrack = new AudioTrack(AudioManager.STREAM_MUSIC,
			    SAMPLERATE, CHANNEL_OUT, AUDIO_FORMAT, bufferSize,
			    AudioTrack.MODE_STREAM);

		    audiotrack.play();

		    while (status == true) {
			try {

			    DatagramPacket packet = new DatagramPacket(buffer,
				    buffer.length);
			    socket.receive(packet);
			    Log.d("VR", "pakiet utworzony");

			    // odczytanie z pakietu
			    buffer = packet.getData();
			    Log.d("VR", "pakiet wpisany w bufor");

			    // 
			    audiotrack.write(buffer, 0, bufferSize);
			    Log.d("VR", "wpisanie bufora do audiotrack");
			 
			} catch (IOException e) {
			    socket.close();
			    Log.e("VR", "IOException" + e.toString());
			}
		    }
		 

		} catch (SocketException e) {
		   
		    Log.e("VR", "SocketException" + e.toString());
		}

	    }

	});


    
    public String getWifiIpAddress() {
	WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	int ip = wifiInfo.getIpAddress();

	String ipString = String.format(Locale.getDefault(), "%d.%d.%d.%d", (ip & 0xff),
		(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

	adress_ip.setText("moj IP to: " + ipString);

	return ipString;

    }

    Thread streamThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		try {

		    DatagramSocket socket = new DatagramSocket();
		    Log.d("VS", "socket utworzony ");

		    byte[] buffer = new byte[minBufSize];

		    Log.d("VS", "bufor utworzony z rozmiarem " + minBufSize);
		    DatagramPacket packet;

		    InetAddress cel = InetAddress.getByName(wprowadz
			    .getText().toString());
		    Log.d("VS", "Adres odebrany");

		   
		    Log.d("VS", "recorder zainicjowany");

		    recorder.startRecording();

		    while (status == true) {

			minBufSize = recorder.read(buffer, 0, buffer.length);

			packet = new DatagramPacket(buffer, buffer.length, cel,
				SERVERPORT);

			socket.send(packet);

			
		    }
		    //socket.close();
		   		
		} catch (IOException e) {
		  
		    Log.e("VS", "IOException" + e.toString());
		    
		}

	    }

	});
    



    @Override
    public boolean onLongClick(View v) {
	// TODO Auto-generated method stub
	switch (v.getId())
	{
	case R.id.nagraj:
	    status = true;
	    streamThread.start();
	    if (wprowadz.getText().toString().length() == 0)
	    {
		Toast.makeText(getApplicationContext(), "najpierw wprowadz adres" , Toast.LENGTH_LONG).show();
	    }
	    break;
	}
	return false;
    }
  
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
	// TODO Auto-generated method stub
	switch (v.getId())
	{
	case MotionEvent.ACTION_DOWN:
	    status = false;
	    streamThread.start();
	
	    break;
	case MotionEvent.ACTION_UP:
	    status = false;
	    if (!Thread.currentThread().isInterrupted())
	    {
	    	 recorder.stop();
	 	    recorder.release();
	 	    audiotrack.release();
	 	    audiotrack.stop();
	 	    
	 	   streamThread.interrupt();
	    }
	   
	    break;
	}
	return false;
    }


}
