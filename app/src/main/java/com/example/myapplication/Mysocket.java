package com.example.myapplication;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;






public class Mysocket extends AsyncTask<ArrayList<String>, Integer, Void>
{
    String re = "? ";
    ProgressDialog progressDialog;
    String processFileName;
    int value = 0;

    Context context;

    public Mysocket(Context context) {
        this.context=context;

    }

    

    @Override
    protected void onPreExecute()
    {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMax(100);
        progressDialog.setTitle("正在上傳&計算");
        progressDialog.setMessage(processFileName);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

    }


    @Override
    protected Void doInBackground(ArrayList<String>... voids) {

        String message;
        ArrayList<String> totallist = voids[0];
        FileInputStream fis = null;

        for (int i = 0; i < totallist.size(); i++)
        {
            message = totallist.get(i);
            System.out.println("正在傳送第 : " + message);

            try {
                Socket s = new Socket("140.124.39.44",8080);
                System.out.println("連線成功！嘗試傳送檔案....");
                System.out.println("傳送圖片...");
                //獲取圖片字節流
                File file = new File(message);              // message 為 輸入路徑  類似這種("/sdcard/DCIM/Camera/P_20180910_214307.jpg");
                ///
                fis = new FileInputStream(file);
                System.out.println("message : " + message);
                System.out.println("fis : " + fis);
                System.out.println((file.getName().getBytes().length + "\r\n").getBytes());
                System.out.println(file.getName().getBytes());
                System.out.println(file.getName());
                System.out.println(file);

                processFileName = file.getName();
                System.out.println("processFileName : " + processFileName);

                // 用來判斷是白校正片還是物品照片，使用ttemp當判斷準則
                String [] temp = file.getParent().split("/");
                String ttemp = temp[temp.length-1];
                System.out.println("ttemp : " + ttemp);

                //獲取輸出流
                OutputStream out = s.getOutputStream();
                String FileName = file.getName();


                //寫入檔名，用 # 與資料作分割
                if (ttemp.equals("white"))
                {
                    out.write(("w" + FileName + "#").getBytes());
                }
                if (ttemp.equals("target"))
                {
                    out.write((FileName + "#").getBytes());
                }

                byte[] buf = new byte[1024];
                int len = 0, total_length = 0;
                long file_length = file.length();
                System.out.println("file_length : " + file_length);

                //2.往輸出流裡面投放數據
                while ((len = fis.read(buf)) != -1)
                {
                    out.write(buf,0,len);
                }


                value = (int)(((float)i/(float) totallist.size())*100);         //計算傳送進度    value顯示為百分比形式
                System.out.println("i : " + i);
                System.out.println("totallist.size() : " + totallist.size());
                System.out.println("value : " + value);
                publishProgress(value);

                //通知服務端，數據發送完畢
                s.shutdownOutput();
                //3.獲取輸出流，接受服務器傳送過來的消息"上傳成功"
                InputStream in = s.getInputStream();


                //創建target檔案之資料夾
                File del = new File(MainActivity.target_path);
                File[] files = del.listFiles();

                while (in != null)
                {
                    System.out.println("成功接收到值");

                    byte[] bufIn = new byte[1024];
                    int num = in.read(bufIn);
                    re = new String(bufIn,0,num);

                    //刪除target資料夾內之資料
                    for (int u = 0; u < files.length; u++) {
                        files[u].delete();

                        System.out.println("刪除 file :" + files[u].getName());
                    }
                    System.out.println("成功接收到值2");

                }
                //關閉
                fis.close();
                out.close();
                in.close();
                s.close();



            }
            catch(Exception e) {
                e.printStackTrace();
                //dialog("系統訊息", "未連接上伺服器");
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {

        progressDialog.setMessage(processFileName);
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);

    }

    protected void onPostExecute(Void result)
    {
        super.onPostExecute(result);
        MainActivity.res.setText("分析結果 : " + re);
        progressDialog.dismiss();
    }
}





