package com.example.readersdk;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StartAuthorizeActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.start_authorize_activity);

    View qrCodeButton = findViewById(R.id.qr_code_button);
    if (deviceIsEmulator()) {
      qrCodeButton.setEnabled(false);
      qrCodeButton.setOnClickListener(this::qrCodeScanningNotSupported);
    } else {
      qrCodeButton.setOnClickListener(view -> startQrCodeScanning());
    }

    View manualCodeEntryButton = findViewById(R.id.manual_code_entry_button);
    manualCodeEntryButton.setOnClickListener(view -> startManualCodeEntry());
  }

  public static boolean deviceIsEmulator() {
    return Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || "google_sdk".equals(Build.PRODUCT);
  }

  private void qrCodeScanningNotSupported(View view) {
    Snackbar.make(view, R.string.not_supported_emulator, Snackbar.LENGTH_SHORT)
        .show();
  }

  private void startQrCodeScanning() {
    startActivity(new Intent(this, ScanQRCodeActivity.class));
    finish();
  }

  private void startManualCodeEntry() {
    startActivity(new Intent(this, ManualCodeEntryActivity.class));
    finish();
  }
}
