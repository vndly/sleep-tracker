package com.mauriciotogneri.sleeptracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class DataService extends Service implements SensorEventListener
{
    private SensorManager sensorManager;
    private Sensor sensor;
    private WakeLock wakeLock;
    private OnSensorData onSensorDataListener;
    private FileLogger fileLogger;

    private static final int SAMPLES_PER_SECOND = 8;

    private static final float THRESHOLD_X = 0.05f;
    private static final float THRESHOLD_Y = 0.05f;
    private static final float THRESHOLD_Z = 0.05f;

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

        fileLogger = new FileLogger();
    }

    public void startRecording(OnSensorData listener)
    {
        onSensorDataListener = listener;

        sensorManager.registerListener(this, sensor, (1000 / SAMPLES_PER_SECOND) * 1000);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        long timestamp = System.currentTimeMillis();

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        onData(timestamp, x, y, z);
    }

    private void onData(long timestamp, float x, float y, float z)
    {
        if (isValid(x, y, z))
        {
            fileLogger.log(timestamp, x, y, z);
        }

        if (onSensorDataListener != null)
        {
            onSensorDataListener.onSensorData(x, y, z, timestamp);
        }
    }

    private boolean isValid(float x, float y, float z)
    {
        return (Math.abs(x) > THRESHOLD_X) || (Math.abs(y) > THRESHOLD_Y) || (Math.abs(z) > THRESHOLD_Z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
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

        fileLogger.close();

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