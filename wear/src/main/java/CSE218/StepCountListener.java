package CSE218;


import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StepCountListener implements SensorEventListener{
    Context context;
    SensorManager sensorManager;
    Sensor mStepSensor;

    private double mStepsCount = 0.0;
    private TextView txtCalculatedStepCount;
    private float rawAccelValues[] = new float[3];
    private float[] gravity = {0, 0, 0};

    // Step detection variables
    private int readingCount = 0;
    private int peakCount = 0;
    private int stepCount = 0;
    private int calculatedStepCount = 0;
    private int initialCounterValue = 0;
    private int LAG_SIZE = 5;
    private int DATA_SAMPLING_SIZE = 15;
    private List<Double> zscoreCalculationValues = new ArrayList<>();

    public StepCountListener(Context context, TextView txtCalculatedStepCount) {
        this.context = context;
        this.txtCalculatedStepCount = txtCalculatedStepCount;
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            Log.i("Accelerometer", "Found");
            mStepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Log.i("Accelerometer", "Not found");
        }
    }


    float[] IsolateGravity(float[] sensorValues) {
        final float alpha = 0.8f;
        float[] acceleration = {0, 0, 0};
        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorValues[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorValues[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorValues[2];

        // Remove the gravity contribution with the high-pass filter.
        acceleration[0] = sensorValues[0] - gravity[0];
        acceleration[1] = sensorValues[1] - gravity[1];
        acceleration[2] = sensorValues[2] - gravity[2];

        return acceleration;
    }

    int DetectPeak(List<Double> inputs, int lag, Double threshold, Double influence) {
        int peaksDetected = 0;
        //init stats instance
        SummaryStatistics stats = new SummaryStatistics();

        //the results (peaks, 1 or -1) of our algorithm
        ArrayList<Integer> signals = new ArrayList<>(Collections.nCopies(inputs.size(), 0));
        //filter out the signals (peaks) from our original list (using influence arg)
        ArrayList<Double> filteredY = new ArrayList<>(Collections.nCopies(inputs.size(), 0d));
        //the current average of the rolling window
        ArrayList<Double> avgFilter = new ArrayList<>(Collections.nCopies(inputs.size(), 0.0d));
        //the current standard deviation of the rolling window
        ArrayList<Double> stdFilter = new ArrayList<>(Collections.nCopies(inputs.size(), 0.0d));

        //init avgFilter and stdFilter
        for (int i = 0; i < lag; i++) {
            stats.addValue(inputs.get(i));
            filteredY.add(inputs.get(i));
        }
        avgFilter.set(lag - 1, stats.getMean());
        stdFilter.set(lag - 1, stats.getStandardDeviation());
        stats.clear();

        peakCount = peakCount + LAG_SIZE;
        for (int i = lag; i < inputs.size(); i++) {
            peakCount = peakCount + 1;
            if (Math.abs(inputs.get(i) - avgFilter.get(i - 1)) > threshold * stdFilter.get(i - 1)) {
                //this is a signal (i.e. peak), determine if it is a positive or negative signal
                if (inputs.get(i) > avgFilter.get(i - 1)) {
                    signals.set(i, 1);
                    if (inputs.get(i) > 1.5 && inputs.get(i) < 4.0) {
                        peaksDetected = peaksDetected + 1;
                    }
                } else {
                    signals.set(i, -1);
                }
                //filter this signal out using influence
                filteredY.set(i, (influence * inputs.get(i)) + ((1 - influence) * filteredY.get(i - 1)));
            } else {
                //ensure this signal remains a zero
                signals.set(i, 0);
                //ensure this value is not filtered
                filteredY.set(i, inputs.get(i));
            }
            //update rolling average and deviation
            for (int j = i - lag; j < i; j++)
            {
                stats.addValue(filteredY.get(j));
            }
            avgFilter.set(i, stats.getMean());
            stdFilter.set(i, stats.getStandardDeviation());

        }
        return peaksDetected;
    }

    // Reads accelerometer data to detect a step.
    void InferStepFromAccelerometerData(SensorEvent event) {
        try {
            readingCount = readingCount + 1;

            rawAccelValues[0] = event.values[0];
            rawAccelValues[1] = event.values[1];
            rawAccelValues[2] = event.values[2];
            rawAccelValues = IsolateGravity(rawAccelValues);

            double rawMagnitude = Math.sqrt(
                    rawAccelValues[0] * rawAccelValues[0] +
                            rawAccelValues[1] * rawAccelValues[1] +
                            rawAccelValues[2] * rawAccelValues[2]);

            if (zscoreCalculationValues.size() < DATA_SAMPLING_SIZE) {
                zscoreCalculationValues.add(rawMagnitude);
            } else if (zscoreCalculationValues.size() == DATA_SAMPLING_SIZE) {
                calculatedStepCount = calculatedStepCount +
                        this.DetectPeak(
                                zscoreCalculationValues, LAG_SIZE, 0.30d, 0.2d);
                // Update value here
                this.txtCalculatedStepCount.setText("Steps: " + calculatedStepCount);
                Log.i("StepSensor", "onChange: " + calculatedStepCount);
                zscoreCalculationValues.clear();
                zscoreCalculationValues.add(rawMagnitude);
            }
        } catch (Exception ex) {
            Log.e("Ex", ex.getMessage());
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            InferStepFromAccelerometerData(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void startSensor() {
        sensorManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
