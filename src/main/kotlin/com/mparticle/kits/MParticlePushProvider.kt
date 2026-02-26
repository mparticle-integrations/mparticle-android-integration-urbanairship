package com.mparticle.kits

import android.content.Context
import com.urbanairship.UAirship
import com.urbanairship.push.PushProvider

/**
 * Used to register for push in the Urban Airship SDK.
 */
internal class MParticlePushProvider private constructor() : PushProvider {
    private var token: String? = null

    override fun getPlatform(): Int = UAirship.ANDROID_PLATFORM

    override fun getDeliveryType(): String = PushProvider.FCM_DELIVERY_TYPE

    override fun getRegistrationToken(context: Context): String? = token

    override fun isAvailable(context: Context): Boolean = true

    override fun isSupported(context: Context): Boolean = true

    fun setRegistrationToken(token: String?) {
        this.token = token
    }

    companion object {
        val instance = MParticlePushProvider()
    }
}
