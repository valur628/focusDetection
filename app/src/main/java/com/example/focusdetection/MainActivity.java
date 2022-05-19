package com.example.focusdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClicks(View view) {
        Intent intent = new Intent(this, DetectionActivity.class);
        startActivity(intent);
        finish();
    }
}