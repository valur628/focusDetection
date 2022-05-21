package com.example.focusdetection;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity(tableName = "MeasurementTableEntity")
public class MeasurementTableEntity {

    @PrimaryKey(autoGenerate = true)
    public int meas_id;

    @ColumnInfo(name = "meas_RecordNumberDB")
    public String meas_RecordNumberDB;

    @ColumnInfo(name = "meas_UseTimerDB")
    public String meas_UseTimerDB;

    @ColumnInfo(name = "meas_StartTimeDB")
    public LocalDateTime meas_StartTimeDB;

    @ColumnInfo(name = "meas_EndTimeDB")
    public LocalDateTime meas_EndTimeDB;

    public MeasurementTableEntity() { }
    public MeasurementTableEntity(int meas_id, String meas_RecordNumber, String meas_UseTimer, LocalDateTime meas_StartTime, LocalDateTime meas_EndTime) {
        this.meas_id = meas_id;
        this.meas_RecordNumberDB = meas_RecordNumber;
        this.meas_UseTimerDB = meas_UseTimer;
        this.meas_StartTimeDB = meas_StartTime;
        this.meas_EndTimeDB = meas_EndTime;
    }

    public int getMeas_id() {
        return meas_id;
    }
    public String getMeas_RecordNumberDB() {
        return meas_RecordNumberDB;
    }
    public String getMeas_UseTimerDB() {
        return meas_UseTimerDB;
    }
    public LocalDateTime getMeas_StartTimeDB() {
        return meas_StartTimeDB;
    }
    public LocalDateTime getMeas_EndTimeDB() {
        return meas_EndTimeDB;
    }
}