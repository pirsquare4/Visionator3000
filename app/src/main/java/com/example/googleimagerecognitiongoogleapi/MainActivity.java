package com.example.googleimagerecognitiongoogleapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.ResponseHandler;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    static final int IMAGE_CAPTURE_CODE = 1001;
    Button cameraBtn;
    ImageView imageView;
    Uri cameraImageUri;
    RelativeLayout parentLayout;
    MyCustomView testView;
    MyCustomView[] my_cs;
    ColorPicker cp;
    ColorPicker cp2;
    int mycolor;
    boolean run;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Os.setenv("GOOGLE_APPLICATION_CREDENTIALS","/sdcard/image-labeler-268908-160d120b9e41.json", true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        run = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        cameraBtn = findViewById(R.id.takePictureButton);
        imageView = findViewById(R.id.previewImage);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });
        parentLayout = findViewById(R.id.SubLayout);
        testView = findViewById(R.id.testView);
        /* Show color picker dialog */
        if (!run) {
            cp = new ColorPicker(MainActivity.this, 0, 0, 0);
            Button selectColor = findViewById(R.id.myColorButton);
            MyCustomView.setColor(Color.BLACK);
            selectColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cp.show();

                    /* On Click listener for the dialog, when the user select the color */

                    Button okColor = cp.findViewById(R.id.okColorButton);
                    okColor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /* You can get single channel (value 0-255) */

                            /* Or the android RGB Color (see the android Color class reference) */
                            MyCustomView.setColor(cp.getColor());
                            cp.dismiss();
                        }
                    });
                }
            });

            cp2 = new ColorPicker(MainActivity.this, 255, 255, 255);
            Button selectColor2 = findViewById(R.id.myColorButton2);
            MyCustomView.setTextColor(Color.WHITE);
            selectColor2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cp2.show();

                    /* On Click listener for the dialog, when the user select the color */

                    Button okColor = cp2.findViewById(R.id.okColorButton);
                    okColor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /* You can get single channel (value 0-255) */

                            /* Or the android RGB Color (see the android Color class reference) */
                            MyCustomView.setTextColor(cp2.getColor());
                            cp2.dismiss();
                        }
                    });
                }
            });
        }
        run = true;


    }

    public void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(cameraImageUri);
            if (my_cs != null) {
                for (int i = 0; i < my_cs.length; i++) {
                    ((ViewGroup)my_cs[i].getParent()).removeView(my_cs[i]);
                }
            }
            my_cs = null;
        }

        Bitmap picture = null;
        if (resultCode == RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            try {
            picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraImageUri);
        } catch(Exception e){
            e.printStackTrace();
        }
        //Bitmap myBitmap = BitmapFactory.decodeFile(cameraImageUri.getPath());

        //imageView.setImageBitmap(myBitmap);

        // Set the bitmap as the source of the ImageView

        // More code goes here

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.JPEG, 90, byteStream);
        String base64Data = Base64.encodeToString(byteStream.toByteArray(),
                Base64.URL_SAFE);
        String requestURL =
                "https://vision.googleapis.com/v1/images:annotate?key=" +
                        getResources().getString(R.string.mykey);
        // Create an array containing
        // the LABEL_DETECTION feature
        JSONObject postData = new JSONObject();
        try {
            JSONArray features = new JSONArray();
            JSONObject feature = new JSONObject();
            feature.put("type", "OBJECT_LOCALIZATION");
            features.put(feature);

            // Create an object containing
            // the Base64-encoded image data
            JSONObject imageContent = new JSONObject();
            imageContent.put("content", base64Data);

            // Put the array and object into a single request
            // and then put the request into an array of requests
            JSONArray requests = new JSONArray();
            JSONObject request = new JSONObject();
            request.put("image", imageContent);
            request.put("features", features);
            requests.put(request);
            postData.put("requests", requests);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert the JSON into a
        // string
        String body = postData.toString();
        Fuel.INSTANCE.post(requestURL, null)
                .header("content-length", body.length())
                .appendHeader("content-type", "application/json")
                .body(body.getBytes(), Charset.defaultCharset())
                .responseString(new ResponseHandler<String>() {

                    @Override
                    public void success(@NotNull Request request,
                                        @NotNull com.github.kittinunf.fuel.core.Response response,
                                        String s) {
                        try {
                            JSONArray labels = new JSONObject(s)
                                    .getJSONArray("responses")
                                    .getJSONObject(0)
                                    .getJSONArray("localizedObjectAnnotations");

                            String results = "";

                            // Loop through the array and extract the
                            // description key for each item
                            Double[][] rect_vals = new Double[labels.length()][4];
                            String[] names = new String[labels.length()];
                            for (int i = 0; i < labels.length(); i++) {
                                Double left = (Double) labels.getJSONObject(i).getJSONObject("boundingPoly").
                                                getJSONArray("normalizedVertices").getJSONObject(0).get("x");
                                Double bottom = (Double)
                                        labels.getJSONObject(i).getJSONObject("boundingPoly").
                                                getJSONArray("normalizedVertices").getJSONObject(0).get("y");

                                Double right = (Double) labels.getJSONObject(i).getJSONObject("boundingPoly").
                                                getJSONArray("normalizedVertices").getJSONObject(1).get("x");
                                Double top = (Double) labels.getJSONObject(i).getJSONObject("boundingPoly").
                                                getJSONArray("normalizedVertices").getJSONObject(2).get("y");
                                rect_vals[i][0] = left;
                                rect_vals[i][1] = top;
                                rect_vals[i][2] = right;
                                rect_vals[i][3] = bottom;
                                names[i] =  labels.getJSONObject(i).getString("name");

                            }

                            // Display the annotations inside the TextView
                            my_cs = new MyCustomView[labels.length()];
                            for (int i = 0; i < labels.length(); i++) {
                                MyCustomView c = new MyCustomView(parentLayout.getContext());
                                c.set_left(rect_vals[i][0]);
                                c.set_top(rect_vals[i][1]);
                                c.set_right(rect_vals[i][2]);
                                c.set_bottom(rect_vals[i][3]);
                                c.set_name(names[i]);
                                parentLayout.addView(c);
                                my_cs[i] = c;
                                c.update();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(@NotNull Request request,
                                        @NotNull com.github.kittinunf.fuel.core.Response response,
                                        @NotNull FuelError fuelError) {

                    }
                });
            }
    }

}
