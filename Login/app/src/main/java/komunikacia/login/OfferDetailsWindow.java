package komunikacia.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OfferDetailsWindow extends Activity {
    private String objectId;
    private static String TAG = OffersWindow.class.getSimpleName();
    private ProgressDialog pDialog;
    private ImageView imageView1;
    private AlertDialog.Builder myAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_details_window);
        getActionBar().setDisplayShowHomeEnabled(false);
        myAlert = new AlertDialog.Builder(this);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Prosím čakajte...");
        pDialog.setCancelable(false);

        Intent intent = getIntent();
        intent.getIntExtra("objectId", 0);
        objectId = intent.getStringExtra("objectId");

        imageView1 = (ImageView) findViewById(R.id.imageView4);
        imageView1.setVisibility(View.VISIBLE);

        loadOffer(BackendlessSettings.urlJsonObjId, objectId);

    }

    private void loadOffer(String URL, String objectId) {

        showpDialog();
        URL += objectId;
        Log.d("Request URL", URL); //len na vypisanie do logu, ako vyzera moja request URL

        final JsonObjectIdRequest jsonObjReq = new JsonObjectIdRequest(Request.Method.GET,URL,null ,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try{
                    TextView textView = (TextView) findViewById(R.id.offerNameTxt);
                    textView.setText(response.getString("name"));

                    Button btn = (Button) findViewById(R.id.buyBtn);
                    btn.setText("KÚPIŤ ZA " + response.getString("price") + "€");

                    JSONArray category =  response.getJSONArray("categories");
                    JSONObject categories = category.getJSONObject(0);

                    String type = null;
                    switch(response.getInt("type")){
                        case 1:{ type = "Chata"; break;}
                        case 2:{ type = "Hotel"; break;}
                        case 3:{ type = "Penzión"; break;}
                        case 4:{ type = "Apartmán"; break;}
                        default: break;
                    };
                    textView = (TextView) findViewById(R.id.textView3);
                    textView.setText(response.getString("details") +
                                    "\n\nLokalita: " + response.getString("locality")+
                                    "\n\nTyp: " + type +
                                    "\n\nMaximálny počet ľudí: " + response.getString("maxPeople")+
                                    "\n\nKategória: " + categories.getString("mainCategory")+
                                    "\n\nPodkategória: " + categories.getString("category")
                    );

                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    UrlImageViewHelper.setUrlDrawable(imageView, response.getString("imageUrl"));

                    textView = (TextView) findViewById(R.id.offerDateTxt);
                    Date start = new Date();
                    Date end = new Date();

                    start.setTime(Long.parseLong(response.getString("startDate")));  //tuuu
                    end.setTime(Long.parseLong(response.getString("endDate")));        //tuuu2

                    java.text.DateFormat formatStart, formatEnd;
                    formatStart = new SimpleDateFormat("dd.MM.");
                    formatEnd = new SimpleDateFormat("dd.MM.yyyy");

                    String staDate = formatStart.format(start);
                    String endDate = formatEnd.format(end);

                    textView.setText(staDate + " - " + endDate); //vyzera to takto: dd.MM. - dd.MM.yyyy  (napr. 3.4. - 20.4.2016)
                    imageView1.setVisibility(View.INVISIBLE);
                    hidepDialog();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidepDialog();

                myAlert.setMessage("Nepodarilo sa nadviazať spojenie so serverom!").create();
                myAlert.setTitle("Error");
                myAlert.setIcon(android.R.drawable.ic_dialog_alert);
                myAlert.setNegativeButton("Zrušiť", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish(); //návrat do predchadzajucej aktivity - OffersWindow
                    }
                });
                myAlert.show();
            }

        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


}
