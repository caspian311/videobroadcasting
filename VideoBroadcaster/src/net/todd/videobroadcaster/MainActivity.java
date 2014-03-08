package net.todd.videobroadcaster;

import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {
	private String hostname = "10.0.2.2";
	private int port = 1234;
	private MediaRecorder recorder;
	private ParcelFileDescriptor pfd;
	private BackgroundThread backgroundThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		backgroundThread = new BackgroundThread();
		backgroundThread.start();

		findViewById(R.id.startButton).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						startButtonPressed();
					}
				});

		findViewById(R.id.stopButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				stopButtonPressed();
			}
		});
	}

	private void stopButtonPressed() {
		backgroundThread.runInBackground(new Runnable() {
			@Override
			public void run() {
				try {
					recorder.stop();
					pfd.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private void startButtonPressed() {
		backgroundThread.runInBackground(new Runnable() {
			@Override
			public void run() {
				pfd = createFileDescriptor(hostname, port);
				recorder = startRecording(pfd);
			}
		});
	}
	
	@Override
	protected void onStop() {
		backgroundThread.quit();
	}

	private MediaRecorder startRecording(ParcelFileDescriptor pfd) {

		MediaRecorder recorder = null;
		try {
			recorder = new MediaRecorder();
			Camera camera = Camera.open();
			camera.unlock();
			recorder.setCamera(camera);
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(pfd.getFileDescriptor());

			recorder.prepare();
			recorder.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return recorder;
	}

	private ParcelFileDescriptor createFileDescriptor(String hostname, int port) {
		ParcelFileDescriptor pfd = null;

		try {
			Socket socket = new Socket(InetAddress.getByName(hostname), port);

			pfd = ParcelFileDescriptor.fromSocket(socket);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return pfd;
	}
}
