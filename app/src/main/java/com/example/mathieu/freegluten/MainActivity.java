package com.example.mathieu.freegluten;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private Button buttonScan;
    String ResultCodeScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = (Button) this.findViewById(R.id.buttonScan);
        final Activity activity = this;
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scannez le code barre");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
    }

    public void onClickButton(View v) {
        Button button = findViewById(R.id.button);
        TextView textCodeScan = findViewById(R.id.textCodeScan);
        String codeScan = textCodeScan.getText().toString();
        String url = "https://world.openfoodfacts.org/api/v0/product/" + codeScan;
        Uri.parse(url);

        Ion.with(this)
                .load(url)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        TextView textResult = findViewById(R.id.textResult);

                        try {
                            JsonArray glutenJson = result.get("product").getAsJsonObject().get("allergens_tags").getAsJsonArray();
                            //Si glutenJson.size() == 0, il n'y a pas d'allergène présent
                            if (glutenJson.size() != 0) {
                                //Recherche de "gluten" dans "allergens_tags"
                                for ( int i = 0; i < glutenJson.size() ; i++)
                                {
                                    String glutenJsonLine = glutenJson.get(i).getAsString();
                                    int glutenReturn = glutenJsonLine.indexOf("gluten");
                                    if (glutenReturn == -1  || glutenJsonLine == "") {
                                        textResult.setText("Gluten free !");
                                    }
                                    else {
                                        textResult.setText("Présence de gluten");
                                    }
                                }
                            } else {
                                textResult.setText("Gluten free !");
                            }

                        }catch (Exception ee) {
                            textResult.setText("Produit introuvable");
                        }


                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView textCodeScan = findViewById(R.id.textCodeScan);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            //Ceci est important, autrement on ne passera pas le résultat au fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
        ResultCodeScan = result.getContents();
        textCodeScan.setText(ResultCodeScan);
    }
}
