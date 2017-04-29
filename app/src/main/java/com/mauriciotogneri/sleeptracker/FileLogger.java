package com.mauriciotogneri.sleeptracker;

import android.os.Environment;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

public class FileLogger
{
    private static final String COLUMN_SEPARATOR = ",";
    private DecimalFormat decimalFormat = new DecimalFormat("#.###");
    private BufferedWriter bufferedWriter;

    public FileLogger()
    {
        try
        {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss");
            String fileName = String.format("%s.csv", dateTimeFormatter.print(DateTime.now()));
            File file = new File(String.format("%s/%s", Environment.getExternalStorageDirectory(), fileName));

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
        writeLine(timestamp(timestamp), decimalFormat.format(x), decimalFormat.format(y), decimalFormat.format(z));
    }

    private String timestamp(long timestamp)
    {
        long inSeconds = (timestamp / 1000);
        long plus2Hours = inSeconds + 7200;

        return String.valueOf(plus2Hours);
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