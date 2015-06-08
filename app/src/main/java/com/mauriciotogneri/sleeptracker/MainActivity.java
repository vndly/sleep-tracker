package com.mauriciotogneri.sleeptracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mauriciotogneri.sleeptracker.DataService.OnSensorData;
import com.mauriciotogneri.sleeptracker.DataService.ServiceBinder;

import java.util.Iterator;

public class MainActivity extends Activity implements OnSensorData
{
    private DataService dataService;
    private ServiceConnection serviceConnection;

    private long initialTime;

    private final LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<>();
    private final LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<>();

    private TextView averageX;
    private TextView averageY;
    private TextView averageZ;

    private static final int MAX_DATA_LENGTH = 50;

    private static final float AXIS_X_RESOLUTION = 2000;
    private static final float AXIS_Y_RESOLUTION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        averageX = (TextView) findViewById(R.id.average_x);
        averageY = (TextView) findViewById(R.id.average_y);
        averageZ = (TextView) findViewById(R.id.average_z);

        configureGraph(R.id.graph_x, seriesX, Color.RED);
        configureGraph(R.id.graph_y, seriesY, Color.GREEN);
        configureGraph(R.id.graph_z, seriesZ, Color.BLUE);

        serviceConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                onConnected(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                onDisconnected();
            }
        };

        Intent intent = new Intent(this, DataService.class);
        //startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void onConnected(IBinder service)
    {
        initialTime = System.currentTimeMillis();

        ServiceBinder binder = (ServiceBinder) service;
        dataService = binder.getService();
        dataService.startRecording(this);

        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.start();
    }

    private void onDisconnected()
    {
        finish();
    }

    private void configureGraph(int id, LineGraphSeries<DataPoint> series, int color)
    {
        GraphView graph = (GraphView) findViewById(id);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-AXIS_Y_RESOLUTION);
        graph.getViewport().setMaxY(AXIS_Y_RESOLUTION);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(-AXIS_X_RESOLUTION);
        graph.getViewport().setMaxX(AXIS_X_RESOLUTION);

        graph.addSeries(series);

        series.setColor(color);
        series.setThickness(4);
    }

    @Override
    public void onSensorData(float x, float y, float z, long timestamp)
    {
        long time = timestamp - initialTime;

        seriesX.appendData(new DataPoint(time, x), true, MAX_DATA_LENGTH);
        seriesY.appendData(new DataPoint(time, y), true, MAX_DATA_LENGTH);
        seriesZ.appendData(new DataPoint(time, z), true, MAX_DATA_LENGTH);

        averageX.setText(String.valueOf(getAverage(seriesX)));
        averageY.setText(String.valueOf(getAverage(seriesY)));
        averageZ.setText(String.valueOf(getAverage(seriesZ)));
    }

    private double getAverage(LineGraphSeries<DataPoint> series)
    {
        double sum = 0;
        int elements = 0;

        for (Iterator<DataPoint> iteratorX = series.getValues(0, Integer.MAX_VALUE); iteratorX.hasNext(); )
        {
            DataPoint element = iteratorX.next();

            sum += element.getY();
            elements++;
        }

        return sum / elements;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        unbindService(serviceConnection);
        //dataService.stopService();
    }
}