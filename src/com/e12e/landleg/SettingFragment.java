package com.e12e.landleg;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingFragment extends PreferenceFragment {
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        // 从xml文件加载选项  
        addPreferencesFromResource(R.xml.preferences);
    }  
}
