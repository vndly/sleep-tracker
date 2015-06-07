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

public class DataService extends Service implements SensorEventListener
{
    private SensorManager sensorManager;
    private Sensor sensor;
    private WakeLock wakeLock;

    private OnSensorData onSensorDataListener;
    private long initialTime;
    private BufferedWriter bufferedWriter;

    private DecimalFormat decimalFormat = new DecimalFormat("#.####");

    private static final int SAMPLES_PER_SECOND = 10;

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

        //Log.d("SERVICE", "CREATED!");

        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyWakeLock");
        wakeLock.acquire();
    }

    public void startRecording(OnSensorData listener)
    {
        onSensorDataListener = listener;
        initialTime = System.currentTimeMillis();

        setupFile();

        sensorManager.registerListener(this, sensor, (1000 / SAMPLES_PER_SECOND) * 1000);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        long timestamp = System.currentTimeMillis() - initialTime;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        writeLine(timestamp + ";" + decimalFormat.format(x) + ";" + decimalFormat.format(y) + ";" + decimalFormat.format(z));

        if (onSensorDataListener != null)
        {
            onSensorDataListener.onSensorData(x, y, z, timestamp);
        }

        //Log.d("DATA", "NEW DATA!");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    private void setupFile()
    {
        try
        {
            File file = new File(Environment.getExternalStorageDirectory() + "/data_" + initialTime + ".csv");

            if (file.createNewFile())
            {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                writeLine("Time;X;Y;Z");
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
        //        Intent notificationIntent = new Intent(this, MainActivity.class);
        //        notificationIntent.setAction("ACTION");
        //        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        //
        //        Intent nextIntent = new Intent(this, DataService.class);
        //        nextIntent.setAction("ACTION");
        //        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);
        //
        //        Notification notification = new NotificationCompat.Builder(this).setContentTitle("Data Service").setSmallIcon(R.drawable.app_icon).setContentIntent(pendingIntent).setOngoing(true).addAction(android.R.drawable.ic_media_next, "Next", pnextIntent).build();
        //        startForeground(123, notification);

        return START_STICKY;
    }

    public void stopService()
    {
        stopForeground(true);
        stopSelf();
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