
package com.odemolliens.rn.jwt;

import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ODJsonWebTokenModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public ODJsonWebTokenModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ODJsonWebToken";
    }


    @ReactMethod
    public void encodeDic(String algorithmByName, ReadableMap payload, String secret, Promise promise) {
        JSONObject json = null;
        try {
            json = this.convertMapToJson(payload);
            this.encode(algorithmByName, json.toString(), secret, promise);
        } catch (JSONException ex) {
            Log.i("EX", ex.toString());
            promise.reject("EX", ex.toString());
        }
    }

    @ReactMethod
    public void encodeArray(String algorithmByName, ReadableArray payload, String secret, Promise promise) {
        JSONArray json = null;
        try {
            json = this.convertArrayToJson(payload);
            this.encode(algorithmByName, json.toString(), secret, promise);
        } catch (JSONException ex) {
            Log.i("EX", ex.toString());
            promise.reject("EX", ex.toString());
        }

    }

    @ReactMethod
    public void encodeJSONString(String algorithmByName, String payload, String secret, Promise promise) {
        this.encode(algorithmByName, payload, secret, promise);
    }

    private void encode(String algorithmByName, String finalPayload, String secret, Promise promise) {
        String header = "{\"typ\":\"JWT\",\"alg\":\"" + algorithmByName + "\"}";
        String headerEncode = this.encode(header);
        String payloadEncode = this.encode(finalPayload);
        String signature = HmacSHA256(headerEncode + "." + payloadEncode, secret);
        String token = headerEncode + "." + payloadEncode + "." + signature;
        Log.i("TOKEN", token);
        promise.resolve(token);
    }

    /**
     * Encodes a String with Base64Url and no padding
     *
     * @param input String to be encoded
     * @return Encoded result from input
     */
    private static String encode(String input) {
        String result = null;
        try {
            byte[] encodeBytes = input.getBytes("UTF-8");
            result = Base64.encodeToString(encodeBytes, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return result;
    }

    private String HmacSHA256(String message, String secret) {
        try {

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String hash = Base64.encodeToString(sha256_HMAC.doFinal(message.getBytes()), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
            return hash;
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeyException e) {
        }
        return "";
    }

    private JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    NumberFormat formatter = new DecimalFormat("#0.000");
                    object.put(key, Double.valueOf(formatter.format(readableMap.getDouble(key))));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, convertMapToJson(readableMap.getMap(key)));
                    break;
                case Array:
                    object.put(key, convertArrayToJson(readableMap.getArray(key)));
                    break;
            }
        }
        return object;
    }

    private JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(convertMapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(convertArrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }
}