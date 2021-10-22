package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;




import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.BreakIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {               // extends: 繼承父類別(AppCompatActivity)的參數
    //private static final Object ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION = 0;
    public static Dialog progressDialog;

    TextView tt;
    static TextView rr;
    static TextView res;
    Button button1;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    Socket s;
    DataOutputStream dos;
    String imagepath = null;
    static int a = 100;
    static String white_path;
    static String target_path;
    String filenameTOMY;
    View view;
    RadioGroup rg;
    static String target_label = "unselected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.black);

        //el = (EditText)findViewById(R.id.input_Text);     // 連結介面上的物件  input_Text為介面上物件的名稱
        //tt = (TextView)findViewById(R.id.textView);
        //rr = (TextView)findViewById(R.id.textView2);
        res = (TextView)findViewById(R.id.textView3);
        //imageview = (ImageView)findViewById((R.id.imageView));

        // Button 初始化的步驟
        //button = (Button)findViewById(R.id.buttonLoadPicture);
        //button.setOnClickListener(buttonLoadPicture);

        rg = (RadioGroup)findViewById(R.id.radioGroup);


        button1 = (Button)findViewById(R.id.button);
        dialog("系統提示", "使用前\n\n請將白校正照片放入->'Download'-> 'white'資料夾 \n\n請將待測品照片放入->'Download'-> 'target'資料夾");
        white_path = creatfile("white");
        target_path = creatfile("target");



        //
        // OTG 讀寫
        Intent intent = getIntent();
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        String deviceName = usbDevice.getDeviceName();
        int deviceVendor = usbDevice.getVendorId();
        StringCharacterIterator deviceText = null;
        deviceText.setText("#Device Info : "+usbDevice.toString()+"\n"+"#Device Name : "+
        deviceName+"\n"+"#Device Vendor : "+deviceVendor);

    }

    List<String> UUri = new ArrayList<>();

    public void choose()
    {
        switch(rg.getCheckedRadioButtonId()) {
            case R.id.mushroom:
                target_label = "label_of_mushroom";
                break;
            case R.id.phalaenopsis:
                target_label = "label_of_phalaenopsis";
                break;
        }
    }

    // 創建資料夾
    public String creatfile(String filename)
    {
        //File file = new File(Environment.getExternalStorageDirectory() ,  filename);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),  filename);
        file.mkdirs();
        try{
            file.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if(file.mkdirs()){
            System.out.println("成功建立");
            dialog("系統訊息", "成功建立");
        }
        else{
            System.out.println("建立失敗");
            if(file.exists()){
                System.out.println("檔案已存在");
                System.out.println("path : " + file.getAbsolutePath());
                //dialog("系統訊息", "建立失敗，檔案已存在" + file.getAbsolutePath());
            }
            else{
                System.out.println("檔案不存在");
                dialog("系統訊息", "檔案不存在，請開啟裝置儲存權限");
            }
        }
        //dialog("路徑", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    //彈出視窗
    public void dialog(String title, String message)
    {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })//.setNegativeButton("cancel",null)
                .create()
                .show();

    }


    // 設定按下 button 來觸發相簿
    public View.OnClickListener buttonLoadPicture = new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            tt.setText("");
            rr.setText("等待上傳");
            if (a == 100)
            {
                dialog("ERROR MESSAGE","please choose 'white' or 'target' first!");
            }
            else{
                //openUsbDevice();
                openGallery();
            }
        }
    };


    // 開啟手機相簿
    public void openGallery()              //  開啟手機相簿
    {
        /*Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(gallery, PICK_IMAGE);  */          //開啟第二個畫面
        //Intent intent = new Intent(Intent.ACTION_PICK);

        UUri.clear();

        Intent intent = new Intent();

        intent.setType("image/**");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        //intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setAction(Intent.ACTION_PICK);
        //startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE);
        startActivityForResult(intent, PICK_IMAGE);


    }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);


            if (resultCode == RESULT_OK && requestCode == PICK_IMAGE)       //為了確保沒有選擇照片時按了上一頁的按鍵，手機不會閃退
            {
                List<Bitmap> bitmaps = new ArrayList<>();
                ClipData clipData = data.getClipData();
                if (clipData != null)                   //若有選取的照片
                {
                    for(int i = 0; i < clipData.getItemCount(); i++)
                    {
                        imageUri = clipData.getItemAt(i).getUri();                  //圖片路徑
                        try {
                            InputStream is = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            bitmaps.add(bitmap);


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        imagepath = getPath(imageUri);              //轉換為絕對路徑
                        UUri.add(imagepath);                    //增添到LIST裡面
                        //tt.setText(imagepath);
                        String [] asdsa = UUri.get(i).split("/");
                        String imagename = asdsa[asdsa.length-1];
                        System.out.println(imagename);
                        if(tt.getText().toString().equals(""))
                        {
                            tt.setText("已選擇:  " + imagename);
                        }
                        else
                        {
                            tt.setText(tt.getText() + "\n" + imagename);
                        }

                    }
                    System.out.println(UUri);
                    String aa = UUri.get(0);
                    int qqq = UUri.size();
                    System.out.println(aa);
                    System.out.println(qqq);

                }else{
                    imageUri = data.getData();
                    try {
                        InputStream is = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }

        }




    public String getPath(Uri uri)              //獲取絕對路徑
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void send(View v) {

        choose();
        //openGallery();
        if (target_label.equals("unselected"))
        {
            dialog("系統訊息", "請先選擇測試資料");
        }
        else {
            if (target_label.equals("label_of_mushroom"))
            {
                socket_label messageSender = new socket_label();
                messageSender.execute(target_label);
            }
            if (target_label.equals("label_of_phalaenopsis"))
            {
                socket_label messageSender = new socket_label();
                messageSender.execute(target_label);
            }
            res.setText("分析結果 : 分析中...");
            int image_number = 7;

            ArrayList<String> total = new ArrayList<>();

            File white = new File(white_path);          //抓取白校正之文件
            File [] white1 = white.listFiles();

            File target = new File(target_path);        //抓取待測物文件
            File [] target1 = target.listFiles();

            if (white.listFiles().length == image_number && target.listFiles().length == image_number)
            {
                //白校正文件上傳
                for(int i = 0; i < white.listFiles().length; i++)
                {
                    String path = white1[i].getAbsolutePath();
                    total.add(path);
                }
                //待測物文件上傳
                for(int i = 0; i < target.listFiles().length; i++)
                {
                    String path = target1[i].getAbsolutePath();
                    total.add(path);
                }

                Mysocket messageSender = new Mysocket(MainActivity.this);
                messageSender.execute(total);
            }
            else{
                dialog("系統提示", "輸入資料不對\n\n請檢查'white'與'target'資料夾");
                //dialog("白色數量", String.valueOf(white.listFiles().length));
                //dialog("目標數量", String.valueOf(target.listFiles().length));
            }
        }
    }



}