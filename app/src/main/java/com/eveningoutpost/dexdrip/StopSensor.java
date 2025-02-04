package com.eveningoutpost.dexdrip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.eveningoutpost.dexdrip.G5Model.Ob1G5StateMachine;
import com.eveningoutpost.dexdrip.models.Calibration;
import com.eveningoutpost.dexdrip.models.JoH;
import com.eveningoutpost.dexdrip.models.Sensor;
import com.eveningoutpost.dexdrip.utilitymodels.AlertPlayer;
import com.eveningoutpost.dexdrip.utilitymodels.CollectionServiceStarter;
import com.eveningoutpost.dexdrip.utilitymodels.Inevitable;
import com.eveningoutpost.dexdrip.calibrations.PluggableCalibration;
import com.eveningoutpost.dexdrip.utils.ActivityWithMenu;
import static com.eveningoutpost.dexdrip.xdrip.gs;

public class StopSensor extends ActivityWithMenu {
   public Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!Sensor.isActive()) {
            Intent intent = new Intent(this, StartNewSensor.class);
            startActivity(intent);
            finish();
        } else {
            JoH.fixActionBar(this);
            setContentView(R.layout.activity_stop_sensor);
            button = (Button)findViewById(R.id.stop_sensor);
            addListenerOnButton();
        }
    }

    @Override
    public String getMenuName() {
        return getString(R.string.stop_sensor);
    }

    public void addListenerOnButton() {

        button = (Button)findViewById(R.id.stop_sensor);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stop();
                JoH.startActivity(Home.class);
                finish();
            }

        });
    }

    public synchronized static void stop() {
        Sensor.stopSensor();
        Inevitable.task("stop-sensor",1000, Sensor::stopSensor);
        AlertPlayer.getPlayer().stopAlert(xdrip.getAppContext(), true, false);

        JoH.static_toast_long(gs(R.string.sensor_stopped));
        JoH.clearCache();
        LibreAlarmReceiver.clearSensorStats();
        PluggableCalibration.invalidateAllCaches();

        Ob1G5StateMachine.stopSensor();

        CollectionServiceStarter.restartCollectionServiceBackground();
        Home.staticRefreshBGCharts();
    }

    public void resetAllCalibrations(View v) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(gs(R.string.are_you_sure));
        builder.setMessage(gs(R.string.do_you_want_to_delete_and_reset_the_calibrations_for_this_sensor));

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calibration.invalidateAllForSensor();
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();


    }
}
