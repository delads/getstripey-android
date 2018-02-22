package com.delads.getstripey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.EnumSet;

import cz.msebera.android.httpclient.Header;
import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.provider.ProviderMode;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.parameters.TransactionParameters;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;

public class DisplayProductActivity extends AppCompatActivity {

    private String m_price;
    private String m_product_id;
    private String m_name;

    private String mBraintreeClientToken;
    private static int BRAINTREE_REQUEST_CODE = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_display_product);


        Intent intent = getIntent();
        //Bitmap image = (Bitmap)intent.getParcelableExtra("image");

        m_product_id = (String)intent.getStringExtra("id");
        m_name = (String)intent.getStringExtra("name");
        String summary = (String)intent.getStringExtra("summary");
        m_price = (String)intent.getStringExtra("price");

        double price_double = 0;

        //Let's get rid of the trailing zero as it's ugly
        try{
            price_double = Double.parseDouble(m_price);
            DecimalFormat df = new DecimalFormat("#.##");
            m_price = df.format(price_double);

        }catch(Exception e){}





        TextView nameView = (TextView)(findViewById(R.id.textViewName));
        nameView.setText(m_name);
        nameView.setTransitionName("name_transition_"+ m_product_id);

        TextView summaryView = (TextView)(findViewById(R.id.textViewSummary));
        summaryView.setText(summary);

        TextView priceView = (TextView)(findViewById(R.id.textViewPrice));
        priceView.setText("$" + m_price);
        priceView.setTransitionName("price_transition_" + m_product_id);

        Button stripeButtonView = (Button)(findViewById(R.id.stripe_buy_button));
        //stripeButtonView.setText("Pay $" + m_price);

        //Don't even need this -assigning onClickListener in xml
       // Button braintreeButtonView = (Button)(findViewById(R.id.buy_button));


        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(this.getApplicationContext().openFileInput(m_product_id + "_image"));//here context can be anything like getActivity() for fragment, this or MainActivity.this
        }catch(Exception e){}



        ImageButton view = (ImageButton)(findViewById(R.id.imageButton));
        view.setImageBitmap(bitmap);
        view.setTransitionName("image_transition_" + m_product_id);


        Log.println(Log.INFO,"DisplayProductActivity", "v.getTransitionName = " + view.getTransitionName());



        stripeButtonView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click


                Intent intent = new Intent(v.getContext(), PaymentFormActivity.class);
                intent.putExtra("price",m_price);
                intent.putExtra("product_id",m_product_id);
                startActivity(intent);

            }
        });






        //Let's kick off for Braintree
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://www.getstripey.com/getbraintreetoken", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                mBraintreeClientToken = new String(response);

                Log.println(Log.INFO,"DisplayProductActivity", "BraintreeToken - " + mBraintreeClientToken);

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.println(Log.ERROR,"DisplayProductActivity", "BraintreeToken - " + mBraintreeClientToken);

            }
        });


    }


    public void onBraintreeSubmit(View v) {

        if(mBraintreeClientToken != null) {
            PaymentRequest paymentRequest = new PaymentRequest()
                    .clientToken(mBraintreeClientToken);
            startActivityForResult(paymentRequest.getIntent(this), BRAINTREE_REQUEST_CODE);
        }
        else{
            //Exit gracefully
            Toast.makeText(getApplicationContext(),"Failed to connect with Braintree. Please try again later", Toast.LENGTH_LONG).show();
            Log.println(Log.INFO,"DisplayProductActivity", "BraintreeToken - " + mBraintreeClientToken);
        }

    }


    public void onPayworksSubmit(View v) {
        MposUi ui = MposUi.initialize(this, ProviderMode.TEST,
                "0bc13071-4f27-4bb4-9d14-d2abec19704a", "yhEHXBXFhTmdPbmQMEmJaX75os9LMaOC");

        ui.getConfiguration().setSummaryFeatures(EnumSet.of(
                // Add this line, if you do want to offer printed receipts
                // MposUiConfiguration.SummaryFeature.PRINT_RECEIPT,
                MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL)
        );

        /*
        // Start with a mocked card reader:
        AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MOCK)
                .mocked()
                .build();
        ui.getConfiguration().setTerminalParameters(accessoryParameters);
        */

        // Add this line if you would like to collect the customer signature on the receipt (as opposed to the digital signature)
        // ui.getConfiguration().setSignatureCapture(MposUiConfiguration.SignatureCapture.ON_RECEIPT);


    //When using the Bluetooth Miura Shuttle / M007 / M010, use the following parameters:
    AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MIURA_MPI)
                                                                     .bluetooth()
                                                                     .build();
    ui.getConfiguration().setTerminalParameters(accessoryParameters);






    /* When using the WiFi Miura M010, use the following parameters:
    AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MIURA_MPI)
                                                                     .tcp("192.168.254.123", 38521)
                                                                     .build();
    ui.getConfiguration().setTerminalParameters(accessoryParameters);
    */



    /* Add this section, if you do want to offer printed receipts
    AccessoryParameters printerAccessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.SEWOO)
                                                            .bluetooth()
                                                            .build();
    ui.getConfiguration().setPrinterParameters(printerAccessoryParameters);
    */


        TransactionParameters transactionParameters = new TransactionParameters.Builder()
                .charge(new BigDecimal(m_price), io.mpos.transactions.Currency.EUR)
                .subject(m_name)
                .customIdentifier("ReferenceTransaction for - " + m_name )
                .build();


        Intent intent = ui.createTransactionIntent(transactionParameters);
        startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);
    }

    /*
    @Override
    public void onBackPressed(){
        Log.println(Log.INFO,"DisplayProductActivity", "Back button Pressed");
       // this.finishAfterTransition();
        this.finish();
    }
    */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.println(Log.INFO,"DisplayProductActivity", "onActivityResult called: requestCode : " + requestCode);
        Log.println(Log.INFO,"DisplayProductActivity", "Payworks request code: " + MposUi.REQUEST_CODE_PAYMENT);

        Log.println(Log.INFO,"DisplayProductActivity", "onActivityResult called: resultCode : " + resultCode);
        Log.println(Log.INFO,"DisplayProductActivity", "Payworks result code approved : " + MposUi.RESULT_CODE_APPROVED);


        if (requestCode == MposUi.REQUEST_CODE_PAYMENT) {
            if (resultCode == MposUi.RESULT_CODE_APPROVED) {
                // Transaction was approved
                Toast.makeText(this, "Transaction approved", Toast.LENGTH_LONG).show();
            } else {
                // Card was declined, or transaction was aborted, or failed
                // (e.g. no internet or accessory not found)
                Toast.makeText(this, "Transaction was declined, aborted, or failed",
                        Toast.LENGTH_LONG).show();
            }
            // Grab the processed transaction in case you need it
            // (e.g. the transaction identifier for a refund).
            // Keep in mind that the returned transaction might be null
            // (e.g. if it could not be registered).
            Transaction transaction = MposUi.getInitializedInstance().getTransaction();


        }

        else if (requestCode == BRAINTREE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(
                        BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE
                );

                //String amount = "10"; //let's hardcode for now

                String nonce = paymentMethodNonce.getNonce();
                // Send the nonce to your server.
                AsyncHttpClient client = new AsyncHttpClient();

                //Let's set the POST params coming from the app. These are expected by the pay_braintree controller on GetStripey
                RequestParams params = new RequestParams();
                params.put("amount",m_price);
                params.put("payment_method_nonce",nonce);


                client.post("http://www.getstripey.com/paybraintreeandroid", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"

                        //For now let's toast
                        Toast.makeText(getApplicationContext(),new String(response), Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        //For now let's toast
                        Toast.makeText(getApplicationContext(),new String (errorResponse), Toast.LENGTH_LONG).show();
                        Log.println(Log.INFO,"DisplayProductActivity", "onFailure called - " + new String (errorResponse));
                    }
                });
            }
        }
    }



}
