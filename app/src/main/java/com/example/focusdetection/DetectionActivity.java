package com.example.focusdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.util.Calendar;
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
    //UI 스레드 용 핸들러
    //private static Handler mHandler ;
    //스레드 용 핸들러

    private TextView tv_ModeName, tv_TimerMode, tv_RestartText, tv_TimeCounter;
    private TextView tv_WaringSearchTop, tv_WaringSearchBottom;
    //텍스트 뷰 목록

    private boolean startDialogCheck = true;
    //타이머 다이얼로그 시작 확인
    private boolean startUIHandlerCheck = true;
    //ui 핸들러 시작 확인

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
    private String SetTimeDB = "02:07:11";
    //템플릿 타이머 시간 (시간:분:초)
    private String[] divideTime;
    //문자열에서 분할된 시간
    private Timer timer = new Timer();

    private String waringSearchBottomText = "집중력 저하가 발견되지 않았습니다.";
    //집중력 저하 탐지 결과 아래쪽 텍스트

    private float leftEyePoint_blink_1, leftEyePoint_blink_2;
    //왼쪽 눈 깜빡임용 랜드마크 포인트
    private float rightEyePoint_blink_1, rightEyePoint_blink_2;
    //오른쪽 눈 깜빡임용 랜드마크 포인트
    private float ratioPoint_1a, ratioPoint_1b, ratioPoint_2a, ratioPoint_2b;
    // 눈 깜빡임용 비율 계산에 쓰일 포인트 변수 (왼쪽, 오른쪽)
    private float leftRatioMeasurement_blink, rightRatioMeasurement_blink;
    // 눈 비율 계산값 변수
    private boolean eye_blink, eye_open;
    //양쪽 눈 감았는지 여부

    private float leftIrisPoint_side_1, leftIrisPoint_side_2, leftIrisPoint_side_3;
    //왼쪽 눈 좌우용 랜드마크 포인트
    private float rightIrisPoint_side_1, rightIrisPoint_side_2, rightIrisPoint_side_3;
    //오른쪽 눈 좌우용 랜드마크 포인트
    private float leftRatioMeasurement_corner1, leftRatioMeasurement_corner2, rightRatioMeasurement_corner1, rightRatioMeasurement_corner2;
    // 눈 비율 계산값 변수
    private boolean iris_corner, iris_center;
    //양쪽 눈 틀어졌는지 여부

    private float leftCheekPoint_side_1, rightCheekPoint_side_1;
    //고개 좌우 판별용 뺨 랜드마크 포인트
    private float cheekRatioMeasurement_side;
    //고개 좌우 판별 뺨 변수
    private float centerHeadPoint_angle_x, centerHeadPoint_angle_z;
    //고개 좌우 각도용 코 랜드마크 포인트
    private float centerForeheadPoint_angle_x, centerForeheadPoint_angle_z;
    //고개 좌우 각도용 이마 랜드마크 포인트
    private boolean head_side, head_middle;
    //고개 돌아갔는지 틀어졌는지 여부

    Point ap1 = new Point();
    Point ap2 = new Point();
    Point ap3 = new Point();
    private float apResult;

    class Point {
        float x;
        float z;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutResId());
        tv_ModeName = findViewById(R.id.mode_name_id);
        tv_TimerMode = findViewById(R.id.timer_name_id);
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

        ui_Handler = new Handler();
        ThreadClass callThread = new ThreadClass();
        //UI 를 업데이트 할 쓰레드를 만들어 준다.

        if (Log.isLoggable(TAG, Log.WARN)) {
            //tv.setText("11111");
            processor.addPacketCallback(
                    OUTPUT_LANDMARKS_STREAM_NAME,
                    (packet) -> {
                        tv_ModeName.setText("1");

                        //Log.v(TAG, "Received multi face landmarks packet.");
                        List<NormalizedLandmarkList> multiFaceLandmarks =
                                PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());

                        tv_ModeName.setText("2");
                        ratioPoint_1a = multiFaceLandmarks.get(0).getLandmarkList().get(5).getY() * 1920f;
                        ratioPoint_1b = multiFaceLandmarks.get(0).getLandmarkList().get(4).getY() * 1920f;

                        tv_ModeName.setText("3");
                        leftEyePoint_blink_1 = multiFaceLandmarks.get(0).getLandmarkList().get(386).getY() * 1920f;
                        leftEyePoint_blink_2 = multiFaceLandmarks.get(0).getLandmarkList().get(373).getY() * 1920f;
                        rightEyePoint_blink_1 = multiFaceLandmarks.get(0).getLandmarkList().get(159).getY() * 1920f;
                        rightEyePoint_blink_2 = multiFaceLandmarks.get(0).getLandmarkList().get(144).getY() * 1920f;

                        tv_ModeName.setText("4");
                        leftRatioMeasurement_blink = (leftEyePoint_blink_2 - leftEyePoint_blink_1) / (ratioPoint_1b - ratioPoint_1a);
                        rightRatioMeasurement_blink = (rightEyePoint_blink_2 - rightEyePoint_blink_1) / (ratioPoint_1b - ratioPoint_1a);

                        tv_ModeName.setText("5");
                        ratioPoint_2a = multiFaceLandmarks.get(0).getLandmarkList().get(275).getY() * 1080f;
                        ratioPoint_2b = multiFaceLandmarks.get(0).getLandmarkList().get(45).getY() * 1080f;

                        tv_ModeName.setText("6");
                        leftIrisPoint_side_1 = multiFaceLandmarks.get(0).getLandmarkList().get(374).getX() * 1080f;
                        leftIrisPoint_side_2 = multiFaceLandmarks.get(0).getLandmarkList().get(475).getX() * 1080f;
                        leftIrisPoint_side_3 = multiFaceLandmarks.get(0).getLandmarkList().get(263).getX() * 1080f;

                        tv_ModeName.setText("7");
                        rightIrisPoint_side_1 = multiFaceLandmarks.get(0).getLandmarkList().get(145).getX() * 1080f;
                        rightIrisPoint_side_2 = multiFaceLandmarks.get(0).getLandmarkList().get(470).getX() * 1080f;
                        rightIrisPoint_side_3 = multiFaceLandmarks.get(0).getLandmarkList().get(133).getX() * 1080f;

                        tv_ModeName.setText("8");
                        leftRatioMeasurement_corner1 = (leftIrisPoint_side_2 - leftIrisPoint_side_1) / (ratioPoint_1b - ratioPoint_1a); // (ratioPoint_2a - ratioPoint_2b);
                        leftRatioMeasurement_corner2 = (leftIrisPoint_side_3 - leftIrisPoint_side_2) / (ratioPoint_1b - ratioPoint_1a);

                        tv_ModeName.setText("9");
                        rightRatioMeasurement_corner1 = (rightIrisPoint_side_2 - rightIrisPoint_side_1) / (ratioPoint_1b - ratioPoint_1a);
                        rightRatioMeasurement_corner2 = (rightIrisPoint_side_3 - rightIrisPoint_side_2) / (ratioPoint_1b - ratioPoint_1a);


                        tv_ModeName.setText("10");
                        rightCheekPoint_side_1 = multiFaceLandmarks.get(0).getLandmarkList().get(50).getZ() * 1080f;
                        leftCheekPoint_side_1 = multiFaceLandmarks.get(0).getLandmarkList().get(280).getZ() * 1080f;
                        cheekRatioMeasurement_side = (rightCheekPoint_side_1 - leftCheekPoint_side_1) / (ratioPoint_1b - ratioPoint_1a);

                        tv_ModeName.setText("11");
                        centerHeadPoint_angle_x = multiFaceLandmarks.get(0).getLandmarkList().get(4).getX() * 1080f;
                        centerHeadPoint_angle_z = Math.abs(multiFaceLandmarks.get(0).getLandmarkList().get(4).getZ() * 1080f);

                        tv_ModeName.setText("12");
                        centerForeheadPoint_angle_x = multiFaceLandmarks.get(0).getLandmarkList().get(9).getX() * 1080f;
                        centerForeheadPoint_angle_z = Math.abs(multiFaceLandmarks.get(0).getLandmarkList().get(9).getZ() * 1080f);

                        tv_ModeName.setText("13");
                        ap2.x = centerHeadPoint_angle_x;
                        ap2.z = centerHeadPoint_angle_z;

                        tv_ModeName.setText("14");
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
                            tv_ModeName.setText("15-1");
                        }
                        else if (cheekRatioMeasurement_side < 0f) {
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
                            tv_ModeName.setText("15-2");
                        }

                        if (startUIHandlerCheck) {
                            ui_Handler.post(callThread);
                            // 핸들러를 통해 안드로이드 OS에게 작업을 요청
                            startUIHandlerCheck = false;
                        }
                        //180이 최대값
                        //tv_WaringSearchBottom.setText(waringSearchBottomText);
                        //화면 크기 바뀌면서 전반적인 값 틀어진 것 같음
                    });
        }
    }

    class ThreadClass extends Thread {
        //@Override
        public void run() {
            tv_WaringSearchTop.setText(centerForeheadPoint_angle_x + "=고개측도 고개각도=" + apResult);
            tv_WaringSearchBottom.setText(cheekRatioMeasurement_side + "=고개지수 오른눈동자=" + rightRatioMeasurement_corner1);
            tv_ModeName.setText("16");
            if ((apResult <= 130f && (cheekRatioMeasurement_side >= 5f || cheekRatioMeasurement_side <= -5f))) {
                //apResult가 130도 아래만 범위에 들어옴, cheekRatioMeasurement_side가 5보다 크거나 -5보다 작을 경우 고개 돌아갔는지 감지
                if (head_side) {
                    //waringSearchBottomText = "고개가 돌아감, ";
                    //tv_WaringSearchTop.setText("집중력 저하 감지");
                    head_side = false;
                }
                tv_ModeName.setText("17-1");
            }
            else {
                //waringSearchBottomText = "고개가 중앙임, ";
                //tv_WaringSearchTop.setText("집중력 저하 없음");
                head_side = true;
                tv_ModeName.setText("17-2");
            }

            tv_ModeName.setText("18");
            if (leftRatioMeasurement_blink < 0.16 || rightRatioMeasurement_blink < 0.16) {
                //leftRatioMeasurement_blink 미만이면 눈감김여부 감지
                if (eye_blink) {
                    //waringSearchBottomText += "눈이 감겼음, ";
                    //tv_WaringSearchTop.setText("집중력 저하 감지");
                    eye_blink = false;
                }
                tv_ModeName.setText("19-1");
            }
            else {
                //waringSearchBottomText += "눈이 떠졌음, ";
                //tv_WaringSearchTop.setText("집중력 저하 없음");
                eye_blink = true;
                tv_ModeName.setText("19-2");
            }

            tv_ModeName.setText("20");
            if ((-0.45 > leftRatioMeasurement_corner1 || leftRatioMeasurement_corner1 > 0.45)
                    && (-0.45 > rightRatioMeasurement_corner1 || rightRatioMeasurement_corner1 > 0.45)) {
                //RatioMeasurement_corner1가 -0.45보다 작거나 0.45보다 클때만 눈동자 쏠림 감지
                if (iris_corner) {
                    //waringSearchBottomText += "눈동자가 쏠림";
                    //tv_WaringSearchTop.setText("집중력 저하 감지");
                    iris_corner = false;
                }
                tv_ModeName.setText("21-1");
            }
            else {
                //waringSearchBottomText += "눈동자가 가운데임";
                //tv_WaringSearchTop.setText("집중력 저하 없음");
                iris_corner = true;
                tv_ModeName.setText("21-2");
            }
            try {
                Thread.sleep(1000); // 쓰레드 슬립으로 일정 시간을 대기한다.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 현재 작업을 OS님에게 다시 요청한다.
            ui_Handler.post(this);
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
            // 반복실행할 구문

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
        tv_TimeCounter = findViewById(R.id.time_counter_id);
        //이건 자르던지 바꾸던지 하셈
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(DetectionActivity.this)
                .setTitle("시작 전 준비")
                .setMessage("하단의 확인 버튼을 누르고 나서 정확히 10초 뒤에 집중력 감지가 실행됩니다. 10초 타이머가 흘러가는 순간부터 공부를 진행해주시면 됩니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        divideTime = SetTimeDB.split(":");
                        timer_hour = Integer.parseInt(divideTime[0]);
                        timer_minute = Integer.parseInt(divideTime[1]);
                        timer_second = Integer.parseInt(divideTime[2]);
                        totalTime = ((((timer_hour * 60) + timer_minute) * 60) + timer_second) * 1000;
                        timer.scheduleAtFixedRate(timerTask, 5000, 1000); //Timer 실행
                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }
}