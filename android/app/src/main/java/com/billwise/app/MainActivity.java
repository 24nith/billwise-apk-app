package com.billwise.app;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
	@Override
	public void onCreate(android.os.Bundle savedInstanceState) {
		registerPlugin(NativePrintPlugin.class);
		registerPlugin(NativeSharePlugin.class);
		super.onCreate(savedInstanceState);
	}
}
