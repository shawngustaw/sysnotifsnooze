package com.shawngustaw.sysnotifsnooze;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.shawngustaw.sysnotifsnooze.R;

public class MainActivity extends AppCompatActivity {

    public static String SKU_SMALL_TIP_2 = "small_tip_2";
    public static String SKU_LARGE_TIP_5 = "large_tip_5";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{
        private static final String KEY_NOTIFICATION_PERMISSION = "notification_permission";
        private static final String KEY_SETTINGS_HIDE_ICON = "hide_icon";

        private static final String TAG = "SettingsFragment";

        private boolean isSwitchSet;

        Context CONTEXT;

        private Preference notification_permission;
        private SwitchPreference settings_hide_icon;
        private boolean isNotificationAccessPermissionGranted;

        private SharedPreferences sharedPreferences;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            CONTEXT = getActivity().getApplicationContext();
            addPreferencesFromResource(R.xml.settings);

            sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);

            notification_permission = findPreference(KEY_NOTIFICATION_PERMISSION);
            settings_hide_icon = (SwitchPreference)findPreference(KEY_SETTINGS_HIDE_ICON);


            notification_permission.setOnPreferenceClickListener(this);
            settings_hide_icon.setOnPreferenceClickListener(this);


            settings_hide_icon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    Log.d(TAG, "in onPreferenceChange");

                    if (!settings_hide_icon.isChecked()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(R.string.string_hide_icon_alert_dialog_msg);
                        builder.setTitle(R.string.string_hide_icon_alert_dialog_title);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.string_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "set switch to TRUE");

                                PackageManager pkg = CONTEXT.getPackageManager();
                                pkg.setComponentEnabledSetting(new ComponentName(CONTEXT, MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                        PackageManager.DONT_KILL_APP);

                                settings_hide_icon.setChecked(true);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(CONTEXT.getString(R.string.string_sharedPreferences_isIconHidden), true);
                                editor.apply();

                                Log.d(TAG, "switch is: " + String.valueOf(settings_hide_icon.isChecked()));
                            }
                        });

                        builder.setNegativeButton(CONTEXT.getString(R.string.string_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                settings_hide_icon.setChecked(false);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(CONTEXT.getString(R.string.string_sharedPreferences_isIconHidden), true);
                                editor.apply();
                            }
                        });

                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                Log.d(TAG, "set switch to FALSE (1)");

                                PackageManager pkg = CONTEXT.getPackageManager();
                                pkg.setComponentEnabledSetting(new ComponentName(CONTEXT, MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                        PackageManager.DONT_KILL_APP);

                                settings_hide_icon.setChecked(false);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(CONTEXT.getString(R.string.string_sharedPreferences_isIconHidden), false);
                                editor.apply();
                                Log.d(TAG, "switch is: " + String.valueOf(settings_hide_icon.isChecked()));
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }else{
                        Log.d(TAG, "set switch to FALSE (2)");

                        PackageManager pkg = CONTEXT.getPackageManager();
                        pkg.setComponentEnabledSetting(new ComponentName(CONTEXT, MainActivity.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);

                        settings_hide_icon.setChecked(false);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(CONTEXT.getString(R.string.string_sharedPreferences_isIconHidden), false);
                        editor.apply();
                        Log.d(TAG, "switch is: " + String.valueOf(settings_hide_icon.isChecked()));
                    }


                    return true;
                }
            });

        }

        @Override
        public void onResume() {
            super.onResume();

            if (!Utils.hasAccessGranted(CONTEXT)) {
                Log.d(TAG, "No Notification Access");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(CONTEXT.getString(R.string.string_sharedPref_granted), false);
                editor.apply();

                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                builder.setMessage(R.string.permission_request_msg);
                builder.setTitle(R.string.permission_request_title);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }else{
                Log.d(TAG, "Has Notification Access");

                if (!sharedPreferences.getBoolean(CONTEXT.getString(R.string.string_sharedPref_granted), false)) {

                    Log.d(TAG, "sending broadcast");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(CONTEXT.getString(R.string.string_sharedPref_granted), true);
                    editor.apply();

                    Intent intent = new Intent(getString(R.string.string_filter_intent));
                    intent.putExtra("command", "hide");
                    CONTEXT.sendBroadcast(intent);
                }
            }
        }



        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()){
                case KEY_NOTIFICATION_PERMISSION:

                    if (isNotificationAccessPermissionGranted){
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                        builder.setMessage(R.string.permission_request_msg);
                        builder.setTitle(R.string.permission_request_title);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }


                    break;


            }
            return false;
        }

        @Override
        public void onStart() {
            super.onStart();

            isNotificationAccessPermissionGranted = Utils.hasAccessGranted(CONTEXT);
            notification_permission.setSummary(isNotificationAccessPermissionGranted ? getString(R.string.granted) : getString(R.string.not_granted));

            isSwitchSet = sharedPreferences.getBoolean(CONTEXT.getString(R.string.string_sharedPreferences_isIconHidden), false);
            settings_hide_icon.setChecked(isSwitchSet);

        }

    }

}