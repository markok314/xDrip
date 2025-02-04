package com.eveningoutpost.dexdrip.utilitymodels;

// jamorham

import android.app.Activity;

import com.eveningoutpost.dexdrip.G5Model.Ob1G5StateMachine;
import com.eveningoutpost.dexdrip.GcmActivity;
import com.eveningoutpost.dexdrip.Home;
import com.eveningoutpost.dexdrip.models.BgReading;
import com.eveningoutpost.dexdrip.models.BloodTest;
import com.eveningoutpost.dexdrip.models.Calibration;
import com.eveningoutpost.dexdrip.models.DesertSync;
import com.eveningoutpost.dexdrip.models.JoH;
import com.eveningoutpost.dexdrip.models.Sensor;
import com.eveningoutpost.dexdrip.Services.ActivityRecognizedService;
import com.eveningoutpost.dexdrip.Services.G5BaseService;
import com.eveningoutpost.dexdrip.cgm.medtrum.MedtrumCollectionService;
import com.eveningoutpost.dexdrip.ui.activities.DatabaseAdmin;
import com.eveningoutpost.dexdrip.utils.DexCollectionType;
import com.eveningoutpost.dexdrip.utils.SdcardImportExport;

import static com.eveningoutpost.dexdrip.Home.get_engineering_mode;
import static com.eveningoutpost.dexdrip.Home.staticRefreshBGCharts;

public class VoiceCommands {


    public static void processVoiceCommand(String allWords, Activity mActivity) {
        if (allWords.contentEquals("delete last calibration")
                || allWords.contentEquals("clear last calibration")) {
            Calibration.clearLastCalibration();
        } else if (allWords.contentEquals("force google reboot")) {
            SdcardImportExport.forceGMSreset();
        } else if (allWords.contentEquals("enable engineering mode")) {
            Pref.setBoolean("engineering_mode", true);
            JoH.static_toast_long("Engineering mode enabled - be careful");
        } else if (get_engineering_mode() && allWords.contentEquals("enable fake data source")) {
            Pref.setString(DexCollectionType.DEX_COLLECTION_METHOD, DexCollectionType.Mock.toString());
            JoH.static_toast_long("YOU ARE NOW USING FAKE DATA!!!");
        } else if (get_engineering_mode() && allWords.equals("break fake data source")) {
            JoH.static_toast_long("Breaking fake data source");
            MockDataSource.breakRaw();
        } else if (get_engineering_mode() && allWords.equals("repair fake data source")) {
            JoH.static_toast_long("Repairing fake data source");
            MockDataSource.fixRaw();

        } else if (allWords.contentEquals("hard reset transmitter")) {
            G5BaseService.setHardResetTransmitterNow();
            JoH.static_toast_long("Will attempt to reset transmitter on next poll!! Can take 15 minutes to process");
        } else if (allWords.contentEquals("reset heart rate sync")) {
            PersistentStore.setLong("nightscout-rest-heartrate-synced-time", 0);
            JoH.static_toast_long("Cleared heart rate sync data");
        } else if (allWords.contentEquals("reset step count sync")) {
            PersistentStore.setLong("nightscout-rest-steps-synced-time", 0);
            JoH.static_toast_long("Cleared step count sync data");
        } else if (allWords.contentEquals("reset motion count sync")) {
            PersistentStore.setLong("nightscout-rest-motion-synced-time", 0);
            JoH.static_toast_long("Cleared motion count sync data");
        } else if (allWords.contentEquals("vehicle mode test")) {
            ActivityRecognizedService.spoofActivityRecogniser(mActivity, JoH.tsl() + "^" + 0);
            staticRefreshBGCharts();
        } else if (allWords.contentEquals("vehicle mode quit")) {
            ActivityRecognizedService.spoofActivityRecogniser(mActivity, JoH.tsl() + "^" + 3);
            staticRefreshBGCharts();
        } else if (allWords.contentEquals("vehicle mode walk")) {
            ActivityRecognizedService.spoofActivityRecogniser(mActivity, JoH.tsl() + "^" + 2);
            staticRefreshBGCharts();
        } else if (allWords.equals("delete random glucose data")) {
            BgReading.deleteRandomData();
            JoH.static_toast_long("Deleting random glucose data");
            staticRefreshBGCharts();
        } else if (allWords.contentEquals("delete selected glucose meter") || allWords.contentEquals("delete selected glucose metre")) {
            Pref.setString("selected_bluetooth_meter_address", "");
        } else if (allWords.contentEquals("delete all finger stick data") || (allWords.contentEquals("delete all fingerstick data"))) {
            BloodTest.cleanup(-100000);
        } else if (allWords.contentEquals("delete all persistent store")) {
            SdcardImportExport.deletePersistentStore();
        } else if (allWords.contentEquals("delete uploader queue")) {
            UploaderQueue.emptyQueue();
        } else if (allWords.contentEquals("clear battery warning")) {
            try {
                final Sensor sensor = Sensor.currentSensor();
                if (sensor != null) {
                    sensor.latest_battery_level = 0;
                    sensor.save();
                }
            } catch (Exception e) {
                // do nothing
            }
        } else if (get_engineering_mode() && allWords.contentEquals("enable dead sensor")) {
            Pref.setBoolean("allow_testing_with_dead_sensor", true);
            JoH.static_toast_long("testing with dead sensor enabled - be careful");
        } else if (allWords.contentEquals("disable dead sensor")) {
            Pref.setBoolean("allow_testing_with_dead_sensor", false);
            JoH.static_toast_long("testing with dead sensor disabled");
        }

        switch (allWords) {
            case "restart g5 session":
                Ob1G5StateMachine.restartSensorWithTimeTravel();
                JoH.static_toast_long("Attempting to restart sensor session");
                break;
            case "restart g5 session nearly ended":
                Ob1G5StateMachine.restartSensorWithTimeTravel((JoH.tsl() - Constants.DAY_IN_MS * 1) + Constants.MINUTE_IN_MS * 20);
                break;
            case "stop g5 session":
                Ob1G5StateMachine.stopSensor();
                JoH.static_toast_long("Attempting to stop sensor session");
                break;
            case "start g5 session":
                Ob1G5StateMachine.startSensor(JoH.tsl());
                JoH.static_toast_long("Attempting to start sensor session");
                break;
            case "clear last update check time":
                UpdateActivity.clearLastCheckTime();
                JoH.static_toast_long(allWords);
                break;
            case "clean up excessive high readings":
                BgReading.cleanupOutOfRangeValues();
                Home.staticRefreshBGChartsOnIdle();
                break;
            case "stop sensor on master":
                JoH.static_toast_long(allWords);
                GcmActivity.push_stop_master_sensor();
                break;
            case "start sensor on master":
                JoH.static_toast_long(allWords);
                GcmActivity.push_start_master_sensor();
                break;
            case "test medtrum calibrate":
                MedtrumCollectionService.calibratePing();
                break;
            case "delete all desert sync data":
                JoH.static_toast_long("deleted all desert sync data");
                DesertSync.deleteAll();
                break;
            case "start usb configuration":
                JoH.startActivity(MtpConfigureActivity.class);
                break;
            case "database administration":
                JoH.startActivity(DatabaseAdmin.class);
                break;
        }

    }


}
