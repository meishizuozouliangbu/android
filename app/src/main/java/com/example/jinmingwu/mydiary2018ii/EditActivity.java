package com.example.jinmingwu.mydiary2018ii;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import android.app.AlertDialog.Builder;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

import me.itangqi.greendao.DaoMaster;
import me.itangqi.greendao.DaoSession;
import me.itangqi.greendao.Note;
import me.itangqi.greendao.NoteDao;

/**
 * Created by jinmingwu on 2018/6/19.
 */

public class EditActivity extends AppCompatActivity {

    public static final String EDIT_DIARY_ACTION = "DiaryEditor.EDIT_DIARY";
    public static final String INSERT_DIARY_ACTION = "DiaryEditor.action.INSERT_DIARY";
    private EditText editTitle;
    private EditText editComment;
    private TextView editMap;
    private TextView myLocationText;
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private Cursor cursor;
    public static final String TAG = "DaoExample";

    private long diaryID;
    private String actionSymbol;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        myLocationText = (TextView)findViewById(R.id.editmapText);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);

        //--------------------------------配置定位BAIDU SDK 参数 开始
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标

        option.setScanSpan(0);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(false);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5*60*1000);
        //可选，7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        option.setIsNeedAddress(true);
        //可选，是否需要地址信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的地址信息，此处必须为true

        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明

        //--------------------------------配置定位BAIDU SDK 参数 结束
        mLocationClient.start(); //开始获取定位

        editTitle = (EditText) findViewById(R.id.editText);
        editComment = (EditText) findViewById(R.id.editText2);
        editMap = (TextView) findViewById(R.id.editmapText);

        // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
        setupDatabase();
        // 获取 NoteDao 对象
        getNoteDao();

        final Intent intent = getIntent();
        actionSymbol = intent.getAction();
        if (EDIT_DIARY_ACTION.equals(actionSymbol)) {
            Bundle bundle = new Bundle();
            bundle = this.getIntent().getExtras();
            diaryID = bundle.getLong("id");
            editTitle.setText(bundle.getString("title"));
            editComment.setText(bundle.getString("diary"));
            editMap.setText(bundle.getString("map"));
            setTitle("编辑日记");
        } else if (INSERT_DIARY_ACTION.equals(actionSymbol)) {
            setTitle("新建日记");
        } else {
            finish();
        }

    }

    //optionBar Menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editactivity_menu, menu);
        return true;
    }

    //share Menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
                if (EDIT_DIARY_ACTION.equals(actionSymbol)) {
                    intent.putExtra(Intent.EXTRA_TEXT, "日记标题：" + editTitle.getText().toString() + "\n日记内容：" + editComment.getText().toString() + "\n" + daoSession.getNoteDao().load(diaryID).getComment());
                } else {
                    final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
                    String comment = "Added on " + df.format(new Date());
                    intent.putExtra(Intent.EXTRA_TEXT, "日记标题：" + editTitle.getText().toString() + "\n日记内容：" + editComment.getText().toString() + "\n" + comment);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
                break;
            default:
                break;
        }
        return true;
    }

    public void onMyButtonClick(View view) {
        switch (view.getId()) {
            case R.id.edit_save: //保存
                this.addNote();
                Intent intent = new Intent();
                intent.setClass(EditActivity.this, NoteActivity.class);
                startActivity(intent);
                break;
            case R.id.edit_giveup: //返回
                if (editComment.getText().toString() == null) {
                    finish();
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(EditActivity.this);
                    dialog.setIcon(R.drawable.dialoginfo);
                    dialog.setTitle("温馨提示");
                    dialog.setMessage("日记尚未保存，是否需要保存？");
                    dialog.setNegativeButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addNote();
                            Intent intent = new Intent();
                            intent.setClass(EditActivity.this, NoteActivity.class);
                            startActivity(intent);
                        }
                    });
                    dialog.setNeutralButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dialog.create();
                    dialog.show();
                }
                break;
            case R.id.edit_map: //获取地图
                //startActivity(new Intent(this,TencentActivity.class));
                //findMap();
                String address = myListener.getAddr();
                myLocationText.setText(address);
                break;
            default:
                Log.d(TAG, "what has gone wrong ?");
                break;
        }
    }

//    private void findMap(){
//        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        //String provider = LocationManager.GPS_PROVIDER;
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);//精准度
//        criteria.setAltitudeRequired(false);//海拔高度
//        criteria.setBearingRequired(false);//方位
//        criteria.setCostAllowed(true);//花费
//        criteria.setPowerRequirement(Criteria.POWER_HIGH);//电池容量
//        String provider = locationManager.getBestProvider(criteria, true);
//
//        Location location = locationManager.getLastKnownLocation(provider);
//
//        updateWithNewLocation(location);
//        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
//    }
//
//    private final LocationListener locationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            updateWithNewLocation(location);
//        }
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//        @Override
//        public void onProviderEnabled(String provider) {
//
//        }
//        @Override
//        public void onProviderDisabled(String provider) {
//            updateWithNewLocation(null);
//        }
//    };
//
//    private void updateWithNewLocation(Location location) {
//        String latLongString = null;
//        myLocationText = (TextView)findViewById(R.id.editmapText);
//        if(location != null){
//            double lat = location.getLatitude();
//            double lng = location.getLongitude();
//            latLongString = "纬度" + lat + "\t经度" + lng ;
//        } else {
//            latLongString = "无法获取地理信息" ;
//        }
//        myLocationText.setText(latLongString);
//    }

    private void addNote() {
        if (EDIT_DIARY_ACTION.equals(actionSymbol)) {
            getNoteDao().deleteByKey(diaryID);
        }
        String noteText = editTitle.getText().toString();
        String noteText2 = editComment.getText().toString();
        String map = editMap.getText().toString();

        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        String comment = "Added on " + df.format(new Date());

        // 插入操作，简单到只要你创建一个 Java 对象
        Note note = new Note(null, noteText, comment, new Date(), noteText2, map);
        getNoteDao().insert(note);
        Log.d(TAG, "Inserted new note, ID: " + note.getId());
        Toast.makeText(getApplicationContext(), "已保存", Toast.LENGTH_SHORT).show();
//        cursor.requery();
    }

    private NoteDao getNoteDao() {
        return daoSession.getNoteDao();
    }

    private void setupDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        db = helper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }
}