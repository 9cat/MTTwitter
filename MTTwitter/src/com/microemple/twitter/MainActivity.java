package com.microemple.twitter;

/**
 * By MT 5433
 * 
 * 
 */
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.microtemple.android.lib.common.L;

public class MainActivity extends Activity {

	private MTTwitterManager	twitterMan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.button1).setOnClickListener(clear());
		findViewById(R.id.button2).setOnClickListener(login());

		findViewById(R.id.button3).setOnClickListener(tweet());
		twitterMan = MTTwitterManager.getInstance().init(this);


	}

	private OnClickListener login() {

		return new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				twitterMan.init(MainActivity.this);
			}

		};
	}

	private OnClickListener clear() {
		return new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				twitterMan.clear();
			}
		};
	}

	private OnClickListener tweet() {
		return new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new Thread() {
					@Override
					public void run() {
						Twitter twitter = twitterMan.getTwitter();
						try {
							twitter.updateStatus("Oooppss!"
								+ System.currentTimeMillis());
						} catch (TwitterException e) {
							L.e(e);
						}
					}
				}.start();
			}
		};


	}


}
