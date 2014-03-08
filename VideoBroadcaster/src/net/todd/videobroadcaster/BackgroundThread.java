package net.todd.videobroadcaster;

import android.os.Handler;
import android.os.Looper;

public class BackgroundThread extends Thread {
	private Handler handler;
	private Looper myLooper;

	@Override
	public void run() {
		try {
			Looper.prepare();
			
			handler = new Handler();
			myLooper = Looper.myLooper();
			
			Looper.loop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void quit() {
		myLooper.quit();
	}
	
	public void runInBackground(Runnable runnable) {
		handler.postAtFrontOfQueue(runnable);
	}
}
