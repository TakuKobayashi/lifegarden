package org.cocos2dx.cpp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SensorStreamer extends ContextSingletonBase implements SensorEventListener{
  private final static int REQUEST_CODE = 1;

  private SensorManager mSensorManager;
  private Sensor mStepDetectorSensor;
  private Sensor mStepConterSensor;
  private FaceDetector mFaceDetector;
  private CameraSource mCameraSource;
  private long mPrevSensorTime = System.currentTimeMillis();
  //10秒静止したらリセット
  private static final long STOP_WORKING_CHECK_TIME = 10000000000l;
  //5歩以上で歩き中
  private static final int START_WALKING_COUNT = 5;

  private int mWorkingCounter = 0;
  private int mPrevWorkingCount = 0;
  private boolean isWorking = false;

  private int mFaceingCounter = 0;
  private boolean isFaceing = false;
  // 50回検知でながら中
  private static final int START_FACING_COUNTER = 10;

  private ArrayList<SensorStreamListener> mListenerQueue;
  private long mPrevRequestTime = System.currentTimeMillis();
  private long mCountTimer = 0;
  private long mFacingOutTime = System.currentTimeMillis();
  private static final long FACING_OUT_TIME = 2000;
  //10秒ながらで殺す
  private static final long KILL_PET_TIME = 2000;

  public void init(Context context){
    super.init(context);
    mListenerQueue = new ArrayList<SensorStreamListener>();
    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    //センサマネージャから TYPE_STEP_DETECTOR についての情報を取得する
    mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    //センサマネージャから TYPE_STEP_COUNTER についての情報を取得する
    mStepConterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    mFaceDetector = new FaceDetector.
                    Builder(context).
                    setTrackingEnabled(false).
                    setLandmarkType(FaceDetector.ALL_LANDMARKS).
                    setMode(FaceDetector.FAST_MODE).
                    build();
    createCameraSource();
  }

  public void addStraemListener(SensorStreamListener listener){
    mListenerQueue.add(listener);
  }

  public void removeStraemListener(SensorStreamListener listener){
    mListenerQueue.remove(listener);
  }

  public void startSensor(){
    mSensorManager.registerListener(this, mStepConterSensor, SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    try {
      mCameraSource.start();
    } catch (Exception e) {
      e.printStackTrace();
      mFaceDetector.release();
      mFaceDetector = null;
      return;
    }
  }

  public void stopSensor(){
    mSensorManager.unregisterListener(this, mStepConterSensor);
    mSensorManager.unregisterListener(this, mStepDetectorSensor);
    mFaceDetector.release();
    mFaceDetector = null;
  }

  private void createCameraSource() {
    Context c = context.getApplicationContext();
    FaceDetector detector = new FaceDetector.Builder(c)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build();
    detector.setProcessor(
            new MultiProcessor.Builder<>(new MultiProcessor.Factory<Face>(){
              @Override
              public Tracker<Face> create(Face face) {
                return new Tracker<Face>(){
                  @Override
                  public void onNewItem(int faceId, Face item) {
                    if((System.currentTimeMillis() - mFacingOutTime) > FACING_OUT_TIME){
                      mFaceingCounter = 0;
                    }
                    Log.d(Config.TAG, "new");
                  }
                  @Override
                  public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
                    ++mFaceingCounter;
                    if(mFaceingCounter > START_FACING_COUNTER){
                      isFaceing = true;
                    }
                    killPetRequest();
                    Log.d(Config.TAG, "update");
                  }

                  @Override
                  public void onMissing(FaceDetector.Detections<Face> detectionResults) {
                    Log.d(Config.TAG, "missing");
                  }

                  @Override
                  public void onDone() {
                    isFaceing = false;
                    mFacingOutTime = System.currentTimeMillis();
                    Log.d(Config.TAG, "done");
                  }
                };
              }
            }).build());

    if (!detector.isOperational()) {

    }

    mCameraSource = new CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(640, 480)
            .setFacing(CameraSource.CAMERA_FACING_FRONT)
            .setRequestedFps(30.0f)
            .build();
  }

  private void killPetRequest(){
    Log.d(Config.TAG, "f1:" + isFaceing + "f2:" + isWorking);
    if(!isFaceing || !isWorking){
      mPrevRequestTime = System.currentTimeMillis();
      mCountTimer = 0;
      return;
    }
    long current = System.currentTimeMillis();
    mCountTimer = mCountTimer + (current - mPrevRequestTime);
    mPrevRequestTime = current;
    Log.d(Config.TAG, "t:" + mCountTimer);
    if(mCountTimer < KILL_PET_TIME) return;
    StringRequest postRequest = new StringRequest(Request.Method.POST,Config.VIEW_URL,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String s) {
                Log.d(Config.TAG, "res:" + s);
              }
            },
            new Response.ErrorListener(){
              @Override
              public void onErrorResponse(VolleyError error){
                Log.d(Config.TAG, "error:" + error.getMessage() + " le:" + error.getLocalizedMessage());
              }
            }){
      @Override
      protected Map<String,String> getParams(){
        HashMap<String, String> params = new HashMap<String, String>();
        return params;
      }
    };
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    requestQueue.add(postRequest);
    sendNotification();
    mCountTimer = 0;
  }

  //デストラクタ
  @Override
  protected void finalize() throws Throwable {
    stopSensor();
    mListenerQueue.clear();
    super.finalize();
  }

  public interface SensorStreamListener{
    public void OnWalking();
    public void OnFaceing();
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    Sensor sensor = event.sensor;
    float[] values = event.values;
    long timestamp = event.timestamp;
    long diff = timestamp - mPrevSensorTime;

    //TYPE_STEP_COUNTER
    if(sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
      if(diff > STOP_WORKING_CHECK_TIME) {
        mWorkingCounter = 0;
        isWorking = false;
      }
      int walk = (int)values[0];
      if(mWorkingCounter == 0) {
        mPrevWorkingCount = walk;
        ++mWorkingCounter;
      }else{
        int d = walk - mPrevWorkingCount;
        mWorkingCounter += d;
        mPrevWorkingCount = walk;
      }
      if(mWorkingCounter > START_WALKING_COUNT){
        isWorking = true;
      }
      mPrevSensorTime = timestamp;
      killPetRequest();
    }
  }

  private void sendNotification() {
    Intent tappedIntent = new Intent(context, AppActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(context, REQUEST_CODE, tappedIntent, PendingIntent.FLAG_ONE_SHOT);

    // LargeIcon の Bitmap を生成
    Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

    // NotificationBuilderを作成
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
    builder.setContentIntent(contentIntent);
    // アイコン
    builder.setSmallIcon(R.mipmap.ic_launcher);
    // Notificationを開いたときに表示されるタイトル
    builder.setContentTitle(context.getString(R.string.notification_title));
    // Notificationを開いたときに表示されるサブタイトル
    builder.setContentText(context.getString(R.string.notification_message));
    // Notificationを開いたときに表示されるアイコン
    builder.setLargeIcon(largeIcon);
    // 通知するタイミング
    builder.setWhen(System.currentTimeMillis());
    // 通知時の音・バイブ・ライト
    builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
    // タップするとキャンセル(消える)
    builder.setAutoCancel(false);
    builder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.death_sound));

    // NotificationManagerを取得
    NotificationManager manager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
    // Notificationを作成して通知
    manager.notify(1, builder.build());
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    Log.d(Config.TAG, "sensorChanged");
  }
}
