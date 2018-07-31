package com.example.readersdk;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import com.example.readersdk.util.AnimationListenerAdapter;
import java.util.ArrayList;
import java.util.List;

public class JewelImageView extends AppCompatImageView {

  private static final int WAIT_BEFORE_ANIMATE_UP_DELAY_MS = 300;

  public JewelImageView(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
    Activity activity = (Activity) context;
    activity.getWindow().setBackgroundDrawableResource(R.drawable.window_background);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
            animateJewelUpFromCenter();
          }
        });
  }

  private void animateJewelUpFromCenter() {
    ViewGroup parent = (ViewGroup) getParent();

    List<View> hiddenViews = new ArrayList<>();
    for (int index = 0; index < parent.getChildCount(); index++) {
      View childView = parent.getChildAt(index);
      if (childView != this && childView.getVisibility() == VISIBLE) {
        childView.setAlpha(0);
        childView.setEnabled(false);
        hiddenViews.add(childView);
      }
    }

    int screenCenterY = getScreenCenterY(parent);
    float currentY = getY();
    setY(screenCenterY);
    postDelayed(() -> animate().y(currentY).setListener(new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        for (View hiddenView : hiddenViews) {
          hiddenView.setEnabled(true);
          hiddenView.animate().alpha(1);
        }
      }
    }), WAIT_BEFORE_ANIMATE_UP_DELAY_MS);
  }

  private int getScreenCenterY(ViewGroup parent) {
    Rect rectangle = new Rect();
    Activity activity = (Activity) getContext();
    Window window = activity.getWindow();
    window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
    int contentViewTop =
        window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
    int titleBarHeight = contentViewTop - rectangle.top;
    return parent.getHeight() / 2 + titleBarHeight / 2;
  }
}
