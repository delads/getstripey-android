package com.delads.getstripey;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;

public class BraintreePayPageActivity extends AppCompatActivity {

    private String mBraintreeClientToken;
    private static int REQUEST_CODE = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braintree_payment);


        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://www.getstripey.com/getbraintreetoken", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                mBraintreeClientToken = response.toString();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });

    }

    public void onBraintreeSubmit(View v) {
        PaymentRequest paymentRequest = new PaymentRequest()
                .clientToken(mBraintreeClientToken);
        startActivityForResult(paymentRequest.getIntent(this), REQUEST_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(
                        BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE
                );

                String amount = "10"; //let's hardcode for now

                String nonce = paymentMethodNonce.getNonce();
                // Send the nonce to your server.
                AsyncHttpClient client = new AsyncHttpClient();

                //Let's set the POST params coming from the app. These are expected by the pay_braintree controller on GetStripey
                RequestParams params = new RequestParams();
                params.put("amount",amount);
                params.put("payment_method_nonce",nonce);


                client.post("http://www.getstripey.com/pay_braintree", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"

                        //For now let's toast
                        Toast.makeText(getApplicationContext(),"Success - " + response.toString(), Toast.LENGTH_LONG);
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        //For now let's toast
                        Toast.makeText(getApplicationContext(),"Failure - " + errorResponse.toString(), Toast.LENGTH_LONG);
                    }
                });
            }
        }
    }






}
