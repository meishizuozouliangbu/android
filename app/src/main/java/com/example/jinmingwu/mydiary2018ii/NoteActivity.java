package com.example.jinmingwu.mydiary2018ii;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import me.itangqi.greendao.DaoMaster;
import me.itangqi.greendao.DaoSession;
import me.itangqi.greendao.NoteDao;

/**
 * Created by jinmingwu on 2018/6/19.
 */

public class NoteActivity extends ListActivity {
    private int number = 0; //记录ListView显示次数
    private SQLiteDatabase db;
    private EditText editText;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private Cursor cursor;
    public static final String TAG = "DaoExample";
    private Button buttonList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
        setupDatabase();
        // 获取 NoteDao 对象
        getNoteDao();
        refreshDown();
        editText = (EditText) findViewById(R.id.editTextNote);
        buttonList = (Button) findViewById(R.id.menuList);
    }

    public void refreshDown(){
        String textColumn = NoteDao.Properties.Title.columnName; //标题
        String textColumn2 = NoteDao.Properties.Comment.columnName; //日期
        String orderBy = NoteDao.Properties.Date.columnName + " COLLATE LOCALIZED ASC";
        cursor = db.query(getNoteDao().getTablename(), getNoteDao().getAllColumns(), null, null, null, null, orderBy);
        String[] from = {textColumn, textColumn2};
        int[] to = {android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, from, to);
        setListAdapter(adapter);
    }

    public void refreshUp(){
        String textColumn = NoteDao.Properties.Title.columnName; //标题
        String textColumn2 = NoteDao.Properties.Comment.columnName; //日期
        String orderBy = NoteDao.Properties.Date.columnName + " COLLATE LOCALIZED DESC";
        cursor = db.query(getNoteDao().getTablename(), getNoteDao().getAllColumns(), null, null, null, null, orderBy);
        String[] from = {textColumn, textColumn2};
        int[] to = {android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, from, to);
        setListAdapter(adapter);
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

    private NoteDao getNoteDao() {
        return daoSession.getNoteDao();
    }

    /**
     * Button 点击的监听事件
     *
     * @param view
     */
    public void onMyButtonClick(View view) {
        switch (view.getId()) {
            case R.id.menuAdd:
                //addNote();
                Intent intent = new Intent();
                intent.setClass(NoteActivity.this, EditActivity.class);
                intent.setAction(EditActivity.INSERT_DIARY_ACTION);
                intent.setData(getIntent().getData());
                startActivity(intent);
                break;
            case R.id.buttonSearch:
                search();
                break;
            case R.id.menuList:
                if(++number % 2 == 0){
                    refreshDown();
                    buttonList.setText("我的列表Down");
                } else {
                    refreshUp();
                    buttonList.setText("我的列表Up");
                }
                editText.setText("");
                Toast.makeText(getApplicationContext(),"已重新刷新列表",Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.d(TAG, "what has gone wrong ?");
                break;
        }
    }

//    private void addNote() {
//        String noteText = editText.getText().toString();
//        editText.setText("");
//
//        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
//        String comment = "Added on " + df.format(new Date());
//
//        // 插入操作，简单到只要你创建一个 Java 对象
//        Note note = new Note(null, noteText, comment, new Date());
//        getNoteDao().insert(note);
//        Log.d(TAG, "Inserted new note, ID: " + note.getId());
//        cursor.requery();
//    }

    private void search() {
        String noteText = editText.getText().toString();
        String textColumn = NoteDao.Properties.Title.columnName; //标题
        String textColumn2 = NoteDao.Properties.Comment.columnName; //日期
        String orderBy = NoteDao.Properties.Date.columnName + " COLLATE LOCALIZED ASC";
        cursor = db.query(getNoteDao().getTablename(), getNoteDao().getAllColumns(), "TITLE like ?", new String[]{"%"+noteText+"%"}, null, null, orderBy);
        String[] from = {textColumn, textColumn2};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, from, to);
        setListAdapter(adapter);
    }

    /**
     * ListView 的监听事件，用于删除一个 Item
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final long thisId = id;
        AlertDialog.Builder dialog = new AlertDialog.Builder(NoteActivity.this);
        dialog.setIcon(R.drawable.dialogedit);
        if(daoSession.getNoteDao().load(thisId).getTitle().toString().isEmpty() == true){
            dialog.setTitle("空内容");
        } else {
            dialog.setTitle(daoSession.getNoteDao().load(thisId).getTitle());
        }
        if(daoSession.getNoteDao().load(thisId).getDiary().toString().isEmpty() == true){
            dialog.setMessage("空内容");
        } else {
            dialog.setMessage(daoSession.getNoteDao().load(thisId).getDiary());
        }
        dialog.setNegativeButton("编辑", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent();
                intent.setClass(NoteActivity.this, EditActivity.class);
                intent.setAction(EditActivity.EDIT_DIARY_ACTION);
                Bundle bundle = new Bundle();
                bundle.putLong("id", daoSession.getNoteDao().load(thisId).getId());
                bundle.putString("title", daoSession.getNoteDao().load(thisId).getTitle());
                bundle.putString("diary", daoSession.getNoteDao().load(thisId).getDiary());
                bundle.putString("map", daoSession.getNoteDao().load(thisId).getMap());
                intent.putExtras(bundle);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),"正在编辑",Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setNeutralButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 删除操作，你可以通过「id」也可以一次性删除所有
                getNoteDao().deleteByKey(thisId);
                Log.d(TAG, "Deleted note, ID: " + thisId);
                cursor.requery();
                Toast.makeText(getApplicationContext(),"已删除",Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setPositiveButton("删除全部", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getNoteDao().deleteAll();
                Log.d(TAG, "Deleted note, ID: " + thisId);
                cursor.requery();
                Toast.makeText(getApplicationContext(),"已全部删除",Toast.LENGTH_SHORT).show();
            }
        });
        dialog.create();
        dialog.show();

    }
}
