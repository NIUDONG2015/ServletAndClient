package com.n1njac.testservlet;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private EditText acount,password;
    private Button login;
    private TextView content;

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            String s = (String) msg.obj;
            content.setText(s);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){

                    @Override
                    public void run() {
                        try {
                            String s = doPost(acount.getText().toString(),password.getText().toString());
                            Message message = Message.obtain();
                            message.obj = s;
                            handler.sendMessage(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    private void init() {
        acount = (EditText) findViewById(R.id.acount_ed);
        password = (EditText) findViewById(R.id.password_ed);
        login = (Button) findViewById(R.id.login_btn);
        content = (TextView) findViewById(R.id.content);
    }

    private String doPost(String name,String password) throws Exception {
        String path = "http://10.2.106.114:8088/test";
        String data = "username="+name+"&password="+password;

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        out.print(data);
        out.flush();

        String result = "";

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = in.readLine()) != null){
            result += line;
        }

        return result;
    }

    private void loginServlet(final String name, final String password) throws Exception {

        new Thread(){

            @Override
            public void run() {
                String path = "http://192.168.155.7:8088/test";
                String data = null;
                try {
                    data = "username" + URLEncoder.encode(name,"utf-8") +
                            "&password" + URLEncoder.encode(password,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                URL url = null;
                try {
                    url = new URL(path);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    conn.setRequestMethod("POST");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", data.length()+"");
                conn.setDoOutput(true);
                try {
                    conn.getOutputStream().write(data.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                InputStream is = null;
                try {
                    is = conn.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (conn.getResponseCode() == 200){

                        String result = new String(loadData(is),"GBK");
                        Message message = Message.obtain();
                        message.obj = result;
                        handler.sendMessage(message);

                        Log.i("xyz","来自服务器端的消息:" + result);
                        is.close();

                    }else {
                        String result = new String(loadData(is),"GBK");

                        Log.i("xyz","来自服务器端的消息:" + result);
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    ////将服务端向客户端发来的输入流里面的数据转化为字节数组的形式
    private static byte[] loadData(InputStream is) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int len  = -1;
        while ((len = is.read(bytes)) != -1){
            os.write(bytes,0,len);
        }
        is.close();
        os.close();
        return os.toByteArray();
    }
}
