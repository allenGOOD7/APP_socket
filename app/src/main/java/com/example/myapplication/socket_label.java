package com.example.myapplication;
import android.os.AsyncTask;
import java.io.OutputStream;
import java.net.Socket;




public class socket_label extends AsyncTask<String, Void, Void>
{

    @Override
    protected Void doInBackground(String... voids) {

        String message = voids[0];
        try
        {
            Socket s = new Socket("140.124.39.44",8080);
            //獲取輸出流
            OutputStream out = s.getOutputStream();

            out.write((message + "#").getBytes());

            //通知服務端，數據發送完畢
            s.shutdownOutput();
            //關閉
            out.close();
            s.close();



        }catch(Exception e) {
            e.printStackTrace();
        }



        return null;
    }
}
