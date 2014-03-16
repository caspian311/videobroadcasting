package net.todd.videobroadcaster;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
	private static final String HOSTNAME = "10.0.2.2";
	private static final int PORT = 1234;
	
	private MediaRecorder recorder;
	private ParcelFileDescriptor pfd;
	private BackgroundThread backgroundThread;
	private boolean isRecording;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final Camera camera = Camera.open();
		FrameLayout frameLayout = (FrameLayout)findViewById(R.id.video_view);
		frameLayout.addView(new CameraPreview(this, camera));

		findViewById(R.id.recordButton).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (isRecording) {
							stopButtonPressed(camera);
						} else {
							startButtonPressed(camera);
						}
					}
				});
	}

	private void stopButtonPressed(final Camera camera) {
		backgroundThread.runInBackground(new Runnable() {
			@Override
			public void run() {
				try {
					recorder.stop();
					recorder.release();
					camera.lock();
					
					pfd.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
				isRecording = false;
			}
		});
	}

	private void startButtonPressed(final Camera camera) {
		backgroundThread.runInBackground(new Runnable() {
			@Override
			public void run() {
				pfd = ParcelFileDescriptor.fromSocket(createSocket());
				recorder = startRecording(pfd, camera);
				
				isRecording = false;
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		backgroundThread = new BackgroundThread();
		backgroundThread.start();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		backgroundThread.quit();
		backgroundThread = null;
	}

	private static String getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
	    return new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4").toString();
	}
	
	private MediaRecorder startRecording(ParcelFileDescriptor pfd, Camera camera) {
		MediaRecorder recorder = null;

		try {
			recorder = new MediaRecorder();
			camera.unlock();
			recorder.setCamera(camera);
			
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			
			recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
			
//			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			recorder.setOutputFile(pfd.getFileDescriptor());
//			recorder.setOutputFile(getOutputMediaFile());
			
//			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//			recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

			recorder.prepare();
			recorder.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return recorder;
	}

	private Socket createSocket() {
		Socket socket = null;
		try {
			socket = new Socket(InetAddress.getByName(HOSTNAME), PORT);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return socket;
	}
}
