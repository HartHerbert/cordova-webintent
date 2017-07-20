package com.borismus.webintent;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.apache.cordova.CordovaActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ClipData;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;

/**
 * WebIntent is a PhoneGap plugin that bridges Android intents and web
 * applications:
 *
 * 1. web apps can spawn intents that call native Android applications. 2.
 * (after setting up correct intent filters for PhoneGap applications), Android
 * intents can be handled by PhoneGap web applications.
 *
 * @author boris@borismus.com
 *
 */
public class WebIntent extends CordovaPlugin {

    private CallbackContext onNewIntentCallbackContext = null;

    //public boolean execute(String action, JSONArray args, String callbackId) {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {

            if (action.equals("startActivity")) {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                // Parse the arguments
                final CordovaResourceApi resourceApi = webView.getResourceApi();
                JSONObject obj = args.getJSONObject(0);
                String type = obj.has("type") ? obj.getString("type") : null;
                Uri uri = obj.has("url") ? resourceApi.remapUri(Uri.parse(obj.getString("url"))) : null;
                JSONObject extras = obj.has("extras") ? obj.getJSONObject("extras") : null;
                Map<String, String> extrasMap = new HashMap<String, String>();

                // Populate the extras if any exist
                if (extras != null) {
                    JSONArray extraNames = extras.names();
                    for (int i = 0; i < extraNames.length(); i++) {
                        String key = extraNames.getString(i);
                        String value = extras.getString(key);
                        extrasMap.put(key, value);
                    }
                }

                startActivity(obj.getString("action"), uri, type, extrasMap);
                //return new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                return true;

            } else if (action.equals("getAction")) {
                Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                //return new PluginResult(PluginResult.Status.OK, i.getAction());
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, i.getAction()));
                return true;

            } else if (action.equals("hasExtra")) {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }
                Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                String extraName = args.getString(0);
                //return new PluginResult(PluginResult.Status.OK, i.hasExtra(extraName));
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, i.hasExtra(extraName)));
                return true;

            } else if (action.equals("getExtra")) {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }
                Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                String extraName = args.getString(0);
                if (i.hasExtra(extraName)) {
                    String r = i.getStringExtra(extraName);
                    if (null == r) {
                        r = ((Uri) i.getParcelableExtra(extraName)).toString();
                    }

                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, r));
                    return true;
                } else {
                    //return new PluginResult(PluginResult.Status.ERROR);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                    return false;
                }
            } else if (action.equals("getExtraArray")) {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }
                Intent intent = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                String extraName = args.getString(0);
                if (intent.hasExtra(extraName)) {
                    ArrayList<String> arrayListExtra = intent.getStringArrayListExtra(extraName);
                    JSONArray jsonResult = new JSONArray();

                    if (arrayListExtra != null && arrayListExtra.isEmpty() == false) {
                        for (int index = 0; index < arrayListExtra.size(); index++){
                            jsonResult.put(arrayListExtra.get(index));
                        }
                    }

                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, jsonResult));
                    return true;
                } else {
                    //return new PluginResult(PluginResult.Status.ERROR);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                    return false;
                }
            } else if (action.equals("getUri")) {
                if (args.length() != 0) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                String uri = i.getDataString();
                //return new PluginResult(PluginResult.Status.OK, uri);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, uri));
                return true;
            } else if (action.equals("getType")) {
                if (args.length() != 0) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                String type = i.getType();
                //return new PluginResult(PluginResult.Status.OK, type);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, type));
                return true;
            } else if (action.equals("onNewIntent")) {
                //save reference to the callback; will be called on "new intent" events
                this.onNewIntentCallbackContext = callbackContext;

                if (args.length() != 0) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true); //re-use the callback on intent events
                callbackContext.sendPluginResult(result);
                return true;
                //return result;
            } else if (action.equals("sendBroadcast")) {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                // Parse the arguments
                JSONObject obj = args.getJSONObject(0);

                JSONObject extras = obj.has("extras") ? obj.getJSONObject("extras") : null;
                Map<String, String> extrasMap = new HashMap<String, String>();

                // Populate the extras if any exist
                if (extras != null) {
                    JSONArray extraNames = extras.names();
                    for (int i = 0; i < extraNames.length(); i++) {
                        String key = extraNames.getString(i);
                        String value = extras.getString(key);
                        extrasMap.put(key, value);
                    }
                }

                sendBroadcast(obj.getString("action"), extrasMap);
                //return new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                return true;
            } else if (action.equals("removeExtra")) {
                if (args.length() != 1) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }
                Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                String extraName = args.getString(0);
                i.removeExtra(extraName);
                ClipData c = new ClipData(null,new String[]{},new ClipData.Item(""));
                i.setClipData(c);
                this.cordova.getActivity().setIntent(i);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            }

            //return new PluginResult(PluginResult.Status.INVALID_ACTION);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            String errorMessage=e.getMessage();
            //return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION,errorMessage));
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final Intent intent = this.cordova.getActivity().getIntent();
        Log.d("WEBINTENT","onStart");
        String url = "";

        if (this.onNewIntentCallbackContext != null) {
            if(intent.getAction().equals(Intent.ACTION_SEND)){
                Boolean hasStream = false;
                try{
                    hasStream = (intent.getExtras().get(Intent.EXTRA_STREAM)==null)?false:true;//native galery
                }catch (NullPointerException e){
                    //catched exception
                }
                Boolean hasClipData = (intent.getClipData().getItemCount() < 1)?false:true;//whatsapp and other applications
                ClipData clipData = intent.getClipData();
                for(int i = 0; i < clipData.getItemCount();i++){
                    ClipData.Item item =  clipData.getItemAt(i);

                    Uri uri;
                    try{
                        uri = item.getUri();
                        if(uri != null){
                            //image has been passed by Intent in clipboard
                            //url = returnUrl(this.cordova.getActivity().getApplicationContext(),uri);
                            PluginResult result = new PluginResult(PluginResult.Status.OK, uri.toString());
                            result.setKeepCallback(true);
                            this.onNewIntentCallbackContext.sendPluginResult(result);
                            return;
                        }
                    }catch(Exception e){
                        //catched
                        uri = null;
                        PluginResult error = new PluginResult(PluginResult.Status.ERROR, "Error getting filepath");
                        this.onNewIntentCallbackContext.sendPluginResult(error);
                    }
                }
                Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
                //intent.getExtras().get(Intent.)
                if(uri != null){
                    try{
                        url = returnUrl(this.cordova.getActivity().getApplicationContext(),uri);
                    }catch (Exception e){
                        PluginResult error = new PluginResult(PluginResult.Status.ERROR, "Error getting filepath");
                        this.onNewIntentCallbackContext.sendPluginResult(error);
                        return;
                    }
                    PluginResult result = new PluginResult(PluginResult.Status.OK, url);
                    result.setKeepCallback(true);
                    this.onNewIntentCallbackContext.sendPluginResult(result);
                }else{
                    Log.d("WEBINTENT","STREAM NULL");
                }
            }
        }
    }

    private String returnUrl(Context context, Uri uri){
        String url = getFileNameByUri(context,uri);
        if(url == null){
            return "";
        }
        return "file://"+url;
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            /*String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);*/
            String[] projection = {MediaStore.MediaColumns.DATA};
            CursorLoader cursorLoader = new CursorLoader(context, contentUri, projection, null, null, null);
            cursor = cursorLoader.loadInBackground();
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String getFileNameByUri(Context context, Uri uri)
    {
        String fileName="unknown";//default fileName
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content")==0)
        {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst())
            {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                fileName = filePathUri.getLastPathSegment().toString();
            }
        }
        else if (uri.getScheme().compareTo("file")==0)
        {
            fileName = filePathUri.getLastPathSegment().toString();
        }
        else
        {
            fileName = fileName+"_"+filePathUri.getLastPathSegment();
        }
        return fileName;
    }

    @Override
    public void onNewIntent(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        super.onNewIntent(intent);
        //just set the intent as the newest one
        //and get it onStart()
        this.cordova.getActivity().setIntent(intent);
    }

    void startActivity(String action, Uri uri, String type, Map<String, String> extras) {
        Intent i = (uri != null ? new Intent(action, uri) : new Intent(action));

        if (type != null && uri != null) {
            i.setDataAndType(uri, type); //Fix the crash problem with android 2.3.6
        } else {
            if (type != null) {
                i.setType(type);
            }
        }

        for (String key : extras.keySet()) {
            String value = extras.get(key);
            // If type is text html, the extra text must sent as HTML
            if (key.equals(Intent.EXTRA_TEXT) && type.equals("text/html")) {
                i.putExtra(key, Html.fromHtml(value));
            } else if (key.equals(Intent.EXTRA_STREAM)) {
                // allowes sharing of images as attachments.
                // value in this case should be a URI of a file
                final CordovaResourceApi resourceApi = webView.getResourceApi();
                i.putExtra(key, resourceApi.remapUri(Uri.parse(value)));
            } else if (key.equals(Intent.EXTRA_EMAIL)) {
                // allows to add the email address of the receiver
                i.putExtra(Intent.EXTRA_EMAIL, new String[] { value });
            } else {
                i.putExtra(key, value);
                Log.d("KEY",""+key);
                Log.d("Value",""+value);
            }
        }
        ((CordovaActivity)this.cordova.getActivity()).startActivity(i);
    }

    void sendBroadcast(String action, Map<String, String> extras) {
        Intent intent = new Intent();
        intent.setAction(action);
        for (String key : extras.keySet()) {
            String value = extras.get(key);
            intent.putExtra(key, value);
        }

        ((CordovaActivity)this.cordova.getActivity()).sendBroadcast(intent);
    }
}
