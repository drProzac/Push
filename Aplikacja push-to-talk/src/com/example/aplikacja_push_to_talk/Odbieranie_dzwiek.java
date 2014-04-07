package com.example.aplikacja_push_to_talk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Odbieranie_dzwiek extends Activity implements OnClickListener {

    private int SAMPLERATE = 8000;
  
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack audiotrack;
    private boolean status = true;

    private Button stop;
    private TextView adress_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_dzwiek_2);
	

	stop = (Button) findViewById(R.id.stop);
	stop.setOnClickListener(this);

	

	getWifiIpAddress();
    }

    public String getWifiIpAddress() {
	WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	int ip = wifiInfo.getIpAddress();

	String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
		(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

	adress_ip.setText("Odbierajacy IP: " + ipString);

	return ipString;
    }

 

    @Override
    public void onClick(View v) {
	// TODO Auto-generated method stub
	switch (v.getId()) {
	case R.id.stop:
	    status = false;
	    audiotrack.release();
	    finish();
	    break;
	}
    }
}
