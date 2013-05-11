package com.swcm.remindme;

import com.swcm.remind4me.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {

	protected boolean active = true;
	protected int splashTime = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		Thread mythread = new Thread() {
			public void run() {
				try {
					sleep(splashTime);
				} catch (Exception e) {
				} finally {
					Intent intent = new Intent(SplashActivity.this,
							ListRemindsActivity.class);
					startActivity(intent);
					finish();
				}
			}
		};
		mythread.start();

	}

}
