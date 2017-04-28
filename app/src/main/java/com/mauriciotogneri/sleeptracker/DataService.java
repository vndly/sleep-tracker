package com.mauriciotogneri.sleeptracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class DataService extends Service implements SensorEventListener
{
    private SensorManager sensorManager;
    private Sensor sensor;
    private WakeLock wakeLock;

    private OnSensorData onSensorDataListener;
    private BufferedWriter bufferedWriter;

    private DecimalFormat decimalFormat = new DecimalFormat("#.####");

    private static final int SAMPLES_PER_SECOND = 8;
    private static final String COLUMN_SEPARATOR = ",";

    @Override
    public IBinder onBind(Intent intent)
    {
        return new ServiceBinder(this);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyWakeLock");
        wakeLock.acquire();
    }

    public void startRecording(OnSensorData listener)
    {
        onSensorDataListener = listener;

        setupFile();

        sensorManager.registerListener(this, sensor, (1000 / SAMPLES_PER_SECOND) * 1000);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        long timestamp = System.currentTimeMillis();

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        writeLine(timestamp + COLUMN_SEPARATOR + decimalFormat.format(x) + COLUMN_SEPARATOR + decimalFormat.format(y) + COLUMN_SEPARATOR + decimalFormat.format(z));

        if (onSensorDataListener != null)
        {
            onSensorDataListener.onSensorData(x, y, z, timestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    private void setupFile()
    {
        try
        {
            SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            File file = new File(Environment.getExternalStorageDirectory() + "/" + sourceDateFormat.format(System.currentTimeMillis()) + ".csv");

            if (file.createNewFile())
            {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                writeLine("Time" + COLUMN_SEPARATOR + "X" + COLUMN_SEPARATOR + "Y" + COLUMN_SEPARATOR + "Z");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void writeLine(String text)
    {
        if (bufferedWriter != null)
        {
            try
            {
                bufferedWriter.write(text + "\n");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        //Log.d("SERVICE", "DESTROYED!");

        sensorManager.unregisterListener(this);

        try
        {
            wakeLock.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    public static class ServiceBinder extends Binder
    {
        private final DataService dataService;

        public ServiceBinder(DataService dataService)
        {
            this.dataService = dataService;
        }

        public DataService getService()
        {
            return dataService;
        }
    }

    public interface OnSensorData
    {
        void onSensorData(float x, float y, float z, long timestamp);
    }
}