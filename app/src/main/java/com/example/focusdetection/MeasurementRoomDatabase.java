package com.example.focusdetection;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



@Database(entities = {MeasurementTableEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverters.class})
public abstract class MeasurementRoomDatabase extends RoomDatabase {

    public abstract MeasurementTableDao measurementTableDao();
    public static final int NUMBER_OF_THREADS = 4;

    private static volatile MeasurementRoomDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MeasurementRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MeasurementRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeasurementRoomDatabase.class, "measurement_database")
                            .addCallback(setInitialRoomDatabaseCallback) // modified
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // modified
    private static final RoomDatabase.Callback setInitialRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    long now = System.currentTimeMillis();
                    super.onCreate(db);
                    databaseWriteExecutor.execute(() -> {
                        MeasurementTableDao measurementTableDao = INSTANCE.measurementTableDao();
                        measurementTableDao.deleteAll();

                        MeasurementTableEntity measurementTableEntity = new MeasurementTableEntity(1, "220521000001", "student", LocalDateTime.now(), LocalDateTime.now());
                        measurementTableDao.insert(measurementTableEntity);

                        measurementTableEntity = new MeasurementTableEntity(2, "220521000001", "student", LocalDateTime.now(), LocalDateTime.now());
                        measurementTableDao.insert(measurementTableEntity);
                    });
                }
            };
}