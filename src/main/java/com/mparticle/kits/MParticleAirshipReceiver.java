package com.mparticle.kits;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mparticle.MParticle;
import com.urbanairship.AirshipReceiver;

public class MParticleAirshipReceiver extends AirshipReceiver {

    @Override
    protected void onChannelCreated(@NonNull Context context, @NonNull String channelId) {
        super.onChannelCreated(context, channelId);

        Object channelIdListener = MParticle.getInstance().getKitInstance(MParticle.ServiceProviders.URBAN_AIRSHIP);
        if (channelIdListener != null) {
            ((UrbanAirshipKit.ChannelIdListener)channelIdListener).channelIdUpdated();
        }
    }
}
