package com.example.wuziqi;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	
	private WuziqiPanel wuziqiPanel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		wuziqiPanel = (WuziqiPanel) findViewById(R.id.wuziqi);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		int id = item.getItemId();
		
		if(id == R.id.action_settings){
			wuziqiPanel.restart();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

}
