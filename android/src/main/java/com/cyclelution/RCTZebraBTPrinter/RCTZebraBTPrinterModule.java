package com.cyclelution.RCTZebraBTPrinter;

import java.lang.reflect.Method;
import java.util.Set;
import javax.annotation.Nullable;

import android.app.Activity;

import android.util.Log;
import android.util.Base64;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Callback;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;

import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import static com.cyclelution.RCTZebraBTPrinter.RCTZebraBTPrinterPackage.TAG;

@SuppressWarnings("unused")
public class RCTZebraBTPrinterModule extends ReactContextBaseJavaModule {

    // Debugging
    private static final boolean D = true;

    private final ReactApplicationContext reactContext;

    private Connection printerConnection;
    private ZebraPrinter printer;

    private String delimiter = "";

    public RCTZebraBTPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);

        if (D) Log.d(TAG, "Bluetooth module started");

        this.reactContext = reactContext;
    }

     @ReactMethod
    /**
     * Entry point
     */
    public void printLabel(String userPrinterSerial, String userPrintCount, String userText1, String userText2, String userText3, Promise promise) {

        if (D) Log.d(TAG, "printLabel triggered on Android " + userPrinterSerial + " " + userText1);
        //promise.resolve(true);

        if (D) Log.d(TAG, "printLabel connecting to printer");

        printerConnection = null;

        printerConnection = new BluetoothConnection(userPrinterSerial);

        try {

            printerConnection.open();

            if (D) Log.d(TAG, "printLabel com open");

            ZebraPrinter printer = null;

            if (printerConnection.isConnected()) {

                try {

                    printer = ZebraPrinterFactory.getInstance(printerConnection);

                    PrinterLanguage pl = printer.getPrinterControlLanguage();

                } catch (ConnectionException e) {

                    if (D) Log.d(TAG, "printLabel com failed to open 2nd stage");
                    printer = null;

                } catch (ZebraPrinterLanguageUnknownException e) {

                    if (D) Log.d(TAG, "printLabel print language get failed");
                    printer = null;

                }

            }

            try {

                if (D) Log.d(TAG, "printLabel trying to send print job");

                String cpclConfigLabel = "! 0 200 200 304 "+ userPrintCount + "\r\n" + "TEXT 0 3 10 10 CYC LABEL START\r\n" + "TEXT 0 3 10 40 "+userText1+" " + userText2 + " " + userText3 + "\r\n" + "BARCODE 128 1 1 40 10 80 "+userText1+"\r\n" + "TEXT 0 3 10 150 CYC LABEL END\r\n" + "FORM\r\n" + "PRINT\r\n";

                byte[]  configLabel = cpclConfigLabel.getBytes();

                printerConnection.write(configLabel);

                if (printerConnection instanceof BluetoothConnection) {

                    String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();

                    if (D) Log.d(TAG, "printLabel printed with " + friendlyName);

                }

            } catch (ConnectionException e) {

                if (D) Log.d(TAG, "printLabel com failed to open 2nd stage");
                promise.resolve(false);

            } finally {

                //disconnect();
                if (D) Log.d(TAG, "printLabel done");
                promise.resolve(true);

            }

        } catch (ConnectionException e) {

            if (D) Log.d(TAG, "printLabel com failed to open");
            promise.resolve(false);

        }



    }

    @Override
    public String getName() {
        return "RCTZebraBTPrinter";
    }

}
