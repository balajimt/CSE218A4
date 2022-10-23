package CSE218;


import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

public class HeartRateListener implements SensorEventListener{
    Context context;
    SensorManager sensorManager;
    Sensor mHeartSensor;
    private final TextView tvHeartRate;

    private double heartrate;

    public HeartRateListener(Context context, TextView tvHeartRate) {
        this.context = context;
        this.tvHeartRate = tvHeartRate;
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
            Log.i("Heartrate Sensor", "Found");
            mHeartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        } else {
            Log.i("Heartrate Sensor", "Not found");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heartrate = event.values[0];
            this.tvHeartRate.setText("" + heartrate + " bpm");
            Log.i("heartSensor", "onChange: " + heartrate);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public double getHeartRate() {
        return heartrate;
    }

    public void startSensor() {
        sensorManager.registerListener(this, mHeartSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
