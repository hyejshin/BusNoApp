package com.example.hyejung.r1315842_05;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by HyeJung on 2015-10-28.
 */
public class DisplayBus extends AppCompatActivity {

    private GoogleMap mMap;
    double minX=1000, minY=1000, currX, currY;
    String busNo="bus";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_map);

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = lm.getBestProvider(new Criteria(), true);
        Location location = lm.getLastKnownLocation(locationProvider);
        currY = location.getLatitude();
        currX = location.getLongitude();

        Intent it = getIntent();
        String busRouteId = it.getStringExtra("busRouteId");

        String serviceUrl = "http://ws.bus.go.kr/api/rest/buspos/getBusPosByRtid";
        String serviceKey = "VZk4EhfOFiAaRhqvId46im%2BuOcPd%2FzIiLaquayzXt6xEWy2G8n4hojoJnJel4KFwlDV5b5988PmYZZTx9mXWQw%3D%3D";
        String strUrl = serviceUrl + "?ServiceKey=" + serviceKey + "&busRouteId=" + busRouteId;

        new DownloadWebpageTask().execute(strUrl);
    }

    public void displayMap(String gpsX, String gpsY, String plainNo) {
        double latitude, longitude;
        latitude = Double.parseDouble(gpsY);
        longitude = Double.parseDouble(gpsX);

        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).
                icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)).title(plainNo));
        final LatLng LOC = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LOC, 15));
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
            displayBuspos(result);
        }

        private void displayBuspos(String result) {
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();
                boolean bSet_plainNo = false;
                boolean bSet_gpsX = false;
                boolean bSet_gpsY = false;
                String gpsX = "0", gpsY = "0", plainNo = "bus";

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        ;
                    } else if (eventType == XmlPullParser.START_TAG) {
                        String tag_name = xpp.getName();
                        if (tag_name.equals("gpsX"))
                            bSet_gpsX = true;
                        else if (tag_name.equals("gpsY"))
                            bSet_gpsY = true;
                        else if (tag_name.equals("plainNo"))
                            bSet_plainNo = true;
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (bSet_gpsX) {
                            gpsX = xpp.getText();
                            bSet_gpsX = false;
                        } else if (bSet_gpsY) {
                            gpsY = xpp.getText();
                            bSet_gpsY = false;
                        }else if (bSet_plainNo) {
                            plainNo = xpp.getText();
                            bSet_plainNo = false;
                            displayMap(gpsX, gpsY, plainNo);
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {
                //tv.setText(e.getMessage());
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
}
