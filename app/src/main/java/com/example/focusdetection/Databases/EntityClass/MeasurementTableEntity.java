package com.example.focusdetection.Databases.EntityClass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity(tableName = "MeasurementTable")
public class MeasurementTableEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int meas_id;

    @ColumnInfo
    private String meas_RecordNumberDB;

    @ColumnInfo
    private String meas_UseTimerDB;

    @ColumnInfo
    private LocalDateTime meas_StartTimeDB;

    @ColumnInfo
    private LocalDateTime meas_EndTimeDB;

    public MeasurementTableEntity() {
    }

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
    public void setMeas_id(int meas_id) {
        this.meas_id = meas_id;
    }

    public String getMeas_RecordNumberDB() { return meas_RecordNumberDB; }
    public void setMeas_RecordNumberDB(String meas_RecordNumberDB) {
        this.meas_RecordNumberDB = meas_RecordNumberDB;
    }

    public String getMeas_UseTimerDB() { return meas_UseTimerDB; }
    public void setMeas_UseTimerDB(String meas_UseTimerDB) {
        this.meas_UseTimerDB = meas_UseTimerDB;
    }

    public LocalDateTime getMeas_StartTimeDB() {  return meas_StartTimeDB; }
    public void setMeas_StartTimeDB(LocalDateTime meas_StartTimeDB) {
        this.meas_StartTimeDB = meas_StartTimeDB;
    }

    public LocalDateTime getMeas_EndTimeDB() {
        return meas_EndTimeDB;
    }
    public void setMeas_EndTimeDB(LocalDateTime meas_EndTimeDB) {
        this.meas_EndTimeDB = meas_EndTimeDB;
    }
}