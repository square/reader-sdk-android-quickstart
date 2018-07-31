package com.example.readersdk;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.sdk.reader.ReaderSdk;
import com.squareup.sdk.reader.authorization.AuthorizationManager;
import com.squareup.sdk.reader.authorization.AuthorizationState;
import com.squareup.sdk.reader.authorization.DeauthorizeErrorCode;
import com.squareup.sdk.reader.authorization.Location;
import com.squareup.sdk.reader.checkout.AdditionalPaymentType;
import com.squareup.sdk.reader.checkout.CheckoutErrorCode;
import com.squareup.sdk.reader.checkout.CheckoutManager;
import com.squareup.sdk.reader.checkout.CheckoutParameters;
import com.squareup.sdk.reader.checkout.CheckoutResult;
import com.squareup.sdk.reader.checkout.CurrencyCode;
import com.squareup.sdk.reader.checkout.Money;
import com.squareup.sdk.reader.core.CallbackReference;
import com.squareup.sdk.reader.core.Result;
import com.squareup.sdk.reader.core.ResultError;
import com.squareup.sdk.reader.hardware.ReaderManager;
import com.squareup.sdk.reader.hardware.ReaderSettingsErrorCode;

public class CheckoutActivity extends AppCompatActivity {

  private static final String TAG = CheckoutActivity.class.getSimpleName();

  private CallbackReference deauthorizeCallbackRef;
  private CallbackReference checkoutCallbackRef;
  private CallbackReference readerSettingsCallbackRef;

  private boolean waitingForActivityStart = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_activity);

    View settingsButton = findViewById(R.id.settings_button);
    settingsButton.setOnClickListener(view -> showSettingsBottomSheet());

    CheckoutManager checkoutManager = ReaderSdk.checkoutManager();
    checkoutCallbackRef = checkoutManager.addCheckoutActivityCallback(this::onCheckoutResult);

    ReaderManager readerManager = ReaderSdk.readerManager();
    readerSettingsCallbackRef =
        readerManager.addReaderSettingsActivityCallback(this::onReaderSettingsResult);

    AuthorizationManager authorizationManager = ReaderSdk.authorizationManager();
    deauthorizeCallbackRef = authorizationManager.addDeauthorizeCallback(this::onDeauthorizeResult);

    if (!authorizationManager.getAuthorizationState().isAuthorized()) {
      goToAuthorizeActivity();
    } else {
      // 100 is the amount of money in the smallest denomination of the specified currency.
      // For example, when the currency is USD, the amount here is 100 cents, i.e. 1 US Dollar.
      Money checkoutAmount = new Money(100, CurrencyCode.current());

      TextView startCheckoutButton = findViewById(R.id.start_checkout_button);
      startCheckoutButton.setOnClickListener(view -> startCheckout(checkoutAmount));
      startCheckoutButton.setText(getString(R.string.start_checkout, checkoutAmount.format()));
    }
  }

  private void showSettingsBottomSheet() {
    BottomSheetDialog dialog = new BottomSheetDialog(this);
    View sheetView = LayoutInflater.from(this).inflate(R.layout.settings_sheet, null);

    AuthorizationManager authorizationManager = ReaderSdk.authorizationManager();
    AuthorizationState authorizationState = authorizationManager.getAuthorizationState();
    Location location = authorizationState.getAuthorizedLocation();
    String locationText = getString(R.string.location_view_format, location.getName());
    TextView locationView = sheetView.findViewById(R.id.location_view);
    locationView.setText(locationText);

    sheetView.findViewById(R.id.reader_settings_button)
        .setOnClickListener(v -> {
          dialog.dismiss();
          startReaderSettings();
        });

    sheetView.findViewById(R.id.deauthorize_button)
        .setOnClickListener(v -> {
          dialog.dismiss();
          deauthorize();
        });

    dialog.setContentView(sheetView);

    BottomSheetBehavior behavior = BottomSheetBehavior.from((View) sheetView.getParent());
    dialog.setOnShowListener(dialogInterface -> behavior.setPeekHeight(sheetView.getHeight()));

    dialog.show();
  }

  private void goToAuthorizeActivity() {
    if (waitingForActivityStart) {
      return;
    }
    waitingForActivityStart = true;
    Intent intent = new Intent(this, StartAuthorizeActivity.class);
    startActivity(intent);
    finish();
  }

  private void startCheckout(Money checkoutAmount) {
    if (waitingForActivityStart) {
      return;
    }
    waitingForActivityStart = true;
    CheckoutManager checkoutManager = ReaderSdk.checkoutManager();
    CheckoutParameters.Builder params = CheckoutParameters.newBuilder(checkoutAmount);
    params.additionalPaymentTypes(AdditionalPaymentType.CASH);
    params.note("Hello 💳 💰 World!");
    checkoutManager.startCheckoutActivity(this, params.build());
  }

  private void onCheckoutResult(Result<CheckoutResult, ResultError<CheckoutErrorCode>> result) {
    if (result.isSuccess()) {
      CheckoutResult checkoutResult = result.getSuccessValue();
      String totalAmount = checkoutResult.getTotalMoney().format();
      showDialog(getString(R.string.checkout_success_dialog_title, totalAmount),
          getString(R.string.checkout_success_dialog_message));
      Log.d(TAG, "\n" + checkoutResult.toString() + "\n");
    } else {
      ResultError<CheckoutErrorCode> error = result.getError();

      switch (error.getCode()) {
        case SDK_NOT_AUTHORIZED:
          goToAuthorizeActivity();
          break;
        case CANCELED:
          Toast.makeText(this, R.string.checkout_canceled_toast, Toast.LENGTH_SHORT).show();
          break;
        case USAGE_ERROR:
          showErrorDialog(error);
          break;
      }
    }
  }

  private void startReaderSettings() {
    if (waitingForActivityStart) {
      return;
    }
    waitingForActivityStart = true;
    ReaderManager readerManager = ReaderSdk.readerManager();
    readerManager.startReaderSettingsActivity(this);
  }

  private void onReaderSettingsResult(Result<Void, ResultError<ReaderSettingsErrorCode>> result) {
    if (result.isError()) {
      ResultError<ReaderSettingsErrorCode> error = result.getError();
      switch (error.getCode()) {
        case SDK_NOT_AUTHORIZED:
          goToAuthorizeActivity();
          break;
        case USAGE_ERROR:
          showErrorDialog(error);
          break;
      }
    }
  }

  private void deauthorize() {
    AuthorizationManager authorizationManager = ReaderSdk.authorizationManager();
    if (authorizationManager.getAuthorizationState().canDeauthorize()) {
      authorizationManager.deauthorize();
    } else {
      showDialog(getString(R.string.cannot_deauthorize_dialog_title),
          getString(R.string.cannot_deauthorize_dialog_message));
    }
  }

  private void onDeauthorizeResult(Result<Void, ResultError<DeauthorizeErrorCode>> result) {
    if (result.isSuccess()) {
      goToAuthorizeActivity();
    } else {
      showErrorDialog(result.getError());
    }
  }

  private void showErrorDialog(ResultError<?> error) {
    String dialogMessage = error.getMessage();
    if (BuildConfig.DEBUG) {
      dialogMessage += "\n\nDebug Message: " + error.getDebugMessage();
      Log.d(TAG, error.getCode() + ": " + error.getDebugCode() + ", " + error.getDebugMessage());
    }
    showDialog(getString(R.string.error_dialog_title), dialogMessage);
  }

  private void showDialog(CharSequence title, CharSequence message) {
    new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(R.string.ok_button, null)
        .show();
  }

  @Override protected void onResume() {
    super.onResume();
    waitingForActivityStart = false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    readerSettingsCallbackRef.clear();
    checkoutCallbackRef.clear();
    deauthorizeCallbackRef.clear();
  }
}
