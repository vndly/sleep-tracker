package com.mauriciotogneri.sleeptracker;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class FileLogger
{
    private static final String COLUMN_SEPARATOR = ",";
    private DecimalFormat decimalFormat = new DecimalFormat("#.####");
    private BufferedWriter bufferedWriter;

    public FileLogger()
    {
        try
        {
            SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            File file = new File(Environment.getExternalStorageDirectory() + "/" + sourceDateFormat.format(System.currentTimeMillis()) + ".csv");

            if (file.createNewFile())
            {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                writeLine("Time", "X", "Y", "Z");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void log(long timestamp, float x, float y, float z)
    {
        writeLine(String.valueOf(timestamp), decimalFormat.format(x), decimalFormat.format(y), decimalFormat.format(z));
    }

    private void writeLine(String time, String x, String y, String z)
    {
        writeLine(String.format("%s%s%s%s%s%s%s", time, COLUMN_SEPARATOR, x, COLUMN_SEPARATOR, y, COLUMN_SEPARATOR, z));
    }

    private void writeLine(String text)
    {
        if (bufferedWriter != null)
        {
            try
            {
                bufferedWriter.write(String.format("%s%n", text));
                bufferedWriter.flush();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void close()
    {
        try
        {
            bufferedWriter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}