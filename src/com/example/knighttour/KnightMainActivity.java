package com.example.knighttour;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class KnightMainActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knight_main);
        
        final Button btnHola = (Button)findViewById(R.id.button1);
        
        btnHola.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Creamos el Intent
            	Intent intent = new Intent(KnightMainActivity.this, PrincipalActivity.class);

            	
            	//Iniciamos la nueva actividad
                startActivity(intent);
            }
        });
	}
    
}
