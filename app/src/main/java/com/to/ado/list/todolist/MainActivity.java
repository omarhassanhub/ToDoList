package com.to.ado.list.todolist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.to.ado.list.todolist.db.TaskContract;
import com.to.ado.list.todolist.db.TaskDbHelper;
import com.to.ado.list.todolist.db.Tasks;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mHelper = new TaskDbHelper(this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        howToDialog();

        updateUI();

        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Tasks task = (Tasks) mTaskListView.getItemAtPosition(position);

                dialog(task);

            }
        });
    }

    public void dialog(final Tasks task) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this item?")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do here
                        deleteTask(task.getId());

                        updateUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void howToDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("How it works?")
                .setMessage("To add an item, PRESS the + sign" + "\n" + "To delete an item, PRESS the item on the list")

                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        updateUI();
                    }
                })

                .create();
        dialog.show();
    }


    private void updateUI() {
        ArrayList taskList = new ArrayList();


        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int title = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int _id = cursor.getColumnIndex(TaskContract.TaskEntry._ID);


            Tasks tasks = new Tasks();

            tasks.setId(cursor.getInt(_id));
            tasks.setTitle(cursor.getString(title));

            taskList.add(tasks);




        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this, R.layout.item_todo,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);


        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }
        cursor.close();
        db.close();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);


                                db.close();

                                updateUI();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();

                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                mTaskListView.setBackgroundColor(color);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteTask(long id) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        db.delete(TaskContract.TaskEntry.TABLE, TaskContract.TaskEntry._ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
        updateUI();
    }

}
