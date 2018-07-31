package com.example.readersdk;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewStub;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Uses https://github.com/dlazaro66/QRCodeReaderView for QR Code scanning.
 */
public class ScanQRCodeActivity extends AppCompatActivity
    implements ActivityCompat.OnRequestPermissionsResultCallback, OnQRCodeReadListener {

  private static final int CAMERA_REQUEST_CODE = 0;

  private QRCodeReaderView qrCodeReaderView;
  private ViewStub qrCodeStub;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.scan_qr_code_activity);

    qrCodeStub = findViewById(R.id.qr_code_stub);

    findViewById(R.id.cancel_button).setOnClickListener(view -> finish());

    if (ActivityCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED) {
      initQRCodeReaderView();
    } else {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
        Snackbar.make(qrCodeStub, R.string.permission_request, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok_button, view -> requestCameraPermission())
            .show();
      } else {
        requestCameraPermission();
      }
    }
  }

  private void initQRCodeReaderView() {
    qrCodeStub.inflate();
    qrCodeReaderView = findViewById(R.id.qr_code_reader);
    qrCodeReaderView.setAutofocusInterval(2000L);
    qrCodeReaderView.setOnQRCodeReadListener(this);
    qrCodeReaderView.setBackCamera();
    qrCodeReaderView.setQRDecodingEnabled(true);
    qrCodeReaderView.startCamera();
  }

  @Override public void onBackPressed() {
    startActivity(new Intent(this, StartAuthorizeActivity.class));
    finish();
  }

  @Override public void onQRCodeRead(String authorizationCode, PointF[] points) {
    qrCodeReaderView.setOnQRCodeReadListener(null);
    AuthorizingActivity.start(this, authorizationCode);
    finish();
  }

  private void requestCameraPermission() {
    String[] permissions = { CAMERA };
    ActivityCompat.requestPermissions(this, permissions, CAMERA_REQUEST_CODE);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode != CAMERA_REQUEST_CODE) {
      return;
    }
    if (grantResults.length == 1 && grantResults[0] == PERMISSION_GRANTED) {
      initQRCodeReaderView();
    } else {
      finish();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if (qrCodeReaderView != null) {
      qrCodeReaderView.startCamera();
    }
  }

  @Override protected void onPause() {
    super.onPause();
    if (qrCodeReaderView != null) {
      qrCodeReaderView.stopCamera();
    }
  }
}
