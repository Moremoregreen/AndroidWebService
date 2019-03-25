package com.moremoregreen.androidwebservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Button btn_Reset, btn_DJSON, btn_KSOAP ;
    EditText ed_tempInput;
    TextView tv_Title , tv_tempResult;
    ListView dataListView;
    private NetworkReceiver receiver = new NetworkReceiver();
    public static boolean internetConn = true;
    // 網路
    public static String sPref = null;
    protected static boolean wifiConnected = false;
    protected static boolean mobileConnected = false;

    private ArrayList<String> mDataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_Reset = findViewById(R.id.main_btnReset);
        btn_KSOAP = findViewById(R.id.main_btnKSOAP);
        btn_DJSON = findViewById(R.id.main_btnDJSON);
        ed_tempInput = findViewById(R.id.input);
        tv_tempResult = findViewById(R.id.tempResult);
        dataListView = findViewById(R.id.main_listView);
        tv_Title = findViewById(R.id.tv_Title);

        btn_DJSON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConn) {
                    tv_Title.setText(getString(R.string.str_parsing));
                    // 呼叫DownloadTask連線WebService進行資料取得與解析，傳入URL
                    new DownloadTask().execute(
                            "https://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=1316a13a-39cd-4db5-b663-930638f244c5");
                } else {
                    tv_Title.setText(getString(R.string.connection_error));
                }
            }
        });
        btn_KSOAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetConn) {
                    tv_Title.setText(getString(R.string.str_parsing));
                    new DownloadTaskSOAP().execute(
                            ed_tempInput.getText().toString());
                } else {
                    tv_Title.setText(getString(R.string.connection_error));
                }
            }
        });
        btn_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_Title.setText(getString(R.string.app_name));
                dataListView.setAdapter(null);
            }
        });
    }

    // 依據網路狀態改變變數wifiConnected或mobileConnected
    @Override
    protected void onStart() {
        super.onStart();
        ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            wifiConnected = info.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = info.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }

    // onResume()時，註冊一BroadcastReceiver (receiver物件)，捕捉兩個事件
    // 1.ConnectivityManager.CONNECTIVITY_ACTION與2.android.net.conn.CONNECTIVITY_CHANGE
    // 當發生網路連線事件或網路連線改變事件時，被自訂的NetworkReceiver()接收
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.new.wifi.STATE_CHANGE");
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //離開程式記得解除註冊
        this.unregisterReceiver(receiver);
    }

    // 偵測網路狀態
    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                wifiConnected = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                mobileConnected = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            } else {
                wifiConnected = false;
                mobileConnected = false;
            }
            // 網路狀態改變時，以Toast顯示連線狀態
            if (wifiConnected || mobileConnected) {
                internetConn = true;
                Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();
            } else {
                internetConn = false;
                Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
                // 失去連線，清除ListView內容
                dataListView.setAdapter(null);
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        //背景執行
        @Override
        protected String doInBackground(String... url) {
            return downloadUrl(url[0]); //url[0] = Web Service URL
        }

        // 下載完成後，開始解析JSON
        @Override
        protected void onPostExecute(String result) {
            tv_Title.setText(R.string.str_parsing_ok);
            mDataList = new ArrayList<String>();
            //開始解析JSON
            try {
                //result = JSON原始字串
                JSONObject json = new JSONObject(result);
                //取得原始字串中的result區段物件
                JSONObject resultObject = json.getJSONObject("result");
                //在取得區段中的results資料陣列(內含公司名字、座標、有無通過....)
                String data = resultObject.getString("results");
                // 將上面data陣列分割成可操作
                JSONArray dataArray = new JSONArray(data);
                // 宣告陣列name，用來儲存dataArray內分割出的資料
                String[] sbmName = new String[dataArray.length()];
                for (int i = 0; i < dataArray.length(); i++) {
                    //取出資料放到mDataList
                    mDataList.add(dataArray.getJSONObject(i).getString("SBMNAME") + " \t" +
                            dataArray.getJSONObject(i).getString("RESULT") + "\n" +
                            dataArray.getJSONObject(i).getString("SBMXADDR"));
                }
                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, mDataList);
                dataListView.setAdapter(adapter);
                dataListView.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.str_user_choose) + mDataList.get(position), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DownloadTaskSOAP extends AsyncTask<String, Void, String> {
        //背景執行
        @Override
        protected String doInBackground(String... url) {
            return downloadUrl(url[0]); //url[0] = Web Service URL
        }

        @Override
        protected void onPostExecute(String result) {
            tv_Title.setText(R.string.str_parsing_ok);
           tv_tempResult.setText("轉換成華氏溫度:" + result);
        }
    }

    private String downloadUrlKSOAP(String url){
        WSCelsiusToFahrenheit  wsc = new WSCelsiusToFahrenheit();
        String convert = wsc.tempconvert(url);
        return convert;
    }

    private String downloadUrl(String url) {
        String downloadHTML = "";
        try {
            URL urlAddress = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlAddress.openConnection();
            conn.setReadTimeout(5000);//ms
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            if (is != null) {
                int leng = 0;
                byte[] Data = new byte[100];
                byte[] totalData = new byte[0];
                int totallength = 0;
                do {
                    leng = is.read(Data);
                    if (leng > 0) {
                        totallength += leng;
                        byte[] temp = new byte[totallength];
                        System.arraycopy(totalData, 0, temp, 0, totalData.length);
                        System.arraycopy(Data, 0, temp, totalData.length, leng);
                        totalData = temp;
                        Log.d(TAG, "totalData: " + totalData);
                    }
                } while (leng > 0);
                downloadHTML = new String(totalData, "UTF-8");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloadHTML;
    }
}

