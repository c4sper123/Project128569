package komunikacia.login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;

public class OffersWindow extends Activity{

    private ArrayList<Offer> offersData = new ArrayList<>();
    private static String TAG = OffersWindow.class.getSimpleName();
    private ProgressDialog pDialog;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_window);
        getActionBar().setDisplayShowHomeEnabled(false);


        userId = getIntent().getStringExtra("userId");

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Prosím čakajte...");
        pDialog.setCancelable(false);

        loadOffers(BackendlessSettings.urlJsonObj);

    }

    private void showAllOffers(){
        ArrayAdapter<Offer> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.offersListView);
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<Offer>{
        public MyListAdapter(){
            super(OffersWindow.this,R.layout.offer_view,offersData);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null)
                itemView = getLayoutInflater().inflate(R.layout.offer_view, parent, false);
            final Offer currentOffer = offersData.get(position);

            TextView nameText = (TextView) itemView.findViewById(R.id.offerNameTxt);
            nameText.setText(currentOffer.getName());
            TextView priceText = (TextView) itemView.findViewById(R.id.offerPriceTxt);
            priceText.setText("" + currentOffer.getPrice().toString() + " €");
            ImageView iconView = (ImageView) itemView.findViewById(R.id.imageView);
            iconView.setScaleType(ImageView.ScaleType.FIT_XY);
            UrlImageViewHelper.setUrlDrawable(iconView, currentOffer.getImageUrl());
            TextView localityText = (TextView) itemView.findViewById(R.id.offerPlaceTxt);
            localityText.setText("   Miesto: " + currentOffer.getLocality());

            TextView detailsText = (TextView) itemView.findViewById(R.id.offerDetailsTxt);
            detailsText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(OffersWindow.this,OfferDetailsWindow.class);
                    intent.putExtra("objectId",currentOffer.getObjectId());
                    startActivity(intent);
                }
            });


            return itemView;
        }
    }

    private void loadOffers(String URL) {

        showpDialog();

        final JsonObjectIdRequest jsonObjReq = new JsonObjectIdRequest(Method.GET,URL,null ,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try{
                    JSONArray data = response.getJSONArray("data");

                    for(int i=0;i<data.length();i++){
                        JSONObject offerObject = (JSONObject) data.get(i);
                        offersData.add(new Offer(offerObject.getString("name"), offerObject.getString("locality"), offerObject.getString("details"),
                                Integer.parseInt(offerObject.getString("price")), Integer.parseInt(offerObject.getString("type")),
                                offerObject.getString("startDate"), offerObject.getString("endDate"), Integer.parseInt(offerObject.getString("maxPeople")),
                                offerObject.getString("imageUrl"), offerObject.getString("objectId")));

                    }
                if(!response.getString("nextPage").equals("null")){
                    loadOffers(response.getString("nextPage"));
                }
                else{
                    showAllOffers();
                    hidepDialog();
                }


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
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                hidepDialog();
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
