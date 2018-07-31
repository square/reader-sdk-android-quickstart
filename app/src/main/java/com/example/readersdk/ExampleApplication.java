package com.example.readersdk;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import com.squareup.sdk.reader.ReaderSdk;

public class ExampleApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    ReaderSdk.initialize(this);
  }

  @Override protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }
}
