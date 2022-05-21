package com.example.focusdetection;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MeasurementTableDao {
    @Query("SELECT * FROM MeasurementTableEntity")
    List<MeasurementTableEntity> getAll();

    @Update
    void update(MeasurementTableEntity measurementTableEntity);

    @Insert
    void insert(MeasurementTableEntity measurementTableEntity);

    @Delete
    void delete(MeasurementTableEntity measurementTableEntity);

    @Query("DELETE FROM MeasurementTableEntity")
    void deleteAll();

}
