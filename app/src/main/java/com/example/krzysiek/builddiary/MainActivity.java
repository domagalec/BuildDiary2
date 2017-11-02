package com.example.krzysiek.builddiary;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;

import com.example.krzysiek.builddiary.Item.Status;


public class MainActivity extends Activity {

        private static final int ADD_TODO_ITEM_REQUEST = 0;
        private static final String FILE_NAME = "BuilderDiaryData.txt";
        private static final String TAG = "BuilderDiary";

        // IDs for menu items
        private static final int MENU_DELETE = Menu.FIRST;
        private static final int MENU_DUMP = Menu.FIRST + 1;


        ItemListAdapter mAdapter;

        ListView listView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Create a new TodoListAdapter for this ListActivity's ListView

            Button addButton;

            mAdapter = new ItemListAdapter(getApplicationContext());

            // Put divider between ToDoItems and FooterView
        //    getListView().setFooterDividersEnabled(true);

            // BYŁODOZROBIENIA - Inflate footerView for footer_view.xml file
           // LayoutInflater inflater = (LayoutInflater)this.getSystemService
           // 		(this.LAYOUT_INFLATER_SERVICE);
           // TextView footerView = (TextView) findViewById(R.id.footerView);
           // TextView footerView = (TextView) ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view, null, false);
        //    LayoutInflater inflater = LayoutInflater.from(this);
        //    TextView footerView = (TextView) inflater.inflate(R.layout.footer_view, null);
            // NOTE: You can remove this block once you've implemented the assignment
            //if (null == footerView) {
            //	return;
            //}
            //DOZROBIENIABYLO - Add footerView to ListView

        //   getListView().addFooterView(footerView);

            // Attach Listener to FooterView
          addButton = (Button) findViewById(R.id.addbutton);



        //    footerView.setOnClickListener(new OnClickListener() {
        //        @Override
        //        public void onClick(View v) {
        //            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
        //            startActivityForResult(intent, ADD_TODO_ITEM_REQUEST);


                    //może FOR RESULT? Implement OnClick().
        //        }
        //    });

            // - Attach the adapter to this ListActivity's ListView
          //  setListAdapter(mAdapter);
            setContentView(R.layout.main);
            listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(mAdapter);

            addButton = (Button) findViewById(R.id.addbutton);
            addButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                    startActivityForResult(intent, ADD_TODO_ITEM_REQUEST);
                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            Log.i(TAG,"Entered onActivityResult()");

            // Check result code and request code
            // if user submitted a new ToDoItem
            // Create a new ToDoItem from the data Intent
            // and then add it to the adapter
            if (requestCode == ADD_TODO_ITEM_REQUEST) {
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {

                    Item newItem = new Item(data);
                    mAdapter.add(newItem);
                    // The user picked a contact.
                    // The Intent's data Uri identifies which contact was selected.
                    //String result=data.getStringExtra("zwrot");
                    //mUserTextView.setText(result);
                    // Do something with the contact here (bigger example below)
                }
            }
        }

        // Do not modify below here

        @Override
        public void onResume() {
            super.onResume();

            // Load saved ToDoItems, if necessary

            if (mAdapter.getCount() == 0)
                loadItems();
        }

        @Override
        protected void onPause() {
            super.onPause();

            // Save ToDoItems

            saveItems();

        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);

            menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete all");
            menu.add(Menu.NONE, MENU_DUMP, Menu.NONE, "Dump to log");
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_DELETE:
                    mAdapter.clear();
                    return true;
                case MENU_DUMP:
                    dump();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        private void dump() {

            for (int i = 0; i < mAdapter.getCount(); i++) {
                String data = ((Item) mAdapter.getItem(i)).toLog();
                Log.i(TAG,	"Item " + i + ": " + data.replace(Item.ITEM_SEP, ","));
            }

        }

        // Load stored ToDoItems
        private void loadItems() {
            BufferedReader reader = null;
            try {
                FileInputStream fis = openFileInput(FILE_NAME);
                reader = new BufferedReader(new InputStreamReader(fis));

                String title = null;
                Double cost = null;
                String status = null;
                Date date = null;

                while (null != (title = reader.readLine())) {
                    cost = Double.valueOf(reader.readLine());
                    status = reader.readLine();
                    date = Item.FORMAT.parse(reader.readLine());
                    mAdapter.add(new Item(title, cost,
                            Status.valueOf(status), date));
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Save ToDoItems to file
        private void saveItems() {
            PrintWriter writer = null;
            try {
                FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        fos)));

                for (int idx = 0; idx < mAdapter.getCount(); idx++) {

                    writer.println(mAdapter.getItem(idx));

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != writer) {
                    writer.close();
                }
            }
        }
    }