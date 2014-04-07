package com.example.aplikacja_push_to_talk;

import java.net.InetAddress;
import java.net.UnknownHostException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Wifi extends Activity implements OnClickListener
 {

    private Button wyslij;
    private Button odbierz;
    private ListView lista_wifi;
    private InetAddress[] inetaddress;
    public EditText hostinput;
    ArrayAdapter<String> lista_urzadzen1;
    String wifis[];

    WifiManager wifimanager;

    public Wysylanie_dzwiek mWysylanie;
    public Odbieranie_dzwiek mOdbieranie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	//setContentView(R.layout.wifi_main);

	
	//lista_wifi = (ListView) findViewById(R.id.lista_wifi);

	
	//lista_wifi.setOnItemClickListener(this);

	//wyslij = (Button) findViewById(R.id.wyslij);
	//wyslij.setOnClickListener(this);

	//odbierz = (Button) findViewById(R.id.odbierz);
	//odbierz.setOnClickListener(this);
    }
    @Override
    public void onClick(View arg0) {
	// TODO Auto-generated method stub
	
    }

 
/*
    @Override
    public void onClick(View v) {
	// TODO Auto-generated method stub
	switch (v.getId()) {
	
	case R.id.wyslij:
	    Intent intent = new Intent(Wifi.this, Wysylanie_dzwiek.class);
	    startActivity(intent);
	    break;
	case R.id.odbierz:
	    Intent intent1 = new Intent(Wifi.this, Wysylanie_dzwiek.class);
	    startActivity(intent1);
	    break;
	}
    }
*/
}
