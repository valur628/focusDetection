package com.example.focusdetection.Databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.focusdetection.Databases.Converters.DateConverters;
import com.example.focusdetection.Databases.DaoClass.ConcentrationTableDao;
import com.example.focusdetection.Databases.EntityClass.ConcentrationTableEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
@Database(entities = {ConcentrationTableEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverters.class})*/
public abstract class ConcentrationRoomDatabase extends RoomDatabase {
/*
    public abstract ConcentrationTableDao getConcentrationTableDao();
    public static final int NUMBER_OF_THREADS = 4;
    private static volatile ConcentrationRoomDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static ConcentrationRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ConcentrationRoomDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        ConcentrationRoomDatabase.class, "concentration_database")
                        .allowMainThreadQueries() // modified
                        .build();
            }
        }
        return INSTANCE;
    }*/
}
