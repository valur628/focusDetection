package com.example.focusdetection;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "ConcentrationTableEntity")
public class ConcentrationTableEntity {
        @PrimaryKey
        public int conc_RecordNumberDB;

        @ColumnInfo(name = "conc_UseTimerDB")
        public String conc_UseTimerDB;

        @ColumnInfo(name = "conc_StartTimeDB")
        public Date conc_StartTimeDB;

        @ColumnInfo(name = "conc_EndTimeDB")
        public Date conc_EndTimeDB;
}
