package com.mauriciotogneri.sleeptracker;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class PhotoActivity extends Activity implements Camera.ShutterCallback, Camera.PictureCallback
{
    private Camera camera;
    private File directory = new File(Environment.getExternalStorageDirectory() + "/photos");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        if (!directory.exists())
        {
            directory.mkdir();
        }

        findViewById(R.id.button_take_picture).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takePicture();
            }
        });
    }

    private void takePicture()
    {
        try
        {
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(640, 480);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            parameters.setPictureFormat(ImageFormat.JPEG);
            camera.setParameters(parameters);
            SurfaceView surfaceView = new SurfaceView(getBaseContext());
            camera.setPreviewDisplay(surfaceView.getHolder());
            camera.startPreview();
            camera.takePicture(this, null, this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {
        try
        {
            File file = new File(directory, "pic1.jpeg");
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();

            Toast.makeText(PhotoActivity.this, "PHOTO TAKEN!", Toast.LENGTH_SHORT).show();

            camera.stopPreview();
            camera.release();
            this.camera = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutter()
    {
    }
}