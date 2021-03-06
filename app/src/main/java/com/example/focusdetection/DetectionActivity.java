package com.example.focusdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.SurfaceTexture;

import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.focusdetection.Databases.DetectionRoomDatabase;
import com.example.focusdetection.Databases.EntityClass.ConcentrationTableEntity;
import com.example.focusdetection.Databases.EntityClass.MeasurementTableEntity;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.glutil.EglManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DetectionActivity extends AppCompatActivity {
    private static final String TAG = "DetectionActivity";
    private static final String BINARY_GRAPH_NAME = "face_mesh_mobile_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "multi_face_landmarks";
    private static final String INPUT_NUM_FACES_SIDE_PACKET_NAME = "num_faces";
    private static final int NUM_FACES = 1;
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private SurfaceView previewDisplayView;
    // Creates and manages an {@link EGLContext}.
    private EglManager eglManager;
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private FrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private ExternalTextureConverter converter;
    // ApplicationInfo for retrieving metadata defined in the manifest.
    private ApplicationInfo applicationInfo;
    // Handles camera access via the {@link CameraX} Jetpack support library.
    private CameraXPreviewHelper cameraHelper;

    Handler ui_Handler = null;
    //UI ????????? ??? ?????????

    private TextView tv_ModeName, tv_TimerName, tv_RestartText, tv_TimeCounter;
    private TextView tv_WaringSearchTop, tv_WaringSearchBottom;
    //????????? ??? ??????

    private boolean startDialogCheck = true;
    //????????? ??????????????? ?????? ??????
    private boolean startUIHandlerCheck = true;
    //ui ????????? ?????? ??????
    private boolean tv_WaringSearchTopCheck = true;
    //tv_WaringSearchTop UI ?????? ??????
    private int finalStopCheck = 0;
    //?????? ?????? ?????? ??????
    //0 ???????????? ??????, 1 ?????? ??????, 2 ??????

    private int detectionModeNumber = 2;
    private String detectionModeString = "?????? ??????";
    //????????? ?????? ?????? (1 = ?????? ??????, 2 = ?????? ??????, 3 = ?????? ??????)
    private double[][] LandmarkJudgmentConditions = new double[3][4];

    private int timer_hour, timer_minute, timer_second;
    //????????? ??????
    private String text_hour, text_minute, text_second;
    //????????? ?????? ??????
    private String nowTime;
    //?????? ??????
    private int totalTime = 0;
    //?????? ??????
    private int globalTime = 0;
    //?????? ????????? ??????
    private int concentrationTime = 0;
    //????????? ???????????? ??????

    private String UseTimerNameDB = "NoNameTimer";
    //????????? ????????? ??????
    private String UseTimerTimeDB = "00:00:16";
    //????????? ????????? ?????? (??????:???:???)
    private String[] divideTime;
    //??????????????? ????????? ??????
    private Timer timer = new Timer();
    private boolean pauseTimerCheck = false;
    //false = ?????????, true = ??????

    LocalDate nowLocalDate = LocalDate.now();
    LocalTime nowLocalTime = LocalTime.now();
    String formatedNowLocalTime = nowLocalDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + nowLocalTime.format(DateTimeFormatter.ofPattern("HHmmss"));
    //??????, ?????? & ???????????? ?????? ??????+?????? ??????

    LocalDateTime startMeasDateTime = LocalDateTime.now();
    LocalDateTime endMeasDateTime = LocalDateTime.now();
    //?????? ?????? ??????
    private boolean meas_check = true;
    //?????? ?????? ???????????? ?????????

    LocalDateTime startConcDateTime = LocalDateTime.now();
    LocalDateTime endConcDateTime = LocalDateTime.now();
    //?????? ?????? ??????
    private boolean conc_check = true;
    //?????? ?????? ???????????? ?????????

    private String waringSearchBottomText = "????????? ????????? ???????????? ???????????????.";
    //????????? ?????? ?????? ?????? ????????? ?????????

    private float leftEyePoint_blink_1, leftEyePoint_blink_2;
    //?????? ??? ???????????? ???????????? ?????????
    private float rightEyePoint_blink_1, rightEyePoint_blink_2;
    //????????? ??? ???????????? ???????????? ?????????
    private float ratioPoint_1a, ratioPoint_1b, ratioPoint_2a, ratioPoint_2b;
    // ??? ???????????? ?????? ????????? ?????? ????????? ?????? (??????, ?????????)
    private float leftRatioMeasurement_blink, rightRatioMeasurement_blink;
    // ??? ?????? ????????? ??????
    private boolean eye_blink, eye_open;
    //?????? ??? ???????????? ??????

    private float leftIrisPoint_side_1, leftIrisPoint_side_2, leftIrisPoint_side_3;
    //?????? ??? ????????? ???????????? ?????????
    private float rightIrisPoint_side_1, rightIrisPoint_side_2, rightIrisPoint_side_3;
    //????????? ??? ????????? ???????????? ?????????
    private float leftRatioMeasurement_corner1, leftRatioMeasurement_corner2, rightRatioMeasurement_corner1, rightRatioMeasurement_corner2;
    // ??? ?????? ????????? ??????
    private boolean iris_corner, iris_center;
    //?????? ??? ??????????????? ??????

    private float leftCheekPoint_side_1, rightCheekPoint_side_1;
    //?????? ?????? ????????? ??? ???????????? ?????????
    private float cheekRatioMeasurement_side;
    //?????? ?????? ?????? ??? ??????
    private float centerHeadPoint_angle_x, centerHeadPoint_angle_z;
    //?????? ?????? ????????? ??? ???????????? ?????????
    private float centerForeheadPoint_angle_x, centerForeheadPoint_angle_z;
    //?????? ?????? ????????? ?????? ???????????? ?????????
    private boolean head_side, head_middle;
    //?????? ??????????????? ??????????????? ??????

    //List<TimerTableEntity> TimerTemplateTableList;
    //????????? ????????? DB??? ???????????? ?????? ?????????
    //int TimerTemplateTableSize;
    //????????? ????????? DB??? ??????
    boolean ui_HandlerCheck = true;
    //UI ????????? ?????????

    Point ap1 = new Point();
    Point ap2 = new Point();
    Point ap3 = new Point();
    //?????? ?????? ????????? ?????? ?????? 3???

    private float apResult;

    class Point {
        float x;
        float z;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutResId());

        //TimerTemplateTableList = TimerDatabase.getDatabase(getApplicationContext()).getTimerTableDao().getAllData();
        //TimerTemplateTableSize = TimerTemplateTableList.size() - 1;
        //UseTimerNameDB = TimerTemplateTableList.get(TimerTemplateTableList.size()).getTime_TimerNameDB();
        //UseTimerTimeDB = TimerTemplateTableList.get(TimerTemplateTableList.size()).getTime_SetTimeDB();
        //?????? ?????? ????????? ???????????? ????????? -1 ????????????

        tv_ModeName = findViewById(R.id.mode_name_id);
        tv_TimerName = findViewById(R.id.timer_name_id);
        tv_RestartText = findViewById(R.id.restart_text_id);
        tv_WaringSearchTop = findViewById(R.id.waring_search_top_id);
        tv_WaringSearchBottom = findViewById(R.id.waring_search_bottom_id);

        eye_blink = true;
        eye_open = true;
        iris_corner = true;
        iris_center = true;
        head_side = true;
        head_middle = true;

        //tv.setText("000");
        if (startDialogCheck) {
            startDialog();
            startDialogCheck = false;
        }
        try {
            applicationInfo =
                    getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }

        //tv.setText("111");
        previewDisplayView = new SurfaceView(this);
        setupPreviewDisplayView();
        //tv.setText("222");

        AndroidAssetUtil.initializeNativeAssetManager(this);
        eglManager = new EglManager(null);
        //tv.setText("333");
        processor =
                new FrameProcessor(
                        this,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        processor
                .getVideoSurfaceOutput()
                .setFlipY(FLIP_FRAMES_VERTICALLY);

        //tv.setText("444");
        PermissionHelper.checkAndRequestCameraPermissions(this);
        //tv.setText("555");
        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        //tv.setText("666");
        Map<String, Packet> inputSidePackets = new HashMap<>();
        //tv.setText("777");
        inputSidePackets.put(INPUT_NUM_FACES_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_FACES));
        //tv.setText("888");
        processor.setInputSidePackets(inputSidePackets);
        //tv.setText("999");

        ap1.x = 540f;
        ap1.z = 0f;
        ap3.x = 540f;
        ap3.z = 1080f;
        //tv.setText("00000");

        if(detectionModeNumber == 1){
            LandmarkJudgmentConditions[0][0] = 100f;
            LandmarkJudgmentConditions[0][1] = 3.5;
            LandmarkJudgmentConditions[0][2] = -3.5;
            LandmarkJudgmentConditions[1][0] = 0.14;
            LandmarkJudgmentConditions[2][0] = -0.60;
            LandmarkJudgmentConditions[2][1] = 0.60;
            detectionModeString = "?????? ??????";
        }
        else if(detectionModeNumber == 2){
            LandmarkJudgmentConditions[0][0] = 120f;
            LandmarkJudgmentConditions[0][1] = 4.5;
            LandmarkJudgmentConditions[0][2] = -4.5;
            LandmarkJudgmentConditions[1][0] = 0.17;
            LandmarkJudgmentConditions[2][0] = -0.50;
            LandmarkJudgmentConditions[2][1] = 0.50;
            detectionModeString = "?????? ??????";
        }
        else if(detectionModeNumber == 3){
            LandmarkJudgmentConditions[0][0] = 140f;
            LandmarkJudgmentConditions[0][1] = 5.5;
            LandmarkJudgmentConditions[0][2] = -5.5;
            LandmarkJudgmentConditions[1][0] = 0.20;
            LandmarkJudgmentConditions[2][0] = -0.40;
            LandmarkJudgmentConditions[2][1] = 0.40;
            detectionModeString = "?????? ??????";
        }

        tv_ModeName.setText(detectionModeString);
        tv_TimerName.setText(UseTimerNameDB);

        ui_Handler = new Handler();
        ThreadClass callThread = new ThreadClass();
        //UI ??? ???????????? ??? ???????????? ????????? ??????.

        if (Log.isLoggable(TAG, Log.WARN)) {
            //tv.setText("11111");
            processor.addPacketCallback(
                    OUTPUT_LANDMARKS_STREAM_NAME,
                    (packet) -> {
                        //tv_ModeName.setText("1");

                        //Log.v(TAG, "Received multi face landmarks packet.");
                        List<NormalizedLandmarkList> multiFaceLandmarks =
                                PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());

                        //tv_ModeName.setText("2");
                        ratioPoint_1a = multiFaceLandmarks.get(0).getLandmarkList().get(5).getY() * 1920f;
                        ratioPoint_1b = multiFaceLandmarks.get(0).getLandmarkList().get(4).getY() * 1920f;

                        //tv_ModeName.setText("3");
                        leftEyePoint_blink_1 = multiFaceLandmarks.get(0).getLandmarkList().get(386).getY() * 1920f;
                        leftEyePoint_blink_2 = multiFaceLandmarks.get(0).getLandmarkList().get(373).getY() * 1920f;
                        rightEyePoint_blink_1 = multiFaceLandmarks.get(0).getLandmarkList().get(159).getY() * 1920f;
                        rightEyePoint_blink_2 = multiFaceLandmarks.get(0).getLandmarkList().get(144).getY() * 1920f;

                        //tv_ModeName.setText("4");
                        leftRatioMeasurement_blink = (leftEyePoint_blink_2 - leftEyePoint_blink_1) / (ratioPoint_1b - ratioPoint_1a);
                        rightRatioMeasurement_blink = (rightEyePoint_blink_2 - rightEyePoint_blink_1) / (ratioPoint_1b - ratioPoint_1a);

                        //tv_ModeName.setText("5");
                        ratioPoint_2a = multiFaceLandmarks.get(0).getLandmarkList().get(275).getY() * 1080f;
                        ratioPoint_2b = multiFaceLandmarks.get(0).getLandmarkList().get(45).getY() * 1080f;

                        //tv_ModeName.setText("6");
                        leftIrisPoint_side_1 = multiFaceLandmarks.get(0).getLandmarkList().get(374).getX() * 1080f;
                        leftIrisPoint_side_2 = multiFaceLandmarks.get(0).getLandmarkList().get(475).getX() * 1080f;
                        leftIrisPoint_side_3 = multiFaceLandmarks.get(0).getLandmarkList().get(263).getX() * 1080f;

                        //tv_ModeName.setText("7");
                        rightIrisPoint_side_1 = multiFaceLandmarks.get(0).getLandmarkList().get(145).getX() * 1080f;
                        rightIrisPoint_side_2 = multiFaceLandmarks.get(0).getLandmarkList().get(470).getX() * 1080f;
                        rightIrisPoint_side_3 = multiFaceLandmarks.get(0).getLandmarkList().get(133).getX() * 1080f;

                        //tv_ModeName.setText("8");
                        leftRatioMeasurement_corner1 = (leftIrisPoint_side_2 - leftIrisPoint_side_1) / (ratioPoint_1b - ratioPoint_1a); // (ratioPoint_2a - ratioPoint_2b);
                        leftRatioMeasurement_corner2 = (leftIrisPoint_side_3 - leftIrisPoint_side_2) / (ratioPoint_1b - ratioPoint_1a);

                        //tv_ModeName.setText("9");
                        rightRatioMeasurement_corner1 = (rightIrisPoint_side_2 - rightIrisPoint_side_1) / (ratioPoint_1b - ratioPoint_1a);
                        rightRatioMeasurement_corner2 = (rightIrisPoint_side_3 - rightIrisPoint_side_2) / (ratioPoint_1b - ratioPoint_1a);


                        //tv_ModeName.setText("10");
                        rightCheekPoint_side_1 = multiFaceLandmarks.get(0).getLandmarkList().get(50).getZ() * 1080f;
                        leftCheekPoint_side_1 = multiFaceLandmarks.get(0).getLandmarkList().get(280).getZ() * 1080f;
                        cheekRatioMeasurement_side = (rightCheekPoint_side_1 - leftCheekPoint_side_1) / (ratioPoint_1b - ratioPoint_1a);

                        //tv_ModeName.setText("11");
                        centerHeadPoint_angle_x = multiFaceLandmarks.get(0).getLandmarkList().get(4).getX() * 1080f;
                        centerHeadPoint_angle_z = Math.abs(multiFaceLandmarks.get(0).getLandmarkList().get(4).getZ() * 1080f);

                        //tv_ModeName.setText("12");
                        centerForeheadPoint_angle_x = multiFaceLandmarks.get(0).getLandmarkList().get(9).getX() * 1080f;
                        centerForeheadPoint_angle_z = Math.abs(multiFaceLandmarks.get(0).getLandmarkList().get(9).getZ() * 1080f);

                        //tv_ModeName.setText("13");
                        ap2.x = centerHeadPoint_angle_x;
                        ap2.z = centerHeadPoint_angle_z;

                        //tv_ModeName.setText("14");
                        if (cheekRatioMeasurement_side >= 0f) {
                            if (centerForeheadPoint_angle_x >= 0f && centerForeheadPoint_angle_x <= 270f) {
                                ap1.x = 270f;
                                ap3.x = 270f;
                                apResult = getLandmarksAngle(ap1, ap2, ap3);
                            } else if (centerForeheadPoint_angle_x <= 540f) {
                                ap1.x = 540f;
                                ap3.x = 540f;
                                apResult = getLandmarksAngle(ap1, ap2, ap3);
                            } else if (centerForeheadPoint_angle_x <= 810f) {
                                ap1.x = 810f;
                                ap3.x = 810f;
                                apResult = getLandmarksAngle(ap1, ap2, ap3);
                            } else if (centerForeheadPoint_angle_x <= 1080f) {
                                ap1.x = 1080f;
                                ap3.x = 1080f;
                                apResult = getLandmarksAngle(ap1, ap2, ap3);
                            } else {
                                apResult = -1f;
                            }
                            //tv_ModeName.setText("15-1");
                        } else if (cheekRatioMeasurement_side < 0f) {
                            if (centerForeheadPoint_angle_x <= 1080f && centerForeheadPoint_angle_x >= 810f) {
                                ap1.x = 810f;
                                ap3.x = 810f;
                                apResult = getLandmarksAngle(ap1, ap2, ap3);
                            } else if (centerForeheadPoint_angle_x >= 540f) {
                                ap1.x = 540f;
                                ap3.x = 540f;
                                apResult = getLandmarksAngle(ap1, ap2, ap3);
                            } else if (centerForeheadPoint_angle_x >= 270f) {
                                ap1.x = 270f;
                                ap3.x = 270f;
                                apResult = getLandmarksAngle(ap1, ap2, ap3);
                            } else if (centerForeheadPoint_angle_x >= 0f) {
                                ap1.x = 0f;
                                ap3.x = 0f;
                                apResult = getLandmarksAngle(ap1, ap2, ap3);
                            } else {
                                apResult = -1f;
                            }
                            //tv_ModeName.setText("15-2");
                        }

                        if (1 <= globalTime && startUIHandlerCheck) {
                            tv_RestartText.setText("?????????");
                            ui_Handler.post(callThread);
                            // ???????????? ?????? ??????????????? OS?????? ????????? ??????
                            startUIHandlerCheck = false;
                        }
                        //180??? ?????????
                        //?????? ?????? ???????????? ???????????? ??? ????????? ??? ??????
                    });
        }
    }

    class ThreadClass extends Thread {
        //????????? ?????? ?????? UI ????????? ?????????
        @Override
        public void run() {
            //tv_WaringSearchTop.setText(centerForeheadPoint_angle_x + "=???????????? ????????????=" + apResult);
            //tv_WaringSearchBottom.setText(cheekRatioMeasurement_side + "=???????????? ???????????????=" + rightRatioMeasurement_corner1);
            //tv_ModeName.setText("16");
            waringSearchBottomText = "";
            if ((apResult <= LandmarkJudgmentConditions[0][0]
                    && (cheekRatioMeasurement_side >= LandmarkJudgmentConditions[0][1] || -LandmarkJudgmentConditions[0][2] >= cheekRatioMeasurement_side))) {
                //apResult??? 130??? ????????? ????????? ?????????, cheekRatioMeasurement_side??? 5?????? ????????? -5?????? ?????? ?????? ?????? ??????????????? ??????
                //if (head_side) {
                waringSearchBottomText += "????????? ?????????, ";
                head_side = false;
                //}
                //tv_ModeName.setText("17-1");
            } else {
                waringSearchBottomText += "????????? ?????????, ";
                head_side = true;
                //tv_ModeName.setText("17-2");
            }

            //tv_ModeName.setText("18");
            if (leftRatioMeasurement_blink < LandmarkJudgmentConditions[1][0] || rightRatioMeasurement_blink < LandmarkJudgmentConditions[1][0]) {
                //leftRatioMeasurement_blink ???????????? ??????????????? ??????
                //if (eye_blink) {
                waringSearchBottomText += "?????? ?????????, ";
                eye_blink = false;
                //}
                //tv_ModeName.setText("19-1");
            } else {
                waringSearchBottomText += "?????? ?????????, ";
                eye_blink = true;
                //tv_ModeName.setText("19-2");
            }

            //tv_ModeName.setText("20");
            if ((leftRatioMeasurement_corner1 < LandmarkJudgmentConditions[2][0] || LandmarkJudgmentConditions[2][1] < leftRatioMeasurement_corner1)
                    && (rightRatioMeasurement_corner1 < LandmarkJudgmentConditions[2][0] || LandmarkJudgmentConditions[2][1] < rightRatioMeasurement_corner1)) {
                //RatioMeasurement_corner1??? -0.45?????? ????????? 0.45?????? ????????? ????????? ?????? ??????
                //if (iris_corner) {
                waringSearchBottomText += "???????????? ??????";
                iris_corner = false;
                //}
                //tv_ModeName.setText("21-1");
            } else {
                waringSearchBottomText += "???????????? ????????????";
                iris_corner = true;
                //tv_ModeName.setText("21-2");
            }
            if(!pauseTimerCheck) {
                if (!head_side || !eye_blink || !iris_corner) {
                    if (concentrationTime >= 8) {
                        tv_WaringSearchTop.setText("???????????? ??????????????? ????????????.");
                        tv_WaringSearchTopCheck = true;
                        if (conc_check) {
                            tv_WaringSearchTop.setText("????????? ?????? ??????");
                            saveDataConcentration();
                            conc_check = false;
                        }

                    }
                    if (concentrationTime <= 20) {
                        concentrationTime++;
                    }
                } else {
                    if (concentrationTime <= 4) {
                        tv_WaringSearchTop.setText("??????????????? ???????????? ????????????.");
                        tv_WaringSearchTopCheck = false;
                        if (!conc_check) {
                            tv_WaringSearchTop.setText("????????? ?????? ??????");
                            startConcDateTime = LocalDateTime.now();
                            conc_check = true;
                        }
                    }
                    if (concentrationTime >= 0) {
                        concentrationTime--;
                    }
                }
            }

            if(finalStopCheck == 1){
                tv_RestartText.setText("?????? ??????");
                saveDataConcentration();
                saveDataMeasurement();
            }
            if(finalStopCheck == 2 && timer_second <= 1) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                pauseTimerCheck = true;
                ui_HandlerCheck = false;
                finish();
            }

            try {
                Thread.sleep(1000); // ????????? ???????????? ?????? ????????? ????????????.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // ?????? ????????? OS????????? ?????? ????????????.
            tv_WaringSearchBottom.setText(waringSearchBottomText);
            if(ui_HandlerCheck) {
                ui_Handler.post(this);
            }
        }
    }

    private void saveDataMeasurement() { //????????? ?????? ?????? ??????, ??????
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
        //MeasurementRoomDatabase.getDatabase(getApplicationContext()).getMeasurementTableDao().deleteAll(); ?????? ??????

        Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
        finalStopCheck = 2;
    }


    private void saveDataConcentration() { //????????? ?????? ?????? ?????? ??????, ??????
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
        //MeasurementRoomDatabase.getDatabase(getApplicationContext()).getMeasurementTableDao().deleteAll(); ?????? ??????

        Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
    }


    public void onClickExit(View view) {
        if(1 <= globalTime) {
            tv_RestartText.setText("?????? ???...");
            if(finalStopCheck == 0) {
                saveDataMeasurement();
                saveDataConcentration();
            }
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            pauseTimerCheck = true;
            ui_HandlerCheck = false;
            finish();
        }
    }

    public void onClickPause(View view) {
        if(1 <= globalTime && finalStopCheck == 0) {
            if(!pauseTimerCheck) {
                //saveDataConcentration();
                tv_RestartText.setText("?????????");
                pauseTimerCheck = true;
            }
            else {
                //startConcDateTime = LocalDateTime.now();
                tv_RestartText.setText("?????????");
                pauseTimerCheck = false;
            }
        }
    }


    protected int getContentViewLayoutResId() {
        return R.layout.activity_detection;
    }

    @Override
    protected void onResume() {
        super.onResume();
        converter =
                new ExternalTextureConverter(
                        eglManager.getContext(), 2);
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();

        previewDisplayView.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onCameraStarted(SurfaceTexture surfaceTexture) {
        previewFrameTexture = surfaceTexture;
        previewDisplayView.setVisibility(View.VISIBLE);
    }

    protected Size cameraTargetResolution() {
        return null;
    }

    public void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    onCameraStarted(surfaceTexture);
                });
        CameraHelper.CameraFacing cameraFacing = CameraHelper.CameraFacing.FRONT;
        cameraHelper.startCamera(
                this, cameraFacing, previewFrameTexture, cameraTargetResolution());
    }

    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    protected void onPreviewDisplaySurfaceChanged(
            SurfaceHolder holder, int format, int width, int height) {
        Size viewSize = computeViewSize(width, height);
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
        boolean isCameraRotated = cameraHelper.isCameraRotated();

        converter.setSurfaceTextureAndAttachToGLContext(
                previewFrameTexture,
                isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    }

    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);

        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                onPreviewDisplaySurfaceChanged(holder, format, width, height);
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }

    private static String getMultiFaceLandmarksDebugString(
            List<NormalizedLandmarkList> multiFaceLandmarks) {
        if (multiFaceLandmarks.isEmpty()) {
            return "No face landmarks";
        }
        String multiFaceLandmarksStr = "Number of faces detected: " + multiFaceLandmarks.size() + "\n";
        int faceIndex = 0;
        for (NormalizedLandmarkList landmarks : multiFaceLandmarks) {
            multiFaceLandmarksStr +=
                    "\t#Face landmarks for face[" + faceIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;
            for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
                multiFaceLandmarksStr +=
                        "\t\tLandmark ["
                                + landmarkIndex
                                + "]: ("
                                + landmark.getX()
                                + ", "
                                + landmark.getY()
                                + ", "
                                + landmark.getZ()
                                + ")\n";
                ++landmarkIndex;
            }
            ++faceIndex;
        }
        return multiFaceLandmarksStr;
    }

    public static float getLandmarksAngle(Point p1, Point p2, Point p3) {
        float p1_2 = (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.z - p2.z, 2));
        float p2_3 = (float) Math.sqrt(Math.pow(p2.x - p3.x, 2) + Math.pow(p2.z - p3.z, 2));
        float p3_1 = (float) Math.sqrt(Math.pow(p3.x - p1.x, 2) + Math.pow(p3.z - p1.z, 2));
        float radian = (float) Math.acos((p1_2 * p1_2 + p2_3 * p2_3 - p3_1 * p3_1) / (2 * p1_2 * p2_3));
        float degree = (float) (radian / Math.PI * 180);
        return degree;
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            // ??????????????? ??????
            globalTime++;
            if(!pauseTimerCheck) {
                // 0??? ????????????
                if (timer_second != 0) {
                    //1?????? ??????
                    timer_second--;

                    // 0??? ????????????
                } else if (timer_minute != 0) {
                    // 1??? = 60???
                    timer_second = 60;
                    timer_second--;
                    timer_minute--;

                    // 0?????? ????????????
                } else if (timer_hour != 0) {
                    // 1?????? = 60???
                    timer_second = 60;
                    timer_minute = 60;
                    timer_second--;
                    timer_minute--;
                    timer_hour--;
                }

                //???, ???, ?????? 10??????(????????????) ??????
                // ?????? ?????? 0??? ????????? ( 8 -> 08 )
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
                if(finalStopCheck == 0) {
                    tv_TimeCounter.setText(nowTime);
                }
                else if(finalStopCheck == 1 || finalStopCheck == 2) {
                    tv_TimeCounter.setText(timer_second + "??? ??? ????????????");
                }
            }

            if (timer_hour == 0 && timer_minute == 0 && timer_second == 0) {
                /*timerTask.cancel();//????????? ??????
                timer.cancel();//????????? ??????
                timer.purge();//????????? ??????*/
                //????????? ?????? ????????? ??? ???????????? ????????? ??? ????????? ???????????? ???????????? ?????? ???????????? ?????? ????????? ??? ????????? ?????? ??????
                if(finalStopCheck == 0) {
                    timer_second += 10;
                    finalStopCheck = 1;
                }
            }
        }
    };

    private void startDialog() {
        tv_TimeCounter = findViewById(R.id.time_counter_id);
        //?????? ???????????? ???????????? ??????
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(DetectionActivity.this)
                .setTitle("?????? ??? ??????")
                .setMessage("????????? ?????? ????????? ????????? ?????? ????????? 10??? ?????? ????????? ????????? ???????????????. ????????? ?????? ???????????? ????????? ????????? ????????? ????????? ?????????????????? ????????????.")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        divideTime = UseTimerTimeDB.split(":");
                        timer_hour = Integer.parseInt(divideTime[0]);
                        timer_minute = Integer.parseInt(divideTime[1]);
                        timer_second = Integer.parseInt(divideTime[2]);
                        totalTime = ((((timer_hour * 60) + timer_minute) * 60) + timer_second) * 1000;
                        timer.scheduleAtFixedRate(timerTask, 5000, 1000); //Timer ??????
                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }
}

//??????????????? ??????
//???????????? ????????? ??????
//??? ????????? ????????????, ?????? ?????? ??????? ?????? ???????