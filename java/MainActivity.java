package com.example.hyejung.r1315842_05;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void findBusRouteID(View v){
        EditText t = (EditText)findViewById(R.id.editText);
        t2 = (EditText)findViewById(R.id.editText2);
        String busNum = t.getText().toString();

        String serviceUrl = "http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList";
        String serviceKey = "VZk4EhfOFiAaRhqvId46im%2BuOcPd%2FzIiLaquayzXt6xEWy2G8n4hojoJnJel4KFwlDV5b5988PmYZZTx9mXWQw%3D%3D";
        String strUrl = serviceUrl+"?ServiceKey="+serviceKey+"&strSrch="+busNum;

        new DownloadWebpageTask().execute(strUrl);
    }
    public void bus(View v){
        String busRouteId = t2.getText().toString();

        Intent it = new Intent(this, DisplayBus.class);
        it.putExtra("busRouteId", busRouteId);
        startActivity(it);
    }
    public class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String) downloadUrl((String) urls[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        protected void onPostExecute(String result) {
            try{
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();
                boolean bSet = false;
                boolean onGoing = true;
                while(eventType != XmlPullParser.END_DOCUMENT && onGoing){
                    if(eventType == XmlPullParser.START_DOCUMENT){
                        ;
                    }else if(eventType == XmlPullParser.START_TAG){
                        String tag_name = xpp.getName();
                        if(tag_name.equals("busRouteId") )
                            bSet = true;
                    }else if(eventType == XmlPullParser.TEXT){
                        if(bSet){
                            String content = xpp.getText();
                            t2.setText(content);
                            bSet = false;
                            onGoing = false;
                        }
                    }else if(eventType == XmlPullParser.END_TAG){
                        ;
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {

            }
        }

        private String downloadUrl(String myurl) throws IOException {

            HttpURLConnection conn = null;
            try {
                URL url = new URL(myurl);
                conn = (HttpURLConnection) url.openConnection();
                BufferedInputStream buf = new BufferedInputStream(conn.getInputStream());
                BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf, "utf-8"));
                String line = null;
                String page = "";
                while ((line = bufreader.readLine()) != null) {
                    page += line;
                }

                return page;
            } finally {
                conn.disconnect();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
