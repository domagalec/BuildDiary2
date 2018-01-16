package com.example.krzysiek.builddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity {

        private static final int ADD_ITEM_REQUEST = 1;
        private static final int EDIT_ITEM_REQUEST = 2;
        private static final String FILE_NAME = "BuildDiaryData.txt";
        private static final String TAG = "BuildDiary";

        // IDs for menu items
        private static final int MENU_DELETE = Menu.FIRST;
        private static final int MENU_BUDGET = Menu.FIRST + 1;

        ItemListAdapter mAdapter;
        TextView summaryView;
        ListView listView;
        TextView showDetails;
        Button addButton;
        ProgressBar budgetBar;
        TextView percentView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SharedPreferences settings = getSharedPreferences("preferences", MODE_PRIVATE);
            settings.getString("budget", "0");

            if(getSupportActionBar() != null){
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayUseLogoEnabled(true);

            // Create a new TodoListAdapter for this ListActivity's ListView
            mAdapter = new ItemListAdapter(getApplicationContext());

            setContentView(R.layout.main);

            summaryView = (TextView) findViewById(R.id.summaryView);
            listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(mAdapter);
            registerForContextMenu(listView);
            budgetBar = (ProgressBar) findViewById(R.id.budgetBar);
            percentView = (TextView) findViewById(R.id.percentView);

            showDetails = (Button) findViewById(R.id.ShowDetails);
            showDetails.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    final LinkedHashMap<String,String> listMap =  new LinkedHashMap<String, String>();
                    String catArr[] = getResources().getStringArray(R.array.category_array);

                    for (String aCatArr : catArr) {
                        double categoryCost = 0.0;

                        for (int j = 0; j < mAdapter.getCount(); j++) {
                            Item item = (Item) mAdapter.getItem(j);
                            if (item.getCategory().equals(aCatArr)) {
                                categoryCost = categoryCost + item.getCost();
                            }
                        }
                        if (categoryCost != 0.0) {
                            listMap.put(aCatArr, String.valueOf(new DecimalFormat("##.00").format(categoryCost)));
                        }
                        Log.i("TAG", aCatArr);
                    }

                    final AlertDialog.Builder detailsBuilder = new AlertDialog.Builder(MainActivity.this);
                    detailsBuilder.setTitle(R.string.summary);

                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View dialogView = inflater.inflate(R.layout.details_dialog, null);
                    detailsBuilder.setView(dialogView);
                    detailsBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            listMap.clear();
                            dialog.dismiss();
                        }
                    });

                    ListView detailsView = (ListView) dialogView.findViewById(R.id.detailsView);

                    DetailsAdapter adapter = new DetailsAdapter(listMap);

                    detailsView.setAdapter(adapter);

                    detailsBuilder.create();
                    detailsBuilder.show();
                }
            });

            addButton = (Button) findViewById(R.id.addbutton);
            addButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                    startActivityForResult(intent, 1);
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Intent intent = new Intent(MainActivity.this, EditItemActivity.class);

                    Item editedItem = (Item) adapterView.getItemAtPosition(position);

                    intent.putExtra("position", position);
                    intent.putExtra("title", editedItem.getTitle());
                    intent.putExtra("cost", editedItem.getCost());
                    intent.putExtra("date", editedItem.getDate());
                    intent.putExtra("category", editedItem.getCategory());

                    startActivityForResult(intent, 2);
                }
            });
        }

    @Override
    public void onCreateContextMenu(ContextMenu menu,View v, ContextMenu.ContextMenuInfo menuInfo){
        if (v.getId() == R.id.listView){
            AdapterView.AdapterContextMenuInfo info =(AdapterView.AdapterContextMenuInfo)menuInfo;
            MenuItem mnu1=menu.add(0,0,0,R.string.menu_edit);
            MenuItem mnu2=menu.add(0,1,1,R.string.menu_delete);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem){
        AdapterView.AdapterContextMenuInfo info=(AdapterView.AdapterContextMenuInfo)menuItem.getMenuInfo();
        int position = ((AdapterView.AdapterContextMenuInfo)info).position;

        switch (menuItem.getItemId()) {
            case 0:
                Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
                Item editedItem = (Item) listView.getItemAtPosition(position);

                //Toast.makeText(getApplicationContext(), "selected Item Name is " + editedItem.getTitle(), Toast.LENGTH_LONG).show();

                intent.putExtra("position", position);
                intent.putExtra("title", editedItem.getTitle());
                intent.putExtra("cost", editedItem.getCost());
                intent.putExtra("date", editedItem.getDate());
                intent.putExtra("category", editedItem.getCategory());

                startActivityForResult(intent, 2);
                break;
            case 1:
                mAdapter.remove(position);
                totalCostUpdate();
                budgetBarUpdate();
                Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
        return true;
    }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            Log.i(TAG, "Entered onActivityResult()");

            // Check result code and request code
            // if user submitted a new Item
            // Create a new Item from the data Intent
            // and then add it to the adapteR
            if (resultCode == RESULT_OK) {
                if (requestCode == ADD_ITEM_REQUEST) {
                    Item newItem = new Item(data);
                    mAdapter.add(newItem);
                    listView.setSelection(mAdapter.getCount());
                    Log.i(TAG, "Added new item");
                    totalCostUpdate();
                    budgetBarUpdate();
                }
                else if (requestCode == EDIT_ITEM_REQUEST) {
                    int position = 0;
                    Item editedItem = new Item(data);
                    position = data.getIntExtra("position", 0);
                    mAdapter.edit(editedItem, position);
                    Log.i(TAG, "Edited item");
                    totalCostUpdate();
                    budgetBarUpdate();
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            // Load saved Items, if necessary

            if (mAdapter.getCount() == 0)
                loadItems();
                totalCostUpdate();
                budgetBarUpdate();
        }


        @Override
        public void onStart() {
        super.onStart();

        // Load saved Items, if necessary

        if (mAdapter.getCount() == 0)
            loadItems();
            totalCostUpdate();
            budgetBarUpdate();
        }

        @Override
        protected void onStop() {
        super.onStop();

        saveItems();
        }

        @Override
        protected void onDestroy() {
        super.onDestroy();

        saveItems();
        }

        @Override
        protected void onPause() {
            super.onPause();

            saveItems();
        }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);

            menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete_all);
            menu.add(Menu.NONE, MENU_BUDGET, Menu.NONE, R.string.set_budget);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_DELETE:

                    AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this);
                    deleteDialogBuilder.setTitle(R.string.are_you_sure)
                        .setMessage(R.string.cannot_undone)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                            mAdapter.clear();
                            totalCostUpdate();
                            budgetBarUpdate();
                            dialog.dismiss();
                        }
                    })

                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog deleteDialog = deleteDialogBuilder.create();
                    deleteDialog.show();

                    return true;

                case MENU_BUDGET:
                    AlertDialog.Builder budgetDialogBuilder = new AlertDialog.Builder(this);

                    final EditText budgetEditText = new EditText(MainActivity.this);
                    budgetEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    budgetEditText.setSelectAllOnFocus(true);
                    SharedPreferences settings = getSharedPreferences("preferences", MODE_PRIVATE);
                    if (!settings.getString("budget", "0").equals("0")) {
                        budgetEditText.setText(settings.getString("budget", "0"));
                    }
                    budgetDialogBuilder.setMessage(R.string.set_budget)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if(!budgetEditText.getText().toString().equals("")){
                                        double budget = Double.parseDouble(budgetEditText.getText().toString());
                                        if (budget>0){
                                            if ((int) Math.round(getTotalCost()*100/budget) <=100) {
                                                budgetBar.setProgress((int) Math.round(getTotalCost() * 100 / budget));
                                                percentView.setText(String.valueOf((int) Math.round(getTotalCost() * 100 / budget))+ "%");
                                                percentView.setTextColor(Color.BLACK);
                                            }
                                            else {
                                                budgetBar.setProgress(100);
                                                percentView.setText(String.valueOf((int) Math.round(getTotalCost() * 100 / budget))+ "%");
                                                percentView.setTextColor(Color.RED);
                                            }
                                        }
                                        else {
                                            budgetBar.setProgress(0);
                                            percentView.setText("0%");
                                            percentView.setTextColor(Color.BLACK);
                                        }
                                    }
                                    else {
                                        budgetBar.setProgress(0);
                                        percentView.setText("0%");
                                        percentView.setTextColor(Color.BLACK);
                                    }

                                    SharedPreferences settings = getSharedPreferences("preferences", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString("budget", budgetEditText.getText().toString());
                                    editor.apply();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.dismiss();
                                }
                            })
                            .setView(budgetEditText);

                    // Create the AlertDialog object and return it
                    AlertDialog budgetDialog = budgetDialogBuilder.create();
                    budgetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    budgetDialog.show();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        // Load stored Items
        private void loadItems() {
            BufferedReader reader = null;
            try {
                FileInputStream fis = openFileInput(FILE_NAME);
                reader = new BufferedReader(new InputStreamReader(fis));

                String title = null;
                String cost = null;
                //String status = null;
                Date date = null;
                String category = null;

                while (null != (title = reader.readLine())) {
                    cost = reader.readLine();
                    //status = reader.readLine();
                    date = Item.FORMAT.parse(reader.readLine());
                    category = reader.readLine();
                    mAdapter.add(new Item(title, Double.valueOf(cost) /*Status.valueOf(status)*/, date, category));

                }
                listView.setSelection(mAdapter.getCount());


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

        // Save Items to file
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

        public void totalCostUpdate(){

            double total = 0;

            for (int i = 0; i < mAdapter.getCount(); i++) {
                   total = total+((Item) mAdapter.getItem(i)).getCost();
            }
            if (total == 0){
                summaryView.setText(new DecimalFormat("0.00").format(total));
            }
            else {
                summaryView.setText(new DecimalFormat("##.00").format(total));
            }
        }

        public double getTotalCost(){
            double total = 0;
            for (int i = 0; i < mAdapter.getCount(); i++) {
                total = total+((Item) mAdapter.getItem(i)).getCost();
            }
            return total;
        }

        public void budgetBarUpdate(){
            SharedPreferences settings = getSharedPreferences("preferences", MODE_PRIVATE);
            settings.getString("budget", "0");
            Double total = getTotalCost();
            //Toast.makeText(MainActivity.this, String.valueOf(total), Toast.LENGTH_SHORT).show();

            if(!settings.getString("budget", "0").equals("")){
                double budget = Double.parseDouble(settings.getString("budget", "0"));
                if (budget>0){
                    if ((int) Math.round(getTotalCost()*100/budget) <=100) {
                        budgetBar.setProgress((int) Math.round(getTotalCost() * 100 / budget));
                        percentView.setText(String.valueOf((int) Math.round(getTotalCost() * 100 / budget))+ "%");
                        percentView.setTextColor(Color.BLACK);

                }
                    else {
                        budgetBar.setProgress(100);
                        percentView.setText(String.valueOf((int) Math.round(getTotalCost() * 100 / budget))+ "%");
                        percentView.setTextColor(Color.RED);                    }
                }
                else {
                    budgetBar.setProgress(0);
                    percentView.setText("0%");
                    percentView.setTextColor(Color.BLACK);

                }
            }
            else {
                budgetBar.setProgress(0);
                percentView.setText("0%");
                percentView.setTextColor(Color.BLACK);
            }
        }
    }