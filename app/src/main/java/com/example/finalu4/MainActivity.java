package com.example.finalu4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;

    DatabaseHelper myDB;
    EditText editName,editSurname;
    Button btnAddData;
    Button buttonShowAll;
    TextView txtXml;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDB= new DatabaseHelper(this);
        txtXml = (TextView) findViewById(R.id.txtXml);

        editName = (EditText)findViewById(R.id.editText1);
        editSurname= (EditText)findViewById(R.id.editText2);
        btnAddData = (Button)findViewById(R.id.button_add);
        buttonShowAll = (Button)findViewById(R.id.button_show);



        sensorManager= (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if(sensor==null)
            finish();
        sensorEventListener= new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(sensorEvent.values[0]<sensor.getMaximumRange()){
                    getWindow().getDecorView().setBackgroundColor(Color.RED);
                }else{
                    getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        AddData();
        viewAll();
        start();
    }
    public void AddData(){
        btnAddData.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      boolean isInserted =  myDB.insertData(editName.getText().toString(),editSurname.getText().toString());
                      if(isInserted=true)
                          Toast.makeText(MainActivity.this, "Datos insertados", Toast.LENGTH_SHORT).show();
                      else
                          Toast.makeText(MainActivity.this, "Datos no insertados", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    public void viewAll(){
        buttonShowAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Cursor res =myDB.getAllData();
                        if(res.getCount()==0){
                            showMessage("Error","Datos no encontrados");
                            return;
                        }
                        StringBuffer buffer = new StringBuffer();
                        while(res.moveToNext()){
                            buffer.append("Id: "+res.getString(0)+ "\n");
                            buffer.append("Name: "+res.getString(1)+ "\n");
                            buffer.append("Surname: "+res.getString(2)+ "\n");
                        }
                        showMessage("Datos",buffer.toString());


                    }
                }
        );
    }
    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();

    }

    public void readXmlPullParser(View view){
        XmlPullParserFactory factory;
        FileInputStream fis =null;
        try{
            StringBuilder sb = new StringBuilder();
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            fis = openFileInput("sample.xml");

            xpp.setInput(fis,null);

            int eventType = xpp.getEventType();
            while (eventType!=XmlPullParser.END_DOCUMENT){
                if(eventType==XmlPullParser.START_DOCUMENT)
                    sb.append("[START]");
                else if(eventType==XmlPullParser.START_TAG)
                    sb.append("\n<"+xpp.getName()+">");
                else if(eventType==XmlPullParser.END_TAG)
                    sb.append("</"+xpp.getName()+">");
                else if(eventType == XmlPullParser.TEXT)
                    sb.append(xpp.getText());


                eventType = xpp.next();
            }


            txtXml.setText(sb.toString());
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally{
            if (fis!= null){
                try{
                    fis.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void start(){
        sensorManager.registerListener(sensorEventListener,sensor,2000*1000);
    }
    public void stop(){
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onPause() {
        stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        start();
        super.onResume();
    }
}