package com.example.jinmingwu.mydiary2018ii;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private EditText editTitle;
    private EditText editComment;
    private TextView editMap;
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private Cursor cursor;
    public static final String TAG = "DaoExample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        editTitle = (EditText)findViewById(R.id.editText);
        editComment = (EditText)findViewById(R.id.editText2);
        editMap = (TextView)findViewById(R.id.editmapText);

        // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
        setupDatabase();
        // 获取 NoteDao 对象
        getNoteDao();

    }

    public void onMyButtonClick(View view) {
        switch (view.getId()) {
            case R.id.edit_save: //保存
                this.addNote();
                Toast.makeText(getApplicationContext(),"已保存",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(EditActivity.this, NoteActivity.class);
                startActivity(intent);
                break;
            case R.id.edit_giveup: //返回
                finish();
                break;
            default:
                Log.d(TAG, "what has gone wrong ?");
                break;
        }
    }

    private void addNote() {
        String noteText = editTitle.getText().toString();
        String noteText2 = editComment.getText().toString();
        String map = editMap.getText().toString();

        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        String comment = "Added on " + df.format(new Date());

        // 插入操作，简单到只要你创建一个 Java 对象
        Note note = new Note(null, noteText, comment, new Date(), noteText2, map);
        getNoteDao().insert(note);
        Log.d(TAG, "Inserted new note, ID: " + note.getId());
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
