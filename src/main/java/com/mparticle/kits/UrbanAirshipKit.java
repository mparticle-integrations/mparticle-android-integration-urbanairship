package com.mparticle.kits;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.mparticle.MPEvent;
import com.mparticle.MParticle;
import com.mparticle.commerce.CommerceEvent;
import com.mparticle.commerce.Product;
import com.mparticle.internal.CommerceEventUtil;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.analytics.CustomEvent;
import com.urbanairship.analytics.InstallReceiver;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.PushService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Urban Airship mParticle kit
 */
public class UrbanAirshipKit extends KitIntegration implements KitIntegration.PushListener, KitIntegration.EventListener, KitIntegration.CommerceListener, KitIntegration.AttributeListener {

    private ChannelIdListener channelIdListener;

    public interface ChannelIdListener {
        void channelIdUpdated();
    }

    // Identities
    private static final String IDENTITY_EMAIL = "email";
    private static final String IDENTITY_FACEBOOK = "facebook_id";
    private static final String IDENTITY_TWITTER = "twitter_id";
    private static final String IDENTITY_GOOGLE = "google_id";
    private static final String IDENTITY_MICROSOFT = "microsoft_id";
    private static final String IDENTITY_YAHOO = "yahoo_id";
    private static final String IDENTITY_FACEBOOK_CUSTOM_AUDIENCE_ID = "facebook_custom_audience_id";
    private static final String IDENTITY_CUSTOMER_ID = "customer_id";

    public static final String CHANNEL_ID_INTEGRATION_KEY = "com.urbanairship.channel_id";


    @Override
    public String getName() {
        return "Urban Airship";
    }

    @Override
    public Object getInstance() {
        return channelIdListener;
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> map, final Context context) {
        channelIdListener = new ChannelIdListener(){

            @Override
            public void channelIdUpdated() {
                updateChannelIntegration();
            }
        };
        MParticleAutopilot.updateConfig(context, map);
        Autopilot.automaticTakeOff(context);
        updateChannelIntegration();
        return null;
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean optedOut) {
        UAirship.shared().getAnalytics().setEnabled(optedOut);

        ReportingMessage message = new ReportingMessage(this, ReportingMessage.MessageType.OPT_OUT, System.currentTimeMillis(), null);
        return Collections.singletonList(message);
    }

    @Override
    public void setInstallReferrer(Intent intent) {
        new InstallReceiver().onReceive(UAirship.getApplicationContext(), intent);
    }

    @Override
    public boolean willHandlePushMessage(Intent intent) {
        return intent.getStringExtra(PushMessage.EXTRA_SEND_ID) != null ||
                intent.getStringExtra(PushMessage.EXTRA_ALERT) != null;
    }

    @Override
    public void onPushMessageReceived(Context context, Intent intent) {
        Intent pushIntent = (new Intent(context, PushService.class))
                .setAction("com.urbanairship.push.ACTION_RECEIVE_GCM_MESSAGE")
                .putExtra("com.urbanairship.push.EXTRA_INTENT", intent);

        WakefulBroadcastReceiver.startWakefulService(context, pushIntent);
    }

    @Override
    public boolean onPushRegistration(String instanceId, String senderId) {
        // Assume mismatch sender Ids means we need to refresh the token
        if (!senderId.equals(UAirship.shared().getPushManager().getGcmToken())) {
            Intent intent = new Intent(UAirship.getApplicationContext(), PushService.class)
                    .setAction("com.urbanairship.push.ACTION_UPDATE_PUSH_REGISTRATION")
                    .putExtra("com.urbanairship.push.EXTRA_GCM_TOKEN_REFRESH", true);

            UAirship.getApplicationContext().startService(intent);
        }

        return true;
    }

    @Override
    public List<ReportingMessage> leaveBreadcrumb(String s) {
        return null;
    }

    @Override
    public List<ReportingMessage> logError(String s, Map<String, String> map) {
        return null;
    }

    @Override
    public List<ReportingMessage> logException(Exception e, Map<String, String> map, String s) {
        return null;
    }

    @Override
    public List<ReportingMessage> logEvent(MPEvent event) {
        logUrbanAirshipEvent(event);
        return Collections.singletonList(ReportingMessage.fromEvent(this, event));
    }

    @Override
    public List<ReportingMessage> logScreen(String screenName, Map<String, String> attributes) {
        UAirship.shared().getAnalytics().trackScreen(screenName);

        ReportingMessage message = new ReportingMessage(this, ReportingMessage.MessageType.SCREEN_VIEW, System.currentTimeMillis(), attributes);
        return Collections.singletonList(message);
    }

    @Override
    public List<ReportingMessage> logLtvIncrease(BigDecimal valueIncreased, BigDecimal totalValue, String eventName, Map<String, String> contextInfo) {
        CustomEvent customEvent = new CustomEvent.Builder(eventName)
                .setEventValue(valueIncreased)
                .create();

        UAirship.shared().getAnalytics().addEvent(customEvent);

        ReportingMessage message = new ReportingMessage(this, ReportingMessage.MessageType.EVENT, System.currentTimeMillis(), contextInfo);
        return Collections.singletonList(message);
    }

    @Override
    public List<ReportingMessage> logEvent(CommerceEvent commerceEvent) {
        List<ReportingMessage> messages = new LinkedList<>();

        if (logAirshipRetailEvents(commerceEvent)) {
            messages.add(ReportingMessage.fromEvent(this, commerceEvent));
        } else {
            for (MPEvent event : CommerceEventUtil.expand(commerceEvent)) {
                logUrbanAirshipEvent(event);
                messages.add(ReportingMessage.fromEvent(this, event));
            }
        }

        return messages;
    }

    @Override
    public void setUserIdentity(MParticle.IdentityType identityType, String identity) {
        String airshipId = getAirshipIdentifier(identityType);
        if (airshipId != null) {
            UAirship.shared().getAnalytics()
                    .editAssociatedIdentifiers()
                    .addIdentifier(airshipId, identity)
                    .apply();
        }
    }

    @Override
    public void removeUserIdentity(MParticle.IdentityType identityType) {
        String airshipId = getAirshipIdentifier(identityType);
        if (airshipId != null) {
            UAirship.shared().getAnalytics()
                    .editAssociatedIdentifiers()
                    .removeIdentifier(airshipId)
                    .apply();
        }
    }

    @Override
    public void setUserAttribute(String s, String s1) {
        // not supported
    }

    @Override
    public void setUserAttributeList(String s, List<String> list) {
        // not supported
    }

    @Override
    public boolean supportsAttributeLists() {
        return false;
    }

    @Override
    public void setAllUserAttributes(Map<String, String> map, Map<String, List<String>> map1) {
        // not supported
    }

    @Override
    public void removeUserAttribute(String s) {
        // not supported
    }

    @Override
    public List<ReportingMessage> logout() {
        // not supported
        return null;
    }

    /**
     * Logs Urban Airship RetailEvents from a CommerceEvent.
     *
     * @param event The commerce event.
     * @return {@code true} if retail events were able to be generated from the CommerceEvent,
     * otherwise {@code false}.
     */
    private boolean logAirshipRetailEvents(CommerceEvent event) {
        if (event.getProductAction() == null || event.getProducts().isEmpty()) {
            return false;
        }

        switch (event.getProductAction()) {
            case Product.PURCHASE:
                for (Product product : event.getProducts()) {
                    populateRetailEvent(RetailEvent.createPurchasedEvent(), product).track();
                }

                break;

            case Product.ADD_TO_CART:
                for (Product product : event.getProducts()) {
                    populateRetailEvent(RetailEvent.createAddedToCartEvent(), product).track();
                }

                break;

            case Product.CLICK:
                for (Product product : event.getProducts()) {
                    populateRetailEvent(RetailEvent.createBrowsedEvent(), product).track();
                }

                break;

            case Product.ADD_TO_WISHLIST:
                for (Product product : event.getProducts()) {
                    populateRetailEvent(RetailEvent.createStarredProduct(), product).track();
                }
                break;
            default:
                return false;

        }

        return true;
    }

    /**
     * Populates an Urban Airship RetailEvent from a product.
     *
     * @param event The retail event.
     * @param product The product.
     * @return The populated retail event.
     */
    private RetailEvent populateRetailEvent(RetailEvent event, Product product) {
        return event.setCategory(product.getCategory())
                .setId(product.getSku())
                .setDescription(product.getName())
                .setValue(product.getTotalAmount())
                .setBrand(product.getBrand());
    }

    /**
     * Logs an Urban Airship CustomEvent from an MPEvent.
     *
     * @param event The MPEvent.
     */
    private void logUrbanAirshipEvent(MPEvent event) {
        CustomEvent.Builder eventBuilder = new CustomEvent.Builder(event.getEventName());
        if (event.getInfo() != null) {
            for (Map.Entry<String, String> entry : event.getInfo().entrySet()) {
                eventBuilder.addProperty(entry.getKey(), entry.getValue());
            }
        }

        UAirship.shared().getAnalytics().addEvent(eventBuilder.create());
    }

    /**
     * Maps MParticle.IdentityType to an Urban Airship device identifier.
     *
     * @param identityType The mParticle identity type.
     * @return The Urban Airship identifier, or {@code null} if one does not exist.
     */
    private String getAirshipIdentifier(MParticle.IdentityType identityType) {
        switch (identityType) {
            case CustomerId:
                return IDENTITY_CUSTOMER_ID;

            case Facebook:
                return IDENTITY_FACEBOOK;

            case Twitter:
                return IDENTITY_TWITTER;

            case Google:
                return IDENTITY_GOOGLE;

            case Microsoft:
                return IDENTITY_MICROSOFT;

            case Yahoo:
                return IDENTITY_YAHOO;

            case Email:
                return IDENTITY_EMAIL;

            case FacebookCustomAudienceId:
                return IDENTITY_FACEBOOK_CUSTOM_AUDIENCE_ID;
        }

        return null;
    }

    /**
     * Sets the Urban Airship Channel ID as an mParticle integration attribute.
     */
    private void updateChannelIntegration() {
        String channelId = UAirship.shared().getPushManager().getChannelId();
        if (!TextUtils.isEmpty(channelId)) {
            HashMap<String, String> integrationAttributes = new HashMap<String, String>(1);
            integrationAttributes.put(UrbanAirshipKit.CHANNEL_ID_INTEGRATION_KEY, channelId);
            setIntegrationAttributes(integrationAttributes);
        }
    }
}