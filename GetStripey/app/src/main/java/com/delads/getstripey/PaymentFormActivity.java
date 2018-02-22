package com.delads.getstripey;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delads.getstripey.com.delads.getstripey.util.PostObject;
import com.delads.getstripey.com.delads.getstripey.util.URLFetcher;
import com.squareup.okhttp.ResponseBody;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class PaymentFormActivity extends AppCompatActivity{

    private String mPrice;
    private String mProduct_id;
    private ProgressBar mProgressBar = null;
    private TextView mErrorMessage = null;
    private LinearLayout mTopLayout = null;
    private Card mCard = null;
    private CompositeSubscription mCompositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_payment_form);

        Intent intent = getIntent();
        //Bitmap image = (Bitmap)intent.getParcelableExtra("image");

        mPrice = (String)intent.getStringExtra("price");
        mProduct_id = (String)intent.getStringExtra("product_id");

        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mErrorMessage = (TextView)findViewById(R.id.error);
        mTopLayout = (LinearLayout)findViewById(R.id.top_layout);

        mCompositeSubscription = new CompositeSubscription();





        Button getTokenButton = (Button)findViewById(R.id.get_token_button);
        getTokenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                /*
                Context context = v.getContext();
                //Let's collect all the info from the form
                EditText numberView = (EditText)findViewById(R.id.number);
                String cc_number = numberView.getText().toString();

                Spinner expMonthView = (Spinner)findViewById(R.id.expMonth);
                Integer cc_expMonth = getInteger(expMonthView);

                Spinner expYearView = (Spinner)findViewById(R.id.expYear);
                Integer cc_expYear = getInteger(expYearView);

                EditText cvcView = (EditText)findViewById(R.id.cvc);
                String cc_cvc = cvcView.getText().toString();

                        Card card = new Card(
                        cc_number,
                        cc_expMonth,
                        cc_expYear,
                        cc_cvc);
                */
                //The new Stripe Android drop-in UI equivalent
                CardInputWidget mCardInputWidget = (CardInputWidget)findViewById(R.id.card_input_widget);
                mCard = mCardInputWidget.getCard();

                //boolean validation = card.validateCard();
                if (mCard != null && mCard.validateCard()) {

                    Stripe stripe = new Stripe(v.getContext(), "pk_test_dY29OuyjBRDtRx5sUkmUVCbp");
                    stripe.createToken(
                            mCard,
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    //Make a call to getstripey.com

                                    String host = "http://www.getstripey.com/payandroid";
                                    Map<String, Object> params = new LinkedHashMap<>();
                                    params.put("product_id", mProduct_id);
                                    params.put("stripeToken", token.getId());

                                    PostObject post = new PostObject();
                                    post.setHost(host);
                                    post.setParams(params);

                                    mProgressBar.setVisibility(View.VISIBLE);
                                    mTopLayout.setBackgroundColor(Color.LTGRAY);
                                    mErrorMessage.setVisibility(View.GONE);

                                    Log.println(Log.DEBUG,"PaymentFormActivity", "Let's do this. About to charge the card");

                                    new CreateChargeTask(PaymentFormActivity.this).execute(post);

                                }
                                public void onError(Exception error) {
                                    //Handle The errors
                                    Log.println(Log.ERROR,"PaymentFormActivity", "Failure - Token=" + error.toString());
                                }
                            });
                } else if (mCard != null && !mCard.validateNumber()) {
                    Log.println(Log.ERROR,"PaymentFormActivity", "The card number that you entered is invalid");
                    mErrorMessage.setText("The card number that you entered is invalid");
                    mErrorMessage.setVisibility(View.VISIBLE);

                } else if (mCard != null && !mCard.validateExpiryDate()) {
                    Log.println(Log.ERROR,"PaymentFormActivity", "The expiration date that you entered is invalid");
                    mErrorMessage.setText("The expiration date that you entered is invalid");
                    mErrorMessage.setVisibility(View.VISIBLE);

                } else if (mCard != null && !mCard.validateCVC()) {
                    Log.println(Log.ERROR,"PaymentFormActivity", "The CVC code that you entered is invalid");
                    mErrorMessage.setText("The CVC code that you entered is invalid");
                    mErrorMessage.setVisibility(View.VISIBLE);

                } else {
                    Log.println(Log.ERROR,"PaymentFormActivity", "The card details that you entered are invalid");
                    mErrorMessage.setText("The card details that you entered are invalid");
                    mErrorMessage.setVisibility(View.VISIBLE);

                }


            }
        });





    }



    private Integer getInteger(Spinner spinner) {
        try {
            return Integer.parseInt(spinner.getSelectedItem().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private class CreateChargeTask extends AsyncTask {

        Context mContext;

        public CreateChargeTask(Context context){
            mContext = context;
        }

        public Object doInBackground(Object... urls) {

            String response =  URLFetcher.postString((PostObject) urls[0]);
           return response;
        }

        public void onPostExecute(Object result) {

            mProgressBar.setVisibility(View.GONE);
            mTopLayout.setBackgroundColor(Color.WHITE);

            if(result != null) {

                String result_string = (String) result.toString();
                Log.println(Log.DEBUG,"PaymentFormActivity", "Response from transaction=" + result_string);

                if (result_string.compareTo("\"success\"") == 0) {
                    Toast.makeText(getApplicationContext(), "Thanks for your purchase!", Toast.LENGTH_LONG).show();

                    Log.println(Log.DEBUG,"PaymentFormActivity", "About to launch new intent");
                    Intent intent = new Intent(this.mContext, MainActivity.class);
                    mContext.startActivity(intent);

                    
                }
                else { //we've got a problem
                    Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
                    Log.println(Log.ERROR,"PaymentFormActivity", "Shite!!");
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Unexpected problem. Please try again", Toast.LENGTH_LONG).show();
                Log.println(Log.ERROR,"PaymentFormActivity", "Shite!!");
            }

        }
    }

    /*

    @Override
    public void createEphemeralKey(String apiVersion, final EphemeralKeyUpdateListener keyUpdateListener) {
        Map<String, String> apiParamMap = new HashMap<>();
        apiParamMap.put("api_version", apiVersion);

        mCompositeSubscription.add(
                mStripeService.createEphemeralKey(apiParamMap)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ResponseBody>() {
                            @Override
                            public void call(ResponseBody response) {
                                try {
                                    String rawKey = response.string();
                                    keyUpdateListener.onKeyUpdate(rawKey);
                                   // mProgressListener.onStringResponse(rawKey);
                                } catch (IOException iox) {

                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                               // mProgressListener.onStringResponse(throwable.getMessage());
                            }
                        }));
    }
    */


}
