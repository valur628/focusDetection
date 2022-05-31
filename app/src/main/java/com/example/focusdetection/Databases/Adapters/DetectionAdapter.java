package com.example.focusdetection.Databases.Adapters;

import android.content.Context;

import com.example.focusdetection.Databases.EntityClass.ConcentrationTableEntity;
import com.example.focusdetection.Databases.EntityClass.MeasurementTableEntity;

import java.util.List;

public class DetectionAdapter{

    Context context;
    List<ConcentrationTableEntity> ConcentrationTableList;
    List<MeasurementTableEntity> MeasurementTableList;

    public DetectionAdapter(Context context, List<ConcentrationTableEntity> ConcentrationTableList, List<MeasurementTableEntity> MeasurementTableList) {
        this.context = context;
        this.ConcentrationTableList = ConcentrationTableList;
        this.MeasurementTableList = MeasurementTableList;
    }
}
