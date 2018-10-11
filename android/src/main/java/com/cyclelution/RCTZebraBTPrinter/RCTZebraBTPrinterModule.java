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

    public void printZplData(String printDevice, String zplData, Promise response) {
      printerConnection = null;
      printerConnection = new BluetoothConnection(printDevice);

      try {
        printerConnection.open();
        ZebraPrinter printer = null;
        if(printerConnection.isConnected()) {
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
            printerConnection.write(zplData.getBytes());
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
                if (D) Log.d(TAG, "printLabel printed with " + friendlyName);
            }

        } catch (ConnectionException e) {
          response.resolve(false);
            // response.resolve("Printer label com failed to open 2nd stage");

        } finally {
          response.resolve(true);
          // response.resolve("Printer successful");
        }

      } catch ( ConnectionException e) {
        response.resolve(false);
        // response.resolve("Printer device failed to open");
      }

    }

    @Override
    public String getName() {
        return "RCTZebraBTPrinter";
    }

}
