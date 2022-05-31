package com.example.focusdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import com.example.focusdetection.Databases.DetectionRoomDatabase;
import com.example.focusdetection.Databases.EntityClass.*;

/**
 * Main activity of MediaPipe example apps.
 */
public class MainActivity extends AppCompatActivity {
    private TextView tv_ModeName, tv_TimerMode, tv_RestartText, tv_TimeCounter;
    private TextView tv_WaringSearchTop, tv_WaringSearchBottom;
    private TextView allViews;
    private String tempViews = "";
    //텍스트 뷰 목록

    private boolean startDialogCheck = true;
    //시작 다이얼로그 확인

    private int timer_hour, timer_minute, timer_second;
    //글로벌 시간
    private String text_hour, text_minute, text_second;
    //텍스트 상의 시간
    private String nowTime;
    //지금 시간
    private int totalTime = 0;
    //전체 시간
    private int globalTime = 0;
    //시작 이후의 시간
    private int concentrationTime = 0;
    //집중력 흐트러짐 시간

    private String UseTimerNameDB = "N/A";
    //템플릿 타이머 이름
    private String UseTimerTimeDB = "02:07:11";
    //템플릿 타이머 시간 (시간:분:초)
    private String[] divideTime;
    //문자열에서 분할된 시간
    private Timer timer = new Timer();
    private boolean pauseTimerCheck = false;
    //false = 흘러감, true = 멈춤

    LocalDate nowLocalDate = LocalDate.now();
    LocalTime nowLocalTime = LocalTime.now();
    String formatedNowLocalTime = nowLocalDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + nowLocalTime.format(DateTimeFormatter.ofPattern("HHmmss"));
    //날짜, 시간 & 문자열에 맞게 날짜+시간 변환
    LocalDateTime startMeasDateTime = LocalDateTime.now();
    LocalDateTime endMeasDateTime = LocalDateTime.now();
    //현재 측정 시간
    private boolean meas_check = true;
    //측정 시간 측정해도 되는지
    LocalDateTime startConcDateTime = LocalDateTime.now();
    LocalDateTime endConcDateTime = LocalDateTime.now();
    //현재 집중 시간
    private boolean conc_check = false;
    //집중 시간 측정해도 되는지

    private String waringSearchBottomText = "집중력 저하가 발견되지 않았습니다.";
    //집중력 저하 탐지 결과 아래쪽 텍스트

    List<ConcentrationTableEntity> ConcentrationTableList;
    List<MeasurementTableEntity> MeasurementTableList;
    int concSIZE;
    int measSIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);/*
        allViews = findViewById(R.id.dblist_id3);
        getDetectionData();
        if (startDialogCheck) {
            startDialog();
            startDialogCheck = false;
        }*/
    }
    public void onClicks(View view) {
        Intent intent = new Intent(this, DetectionActivity.class);
        startActivity(intent);
        finish();
    }
    public void onClicks2(View view) {
        Intent intent = new Intent(this, DetectionActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClicks3(View view) {
        Intent intent = new Intent(this, DetectionActivity.class);
        startActivity(intent);
        finish();
    }
/*
    public void onClicks2(View view) {
        tempViews = "";
        saveDataMeasurement();
        saveDataConcentration();
        getDetectionData();
        measSIZE = MeasurementTableList.size();
        concSIZE = ConcentrationTableList.size();
        for(int i = 0; i<measSIZE;i++) {
            viewAllDBMeas(i);
        }
        for(int i = 0; i<concSIZE;i++) {
            viewAllDBConc(i);
        }
        allViews.setText(tempViews);
        pauseTimerCheck = true;

    }
    public void onClicks3(View view) {
        tempViews = "";
        if(!pauseTimerCheck) {
            pauseTimerCheck = true;
        }
        else {
            pauseTimerCheck = false;
        }
        saveDataConcentration();
        getDetectionData();
        measSIZE = MeasurementTableList.size();
        concSIZE = ConcentrationTableList.size();
        for(int i = 0; i<measSIZE;i++) {
            viewAllDBMeas(i);
        }
        for(int i = 0; i<concSIZE;i++) {
            viewAllDBConc(i);
        }
        allViews.setText(tempViews);
    }

    private void getDetectionData() {
        MeasurementTableList = DetectionRoomDatabase.getDatabase(getApplicationContext()).getMeasurementTableDao().getAllData();
        ConcentrationTableList = DetectionRoomDatabase.getDatabase(getApplicationContext()).getConcentrationTableDao().getAllData();
    }
    private void viewAllDBMeas(int counter){
        tempViews += "[MES" + counter + " : " + MeasurementTableList.get(counter).getMeas_id() + " / ";
        tempViews += MeasurementTableList.get(counter).getMeas_RecordNumberDB() + " / ";
        tempViews += MeasurementTableList.get(counter).getMeas_UseTimerNameDB() + " / ";
        tempViews += MeasurementTableList.get(counter).getMeas_UseTimerTimeDB() + " / ";
        tempViews += MeasurementTableList.get(counter).getMeas_StartTimeDB() + " / ";
        tempViews += MeasurementTableList.get(counter).getMeas_EndTimeDB() + "]\n";
    }

    private void viewAllDBConc(int counter) {
        tempViews += "[CON" + counter + " : " + ConcentrationTableList.get(counter).getConc_id() + " / ";
        tempViews += ConcentrationTableList.get(counter).getConc_RecordNumberDB() + " / ";
        tempViews += ConcentrationTableList.get(counter).getConc_UseTimerNameDB() + " / ";
        tempViews += ConcentrationTableList.get(counter).getConc_UseTimerTimeDB() + " / ";
        tempViews += ConcentrationTableList.get(counter).getConc_StartTimeDB() + " / ";
        tempViews += ConcentrationTableList.get(counter).getConc_EndTimeDB() + "]\n";
    }
    private void saveDataMeasurement() { //여기가 측정 시간 저장
        String Meas_RecordNumberDB_txt = formatedNowLocalTime.trim();
        String Meas_UseTimerNameDB_txt = UseTimerNameDB;
        String Meas_UseTimerTimeDB_txt = UseTimerTimeDB;
        LocalDateTime Meas_StartTimeDB_txt = startMeasDateTime;
        endMeasDateTime = LocalDateTime.now();
        LocalDateTime Meas_EndTimeDB_txt = endMeasDateTime;

        MeasurementTableEntity modelMeasurementTable = new MeasurementTableEntity();
        modelMeasurementTable.setMeas_RecordNumberDB(Meas_RecordNumberDB_txt);
        modelMeasurementTable.setMeas_UseTimerNameDB(Meas_UseTimerNameDB_txt);
        modelMeasurementTable.setMeas_UseTimerTimeDB(Meas_UseTimerTimeDB_txt);
        modelMeasurementTable.setMeas_StartTimeDB(Meas_StartTimeDB_txt);
        modelMeasurementTable.setMeas_EndTimeDB(Meas_EndTimeDB_txt);
        DetectionRoomDatabase.getDatabase(getApplicationContext()).getMeasurementTableDao().insert(modelMeasurementTable);
        //MeasurementRoomDatabase.getDatabase(getApplicationContext()).getMeasurementTableDao().deleteAll(); 이건 삭제

        Toast.makeText(this, "측정 시간 저장", Toast.LENGTH_SHORT).show();
    }


    private void saveDataConcentration() { //여기가 감지 시간 저장
        String Conc_RecordNumberDB_txt = formatedNowLocalTime.trim();
        String Conc_UseTimerNameDB_txt = UseTimerNameDB;
        String Conc_UseTimerTimeDB_txt = UseTimerTimeDB;
        LocalDateTime Conc_StartTimeDB_txt = startConcDateTime;
        endConcDateTime = LocalDateTime.now();
        LocalDateTime Conc_EndTimeDB_txt = endConcDateTime;

        ConcentrationTableEntity modelConcentrationTable = new ConcentrationTableEntity();
        modelConcentrationTable.setConc_RecordNumberDB(Conc_RecordNumberDB_txt);
        modelConcentrationTable.setConc_UseTimerNameDB(Conc_UseTimerNameDB_txt);
        modelConcentrationTable.setConc_UseTimerTimeDB(Conc_UseTimerTimeDB_txt);
        modelConcentrationTable.setConc_StartTimeDB(Conc_StartTimeDB_txt);
        modelConcentrationTable.setConc_EndTimeDB(Conc_EndTimeDB_txt);
        DetectionRoomDatabase.getDatabase(getApplicationContext()).getConcentrationTableDao().insert(modelConcentrationTable);
        //MeasurementRoomDatabase.getDatabase(getApplicationContext()).getMeasurementTableDao().deleteAll(); 이건 삭제

        Toast.makeText(this, "집중 시간 저장", Toast.LENGTH_SHORT).show();
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            // 반복실행할 구문
            globalTime++;
            if(!pauseTimerCheck) {
                // 0초 이상이면
                if (timer_second != 0) {
                    //1초씩 감소
                    timer_second--;

                    // 0분 이상이면
                } else if (timer_minute != 0) {
                    // 1분 = 60초
                    timer_second = 60;
                    timer_second--;
                    timer_minute--;

                    // 0시간 이상이면
                } else if (timer_hour != 0) {
                    // 1시간 = 60분
                    timer_second = 60;
                    timer_minute = 60;
                    timer_second--;
                    timer_minute--;
                    timer_hour--;
                }

                //시, 분, 초가 10이하(한자리수) 라면
                // 숫자 앞에 0을 붙인다 ( 8 -> 08 )
                if (timer_second <= 9) {
                    text_second = "0" + timer_second;
                } else {
                    text_second = Integer.toString(timer_second);
                }

                if (timer_minute <= 9) {
                    text_minute = "0" + timer_minute;
                } else {
                    text_minute = Integer.toString(timer_minute);
                }

                if (timer_hour <= 9) {
                    text_hour = "0" + timer_hour;
                } else {
                    text_hour = Integer.toString(timer_minute);
                }
                nowTime = text_hour + ":" + text_minute + ":" + text_second;
                tv_TimeCounter.setText(nowTime);
            }
            // 시분초가 다 0이라면 toast를 띄우고 타이머를 종료한다..
            if (timer_hour == 0 && timer_minute == 0 && timer_second == 1) {
                //timerTask.cancel();//타이머 종료
                //timer.cancel();//타이머 종료
                //timer.purge();//타이머 종료
                //중간에 잠시 멈추는 건 타이머를 죽이는 게 아니라 타이머를 보기로만 잠시 멈춰두고 다시 시작할 때 시간을 새로 갱신
            }
        }
    };

    private void startDialog() {
        tv_TimeCounter = findViewById(R.id.time_counter_id2);
        //이건 자르던지 바꾸던지 하셈0
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("시작 전 준비")
                .setMessage("하단의 확인 버튼을 누르고 나서 정확히 10초 뒤에 집중력 감지가 실행됩니다. 10초 타이머가 흘러가는 순간부터 공부를 진행해주시면 됩니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        divideTime = UseTimerTimeDB.split(":");
                        timer_hour = Integer.parseInt(divideTime[0]);
                        timer_minute = Integer.parseInt(divideTime[1]);
                        timer_second = Integer.parseInt(divideTime[2]);
                        totalTime = ((((timer_hour * 60) + timer_minute) * 60) + timer_second) * 1000;
                        timer.scheduleAtFixedRate(timerTask, 5000, 1000); //Timer 실행
                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }*/
}