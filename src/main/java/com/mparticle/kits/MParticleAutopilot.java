package com.mparticle.kits;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.mparticle.MParticle;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.util.UAStringUtil;

/**
 * Autopilot for UrbanAirshipKit integration.
 */
public class MParticleAutopilot extends Autopilot {

    private static final String PREFERENCE_NAME = "com.mparticle.kits.urbanairship";

    //persistence keys
    private static final String APP_KEY = "applicationKey";
    private static final String APP_SECRET = "applicationSecret";
    private static final String GCM_SENDER = "gcmSender";
    private static final String NOTIFICATION_ICON_NAME = "notificationIconName";
    private static final String NOTIFICATION_COLOR = "notificationColor";

    // Perform first run defaults
    private static final String FIRST_RUN_KEY = "first_run";

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        AirshipConfigOptions.Builder optionsBuilder = new AirshipConfigOptions.Builder()
                .setGcmSender(preferences.getString(GCM_SENDER, null))
                .setNotificationIcon(preferences.getInt(NOTIFICATION_ICON_NAME, 0))
                .setNotificationAccentColor(preferences.getInt(NOTIFICATION_COLOR, 0));


        if (MParticle.getInstance().getEnvironment() == MParticle.Environment.Development) {
            optionsBuilder.setDevelopmentAppKey(preferences.getString(APP_KEY, null))
                    .setDevelopmentAppSecret(preferences.getString(APP_SECRET, null))
                    .setInProduction(false);
        } else {
            optionsBuilder.setProductionAppKey(preferences.getString(APP_KEY, null))
                    .setProductionAppSecret(preferences.getString(APP_SECRET, null))
                    .setInProduction(true);
        }

        return optionsBuilder.build();
    }

    @Override
    public void onAirshipReady(UAirship airship) {
        SharedPreferences preferences = UAirship.getApplicationContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (preferences.getBoolean(FIRST_RUN_KEY, true)) {
            preferences.edit().putBoolean(FIRST_RUN_KEY, false).apply();
            airship.getPushManager().setUserNotificationsEnabled(true);
        }
    }

    @Override
    public boolean allowEarlyTakeOff(@NonNull Context context) {
        AirshipConfigOptions config = createAirshipConfigOptions(context);
        return config != null && !UAStringUtil.isEmpty(config.getAppKey()) && !UAStringUtil.isEmpty(config.getAppSecret());
    }

    /**
     * Caches the MParticle config for Urban Airship.
     *
     * @param context The application context.
     * @param configuration UrbanAirshipKit configuration.
     */
    static void updateConfig(Context context, UrbanAirshipConfiguration configuration) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(APP_KEY, configuration.getApplicationKey())
                .putString(APP_SECRET, configuration.getApplicationSecret())
                .putString(GCM_SENDER, MParticle.getInstance().getConfigManager().getPushSenderId());


        // Convert accent color hex string to an int
        String accentColor = configuration.getNotificationColor();
        if (!UAStringUtil.isEmpty(accentColor)) {
            try {
                editor.putInt(NOTIFICATION_COLOR, Color.parseColor(accentColor));
            } catch (IllegalArgumentException e) {
                Logger.warn("Unable to parse notification accent color: " + accentColor, e);
            }
        }

        // Convert notification name to a drawable resource ID
        String notificationIconName = configuration.getNotificationIconName();
        if (!UAStringUtil.isEmpty(notificationIconName)) {
            int id = context.getResources().getIdentifier(notificationIconName, "drawable", context.getPackageName());
            if (id != 0) {
                editor.putInt(NOTIFICATION_ICON_NAME, id);
            } else {
                Logger.error("Unable to find notification icon with name: " + notificationIconName);
            }
        }

        editor.apply();
    }
}