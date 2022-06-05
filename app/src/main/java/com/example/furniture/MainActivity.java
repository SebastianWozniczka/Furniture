package com.example.furniture;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.furniture.ml.Model;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    public static ArrayList<String> res,latest;
    public static final String SHARED_PREFS="com.example.furniture.sharedPrefs";
    public static final String TEXT="text";


    @BindView(R.id.button) Button camera;
    @BindView(R.id.button2) Button gallery;
    @BindView(R.id.button3) Button add;
    @BindView(R.id.openA1) Button latestButton;
    @BindView(R.id.result) TextView result;
    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.list) ListView listView;

    int imageSize = 32;
    ArrayAdapter<String> adapter;
    Bitmap image;
    String[] classes;
    int maxPos;
    String s;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        dynamicShortcut();

        ButterKnife.bind(this);

        res = new ArrayList<>();
        latest=new ArrayList<>();
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });


        if (savedInstanceState != null) {

            image = (Bitmap) savedInstanceState.getParcelable("BitmapImage");
            s=savedInstanceState.getString("Result");
            imageView.setImageBitmap(image);
            result.setText(s);
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(new CanvasView(getApplicationContext()));
            }
        });

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.activity_list_item, android.R.id.text1,res);
        listView.setAdapter(adapter);


        if(getSharedPreferences(SHARED_PREFS,MODE_PRIVATE)!=null)
        latestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                latest=fetchAllPreference();
                Intent intent = new Intent(MainActivity.this, ActivityLatest.class);
                startActivity(intent);
            }
        });

    }

    private void dynamicShortcut() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getApplicationContext().getSystemService(ShortcutManager.class);
            ShortcutInfo shortcutTwo = new ShortcutInfo.Builder(getApplicationContext(), "shortcutTwo")
                    .setShortLabel(getApplicationContext().getResources().getString(R.string.app_name))
                    .setLongLabel(getApplicationContext().getResources().getString(R.string.app_name))
                    .setIcon(Icon.createWithResource(getApplicationContext(), R.mipmap.ic_tlo))
                    .setIntents(new Intent[] {
                            new Intent(getApplicationContext(), MainActivity.class)
                                    .setAction("com.example.furniture.MainActivity")
                    })
                    .build();
            List<ShortcutInfo> getShortcut=shortcutManager.getDynamicShortcuts();
            if(getShortcut.size()>0){
                ShortcutInfo shortcutOne=getShortcut.get(0);
                getShortcut.add(shortcutOne);
                shortcutManager.setDynamicShortcuts(Arrays.asList(shortcutOne,shortcutTwo));
            }else{
                shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcutTwo));
            }

        }
    }


    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("BitmapImage", image);
        outState.putString("Result",s);
        Log.i("Sebok",outState.toString());
    }
    @Override
    protected void onResume() {
        super.onResume();

        if(imageView.getDrawable()==null){
            add.setVisibility(View.INVISIBLE);
        }else {
            add.setVisibility(View.VISIBLE);
        }
    }

    public void classifyImage(Bitmap image){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
             classes = new String[]{"Man","Woman", "Cabinet", "Furniture", "Wardrobe", "Wall","Face","Plane"};
            s=classes[maxPos];
            result.setText(classes[maxPos]);

            res.add(classes[maxPos]);
            save(res);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == 3){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }if(requestCode == 1){
                Uri dat = data.getData();
                 image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageView.setImageBitmap(image);
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(ArrayList<String> s)
    {
        SharedPreferences sharedPreferences=getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(TEXT,s.toString());

        editor.apply();
    }

    public ArrayList<String> fetchAllPreference(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        Map<String, String> m = (Map<String, String>) sharedPreferences.getAll();
        ArrayList<String> list = new ArrayList<>(m.values());
        return list;
    }


}