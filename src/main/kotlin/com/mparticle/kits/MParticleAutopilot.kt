package com.mparticle.kits

import android.R
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import com.mparticle.MParticle
import com.mparticle.internal.Logger
import com.mparticle.kits.UrbanAirshipKit.ChannelIdListener
import com.urbanairship.AirshipConfigOptions
import com.urbanairship.Autopilot
import com.urbanairship.UAirship
import com.urbanairship.util.UAStringUtil

/**
 * Autopilot for UrbanAirshipKit integration.
 */
class MParticleAutopilot : Autopilot() {
    override fun createAirshipConfigOptions(context: Context): AirshipConfigOptions {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val optionsBuilder = AirshipConfigOptions.Builder()
            .setNotificationIcon(preferences.getInt(NOTIFICATION_ICON_NAME, 0))
            .setNotificationAccentColor(preferences.getInt(NOTIFICATION_COLOR, 0))
            .setCustomPushProvider(MParticlePushProvider.instance)
            .setIsPromptForPermissionOnUserNotificationsEnabled(false)
        if (MParticle.getInstance()?.environment == MParticle.Environment.Development) {
            optionsBuilder.setDevelopmentAppKey(preferences.getString(APP_KEY, null))
                .setDevelopmentAppSecret(preferences.getString(APP_SECRET, null))
                .setInProduction(false)
        } else {
            optionsBuilder.setProductionAppKey(preferences.getString(APP_KEY, null))
                .setProductionAppSecret(preferences.getString(APP_SECRET, null))
                .setInProduction(true)
        }
        if ("EU".equals(preferences.getString(DOMAIN, null), true)) {
            optionsBuilder.setSite(AirshipConfigOptions.SITE_EU)
        }
        val customDomain = preferences.getString(CUSTOM_DOMAIN_PROXY_URL, null)
        if (!UAStringUtil.isEmpty(customDomain)) {
            optionsBuilder.setInitialConfigUrl(customDomain).setUrlAllowList(arrayOf(customDomain))
        }
        return optionsBuilder.build()
    }

    override fun onAirshipReady(airship: UAirship) {
        val preferences = UAirship.getApplicationContext()
            .getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        if (preferences.getBoolean(FIRST_RUN_KEY, true)) {
            preferences.edit().putBoolean(FIRST_RUN_KEY, false).apply()
            airship.pushManager.userNotificationsEnabled = true
        }

        // Restore the last registration token
        val token = airship.pushManager.pushToken
        MParticlePushProvider.instance.setRegistrationToken(token)
        airship.channel.addChannelListener { callChannelIdListener() }

        callChannelIdListener()
    }

    fun callChannelIdListener() {
        val channelIdListener =
            MParticle.getInstance()?.getKitInstance(MParticle.ServiceProviders.URBAN_AIRSHIP)
        if (channelIdListener != null) {
            (channelIdListener as ChannelIdListener).channelIdUpdated()
        }
    }

    override fun allowEarlyTakeOff(context: Context): Boolean = false


    companion object {
        private const val PREFERENCE_NAME = "com.mparticle.kits.urbanairship"

        //persistence keys
        private const val APP_KEY = "applicationKey"
        private const val APP_SECRET = "applicationSecret"
        private const val DOMAIN = "domain"
        private const val CUSTOM_DOMAIN_PROXY_URL = "customDomainProxyUrl"
        private const val NOTIFICATION_ICON_NAME = "notificationIconName"
        private const val NOTIFICATION_COLOR = "notificationColor"

        // Perform first run defaults
        private const val FIRST_RUN_KEY = "first_run"

        /**
         * Caches the MParticle config for Urban Airship.
         *
         * @param context       The application context.
         * @param configuration UrbanAirshipKit configuration.
         */
        fun updateConfig(context: Context, configuration: UrbanAirshipConfiguration) {
            val editor = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putString(APP_KEY, configuration.applicationKey)
                .putString(APP_SECRET, configuration.applicationSecret)
                .putString(DOMAIN, configuration.domain)
                .putString(CUSTOM_DOMAIN_PROXY_URL, configuration.customDomainProxyUrl)

            // Convert accent color hex string to an int
            val accentColor = configuration.notificationColor
            if (!UAStringUtil.isEmpty(accentColor)) {
                try {
                    val colorValue = when {
                        accentColor.equals("System default") -> {
                            val typedValue = TypedValue()
                            val theme = context.theme
                            theme.resolveAttribute(R.attr.windowBackground, typedValue, true)
                            typedValue.data
                        }

                        else -> Color.parseColor(accentColor)
                    }
                    editor.putInt(NOTIFICATION_COLOR, colorValue)
                } catch (e: IllegalArgumentException) {
                    Logger.warning(e, "Unable to parse notification accent color: $accentColor")
                }
            }

            // Convert notification name to a drawable resource ID
            val notificationIconName = configuration.notificationIconName
            if (!UAStringUtil.isEmpty(notificationIconName)) {
                val id = context.resources.getIdentifier(
                    notificationIconName, "drawable", context.packageName
                )
                if (id != 0) {
                    editor.putInt(NOTIFICATION_ICON_NAME, id)
                } else {
                    Logger.error("Unable to find notification icon with name: $notificationIconName")
                }
            }
            editor.apply()
        }
    }
}
