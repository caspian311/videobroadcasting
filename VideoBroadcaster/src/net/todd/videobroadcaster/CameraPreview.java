package net.todd.videobroadcaster;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView {
	private static final String TAG = CameraPreview.class.toString();

	public CameraPreview(Context context, final Camera camera) {
		super(context);

		final SurfaceHolder holder = getHolder();
		holder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder surfaceHolder) {
				try {
					camera.setPreviewDisplay(holder);
					camera.startPreview();
				} catch (IOException e) {
					Log.e(TAG, "Error setting camera preview: " + e.getMessage());
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
					int w, int h) {
				if (holder.getSurface() == null) {
					return;
				}

				try {
					camera.stopPreview();
				} catch (Exception e) {
					Log.i(TAG, "failed to stop preview");
				}

				try {
					camera.setPreviewDisplay(holder);
					camera.startPreview();
				} catch (Exception e) {
					Log.e(TAG, "Error starting camera preview: " + e.getMessage());
				}
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
			}
		});
	}
}
