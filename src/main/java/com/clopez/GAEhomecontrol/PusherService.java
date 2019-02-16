package com.clopez.GAEhomecontrol;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pusher.rest.Pusher;

public class PusherService {

	public static final String APP_KEY = System.getenv("PUSHER_APP_KEY");
	public static final String CLUSTER = System.getenv("PUSHER_CLUSTER");

	private static final String APP_ID = System.getenv("PUSHER_APP_ID");
	private static final String APP_SECRET = System.getenv("PUSHER_APP_SECRET");
	private static Pusher instance;

	static Pusher getDefaultInstance(Logger log) {
		if (instance != null) {
			return instance;
		}

		log.log(Level.FINE,  "ID: "+APP_ID+" KEY: "+ APP_KEY+" SECRET: "+ APP_SECRET+ " CLUSTER: "+CLUSTER);

		//Instantiate Pusher
		Pusher pusher = new Pusher(APP_ID, APP_KEY, APP_SECRET);
		pusher.setCluster(CLUSTER); // required, if not default mt1 (us-east-1)
		pusher.setEncrypted(true); // optional, ensure subscriber also matches these settings
		instance = pusher;
		return pusher;
	}
}
