package com.example.focusdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.Timer;


import com.example.focusdetection.Databases.DetectionRoomDatabase;
import com.example.focusdetection.Databases.EntityClass.*;

/**
 * Main activity of MediaPipe example apps.
 */
public class MainActivity extends AppCompatActivity {
    private TextView tv_ModeName, tv_TimerMode, tv_RestartText, tv_TimeCounter;
    private TextView tv_WaringSearchTop, tv_WaringSearchBottom;
    //텍스트 뷰 목록

    private boolean startDialogCheck = true;
    //시작 다이얼로그 확인

    private int detectionModeNumber = 2;
    //집중도 측정 모드 (1 = 약함 모드, 2 = 중간 모드, 3 = 강함 모드)

    private int timer_hour, timer_minute, timer_second;
    //글로벌 시간
    private String text_hour, text_minute, text_second;
    //텍스트 상의 시간
    private String nowTime;
    //지금 시간
    private int totalTime;
    //전체 시간

    private String timerNameDB;
    //템플릿 타이머 이름
    private String SetTimeDB = "0:0:12";
    //템플릿 타이머 시간 (시간:분:초)
    private String[] divideTime;
    //문자열에서 분할된 시간
    private Timer timer = new Timer();

    private String waringSearchBottomText = "집중력 저하가 발견되지 않았습니다.";
    //집중력 저하 탐지 결과 아래쪽 텍스트

    private String UseTimerNameDB = "N/A";
    //템플릿 타이머 이름
    private String UseTimerTimeDB = "02:07:11";
    //템플릿 타이머 시간 (시간:분:초)

    LocalDate nowLocalDate = LocalDate.now();
    LocalTime nowLocalTime = LocalTime.now();
    String formatedNowLocalTime = nowLocalDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + nowLocalTime.format(DateTimeFormatter.ofPattern("HHmmss"));
    //날짜, 시간 & 문자열에 맞게 날짜+시간 변환
    LocalDateTime startMeasDateTime = LocalDateTime.now();
    LocalDateTime endMeasDateTime = LocalDateTime.now();
    //현재 측정 시간
    LocalDateTime startConcDateTime = LocalDateTime.now();
    LocalDateTime endConcDateTime = LocalDateTime.now();
    //현재 집중 시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //saveData();
    }
    public void onClicks(View view) {
        Intent intent = new Intent(this, DetectionActivity.class);
        startActivity(intent);
        finish();
    }

    /*private void saveData() { //여기가 저장

        String Meas_RecordNumberDB_txt = formatedNowLocalTime.trim();
        String Meas_UseTimerDB_txt = "02:07:11".trim();
        LocalDateTime Meas_StartTimeDB_txt = currentNowDateTime;
        LocalDateTime Meas_EndTimeDB_txt = currentNowDateTime.plusDays(25);


        MeasurementTableEntity modelTable = new MeasurementTableEntity();
        modelTable.setMeas_RecordNumberDB(Meas_RecordNumberDB_txt);
        modelTable.setMeas_UseTimerDB(Meas_UseTimerDB_txt);
        modelTable.setMeas_StartTimeDB(Meas_StartTimeDB_txt);
        modelTable.setMeas_EndTimeDB(Meas_EndTimeDB_txt);
        MeasurementRoomDatabase.getDatabase(getApplicationContext()).getMeasurementTableDao().insert(modelTable);
        MeasurementRoomDatabase.getDatabase(getApplicationContext()).getMeasurementTableDao().deleteAll();

        Toast.makeText(this, "Data Successfully Saved", Toast.LENGTH_SHORT).show();
    }*/
}