package com.keremtigre.mytravelbook2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    SQLiteDatabase database;
    ArrayList<String> addressList;
    ListView listView;
    ArrayAdapter arrayAdapter;
    ArrayList<Integer> idArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = this.openOrCreateDatabase("Location", MODE_PRIVATE, null);
        listView=findViewById(R.id.listView);
        addressList=new ArrayList<>();
        idArray=new ArrayList<>();
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,addressList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("id",idArray.get(i));
                startActivity(intent);
            }
        });


        getData();
    }
    public void getData(){
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM location", null);
            int addressIx = cursor.getColumnIndex("address");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {
                addressList.add(cursor.getString(addressIx));
                idArray.add(cursor.getInt(idIx));

            }
            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.intent_to_map,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(R.id.intentToMap==item.getItemId()){
            Intent intent=new Intent(MainActivity.this,MapsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}