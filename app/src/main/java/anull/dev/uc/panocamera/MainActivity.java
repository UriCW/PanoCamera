package anull.dev.uc.panocamera;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
//Google VR
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
/*
 * Code mostly ripped off of google vr example SimplePanoramaActivity
 */
public class MainActivity extends AppCompatActivity {
    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;
    /** Configuration information for the panorama. **/
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
    private ImageLoaderTask backgroundImageLoaderTask;
    public boolean loadImageSuccessful;
    private Uri fileUri;

    /* Camera preview */
    CameraPreview camPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Panorama init
        panoWidgetView = (VrPanoramaView) findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());
        panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
        backgroundImageLoaderTask = new ImageLoaderTask();
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));

        //Camera init
        try {
            camPreview = new CameraPreview(this);
            RelativeLayout camLayout=(RelativeLayout)findViewById(R.id.cam_layout);
            camLayout.addView(camPreview);
            //RelativeLayout root_layout = (RelativeLayout) findViewById(R.id.activity_main);
            //root_layout.addView(camPreview);
        }catch (Exception e){
            e.printStackTrace();
        }

        //Button
        Button recordButton = (Button)findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"This does nothing yet",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onPause(){
        super.onPause();
        panoWidgetView.pauseRendering();
    }

    @Override
    protected void onResume(){
        super.onResume();
        panoWidgetView.resumeRendering();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        panoWidgetView.shutdown();
    }

    /**
     * Helper class to manage threading.
     */
    class ImageLoaderTask extends AsyncTask<Pair<Uri, VrPanoramaView.Options>, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Uri, VrPanoramaView.Options>... fileInformation) {
            VrPanoramaView.Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            InputStream istr = null;
            if (fileInformation == null || fileInformation.length < 1
                    || fileInformation[0] == null || fileInformation[0].first == null) {
                AssetManager assetManager = getAssets();
                try {
                    istr = assetManager.open("panorama1.jpg");
                    panoOptions = new VrPanoramaView.Options();
                    panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
                } catch (IOException e) {
                    Log.e("TAG", "Could not decode default bitmap: " + e);
                    return false;
                }
            } else {
                try {
                    istr = new FileInputStream(new File(fileInformation[0].first.getPath()));
                    panoOptions = fileInformation[0].second;
                } catch (IOException e) {
                    Log.e("TAG", "Could not load file: " + e);
                    return false;
                }
            }

            panoWidgetView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
            try {
                istr.close();
            } catch (IOException e) {
                Log.e("TAG", "Could not close input stream: " + e);
            }

            return true;
        }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    MainActivity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e("TAG", "Error loading pano: " + errorMessage);
        }
    }

}
