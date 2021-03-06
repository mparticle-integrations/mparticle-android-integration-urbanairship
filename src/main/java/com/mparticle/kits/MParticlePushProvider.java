package com.mparticle.kits;

import android.content.Context;
import androidx.annotation.NonNull;

import com.urbanairship.UAirship;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.PushProvider;

/**
 * Used to register for push in the Urban Airship SDK.
 */
class MParticlePushProvider implements PushProvider {

    private static MParticlePushProvider instance = new MParticlePushProvider();
    private String token;

    public static MParticlePushProvider getInstance() {
        return instance;
    }

    private MParticlePushProvider() {}

    @Override
    public int getPlatform() {
        return UAirship.ANDROID_PLATFORM;
    }

    @androidx.annotation.NonNull
    @Override
    public String getDeliveryType() {
        return PushProvider.FCM_DELIVERY_TYPE;
    }

    @Override
    public String getRegistrationToken(@NonNull Context context) {
        return token;
    }

    @Override
    public boolean isAvailable(@NonNull Context context) {
        return true;
    }

    @Override
    public boolean isSupported(@NonNull Context context) {
        return true;
    }

    void setRegistrationToken(String token) {
        this.token = token;
    }
}
