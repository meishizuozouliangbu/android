package com.example.jinmingwu.mydiary2018ii;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);

        //--------------------------------配置定位BAIDU SDK 参数 开始
        LocationClientOption option = new LocationClientOption();

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
            case R.id.about:
                Intent intent2 = new Intent();
                intent2.setClass(EditActivity.this, AboutActivity.class);
                startActivity(intent2);
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
                //findMap();
                String address = myListener.getAddr();
                myLocationText.setText(address);
                myListener.stop();
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

    @Override
    protected void onPause() {
        //myListener;
        mLocationClient.stop();
        super.onPause();
    }
}