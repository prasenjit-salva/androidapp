package com.digitstory.testapplication.Services;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.digitstory.testapplication.Utils.Config;

public class MyAccessibilityServiceForBootup extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(Config.TAG, "onAccessibilityEvent");

    }

    @Override
    public void onInterrupt() {
        Log.d(Config.TAG, "onInterrupt");
    }
}