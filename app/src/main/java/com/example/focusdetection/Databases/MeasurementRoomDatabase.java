package com.example.focusdetection.Databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.focusdetection.Databases.Converters.DateConverters;
import com.example.focusdetection.Databases.DaoClass.MeasurementTableDao;
import com.example.focusdetection.Databases.EntityClass.MeasurementTableEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {MeasurementTableEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverters.class})
public abstract class MeasurementRoomDatabase extends RoomDatabase {

    public abstract MeasurementTableDao getMeasurementTableDao();
    public static final int NUMBER_OF_THREADS = 4;
    private static volatile MeasurementRoomDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MeasurementRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
                synchronized (MeasurementRoomDatabase.class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeasurementRoomDatabase.class, "measurement_database")
                            .allowMainThreadQueries() // modified
                            .build();
                }
            }
        return INSTANCE;
    }
}