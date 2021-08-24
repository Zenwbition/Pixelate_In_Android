package com.aiscreenshotdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnPixelateListener{

    private final int IMAGE_CODE = 0;
    private static final String IMAGE_UNSPECIFIED = "image/*";
    private String imageFormat = ".jpg";
    private Button btnSelect;
    private Button btnAIGO;
    private Button btnSave;
    private ImageView imgView;
    private TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSelect = (Button) findViewById(R.id.select);
        btnAIGO = (Button) findViewById(R.id.ai_go);
        btnSave = (Button) findViewById(R.id.save);
        imgView = (ImageView) findViewById(R.id.imageView);
        txt = (TextView) findViewById(R.id.txtView);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery(v);
            }
        });
    }

    public void openGallery(View view) {
        String[] permissions=new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,permissions,1);
        } else {
             selectPic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    selectPic();
                }
                else{
                    Toast.makeText(this,"operation denied!",Toast.LENGTH_LONG).show();
                }
        }
    }

    public void selectPic(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
        startActivityForResult(intent, IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = null;
        ContentResolver resolver = getContentResolver();

        try {

            Uri originalUri = data.getData(); // 获得图片的uri
            if (originalUri != null) {
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);

                imgView.setImageBitmap(bm);
                String[] proj = {MediaStore.Images.Media.DATA};

                Cursor cursor = managedQuery(originalUri, proj, null, null, null);

                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                txt.setText(path);

                imageFormat = path.substring(path.lastIndexOf("."));
                String fileName = TimeUtils.getSimpleDate() + imageFormat;
                Log.e("MainActivity", "fileName=" + fileName);
//                saveToSD(bm, fileName);

                //pixel 马赛克
                new Pixelate(bm)
                        .setDensity(12)
                        .setListener(this)
                        .make();
            } else {
                Bundle bundleExtras = data.getExtras();
                if (bundleExtras != null) {
                    Bitmap bitmaps = bundleExtras.getParcelable("data");
                    imgView.setImageBitmap(bitmaps);
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", e.toString());
        } finally {
            return;
        }
    }

    @Override
    public void onPixelated(Bitmap bitmap, int density) {
        //设置马赛克图片
        imgView.setImageBitmap(bitmap);

        String fileName = TimeUtils.getSimpleDate() + "-" + String.valueOf(density) + imageFormat;
        Log.e("MainActivity", "onPixelated fileName=" + fileName);
//        saveToSD(bitmap, fileName);
    }


}