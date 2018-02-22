package com.delads.getstripey.com.delads.getstripey.util;


        import java.io.BufferedInputStream;
        import java.io.BufferedReader;
        import java.io.ByteArrayOutputStream;
        import java.io.DataOutputStream;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.Reader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.net.URLEncoder;
        import java.nio.charset.Charset;
        import java.util.LinkedHashMap;
        import java.util.Map;

        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.util.Log;

public class URLFetcher {

    public static Bitmap getBitmap(String url){

        try {

        /* Open a new URL and get the InputStream to load data from it. */

            URL img = new URL(url);
            return BitmapFactory.decodeStream(img.openStream());

        }catch(MalformedURLException e){
            Log.e("GetStripey", e.getMessage());
            return null;
        }
        catch(IOException e){
            Log.e("GetStripey", e.getMessage());
            return null;

        }

        catch (Exception e){
            Log.e("GetStripey", e.getMessage());
            return null;
        }
    }


    public static String postString(PostObject post){

        try {

            String host = post.getHost();
            Map<String, Object> params = post.getParams();


            URL url = new URL(host);
            StringBuilder postData = new StringBuilder();


            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            String response = sb.toString();

            return response;
        }
        catch (Exception e){
            Log.println(Log.ERROR,"GetStripey", "Error - " + e.getMessage());
            return null;
        }
    }


    public static String getString(String url){

        try {

            Log.println(Log.INFO,"URLFetcher", "Opening - " + url);

            URL http_url = new URL(url);
            http_url.openConnection();
            BufferedInputStream bis = new BufferedInputStream(http_url.openStream());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result = bis.read();

            while(result != -1) {
                byte b = (byte)result;
                buf.write(b);
                result = bis.read();
            }


            Log.println(Log.INFO,"URLFetcher", "Returning String " + buf.toString());
            return buf.toString();

        }catch(MalformedURLException e){
            Log.println(Log.ERROR,"GetStripey", e.getMessage());
            return null;
        }
        catch(IOException e){
            Log.println(Log.ERROR,"GetStripey", e.getMessage());
            return null;

        }

        catch (Exception e){
            Log.println(Log.ERROR,"GetStripey", "Error - " + e.getMessage());
            return null;
        }
    }




}