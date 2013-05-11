package com.swcm.remindme;

import com.swcm.remind4me.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AboutUsActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus_layout);
		Log.i("About Activity", "started");
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
