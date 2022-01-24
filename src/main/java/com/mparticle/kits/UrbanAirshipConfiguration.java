package com.mparticle.kits;

import com.mparticle.MParticle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UrbanAirshipConfiguration {

    private static final String KEY_APP_KEY = "applicationKey";
    private static final String KEY_APP_SECRET = "applicationSecret";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_ENABLE_TAGS = "enableTags";
    private static final String KEY_USER_ID_FIELD = "namedUserIdField";
    private static final String KEY_EVENT_USER_TAGS = "eventUserTags";
    private static final String KEY_EVENT_ATTRIBUTE_USER_TAGS = "eventAttributeUserTags";
    private static final String KEY_NOTIFICATION_ICON_NAME = "notificationIconName";
    private static final String KEY_NOTIFICATION_COLOR = "notificationColor";
    private static final String KEY_INCLUDE_USER_ATTRIBUTES = "includeUserAttributes";

    private static final String NAMED_USER_TYPE_NONE = "none";
    private static final String NAMED_USER_TYPE_CUSTOMER_ID = "customerId";
    private static final String NAMED_USER_TYPE_EMAIL = "email";
    private static final String NAMED_USER_TYPE_OTHER = "other";

    private String applicationKey;
    private String applicationSecret;
    private String domain;
    private boolean enableTags;
    private boolean includeUserAttributes;
    private MParticle.IdentityType userIdField;
    private Map<Integer, ArrayList<String>> eventClass;
    private Map<Integer, ArrayList<String>> eventClassDetails;
    private Map<Integer, ArrayList<String>> eventAttributeClass;
    private Map<Integer, ArrayList<String>> eventAttributeClassDetails;
    private String notificationIconName = null;
    private String notificationColor = null;

    public UrbanAirshipConfiguration(Map<String, String> settings) {
        applicationKey = settings.get(KEY_APP_KEY);
        applicationSecret = settings.get(KEY_APP_SECRET);
        domain = settings.get(KEY_DOMAIN);
        enableTags = KitUtils.parseBooleanSetting(settings, KEY_ENABLE_TAGS, true);
        userIdField = parseNamedUserIdentityType(settings.get(KEY_USER_ID_FIELD));

        if (settings.containsKey(KEY_EVENT_USER_TAGS)) {
            String eventUserTagsString = settings.get(KEY_EVENT_USER_TAGS);
            try {
                JSONArray eventUserTagsJson = new JSONArray(eventUserTagsString);
                parseTagsJson(eventUserTagsJson);
            } catch (Exception ignored) { }
        }

        if (settings.containsKey(KEY_EVENT_ATTRIBUTE_USER_TAGS)) {
            String eventAttributeUserTagsString = settings.get(KEY_EVENT_ATTRIBUTE_USER_TAGS);
            try {
                JSONArray eventAttributeUserTagsJson = new JSONArray(eventAttributeUserTagsString);
                parseTagsJson(eventAttributeUserTagsJson);
            } catch (Exception ignored) { }
        }
        if (settings.containsKey(KEY_NOTIFICATION_COLOR)) {
            notificationColor = settings.get(KEY_NOTIFICATION_COLOR);
        }
        if (settings.containsKey(KEY_NOTIFICATION_ICON_NAME)) {
            notificationIconName = settings.get(KEY_NOTIFICATION_ICON_NAME);
        }
        includeUserAttributes = KitUtils.parseBooleanSetting(settings, KEY_INCLUDE_USER_ATTRIBUTES, false);
    }

    public Map<Integer, ArrayList<String>> getEventClass() {
        return eventClass;
    }

    public Map<Integer, ArrayList<String>> getEventClassDetails() {
        return eventClassDetails;
    }

    public Map<Integer, ArrayList<String>> getEventAttributeClass() {
        return eventAttributeClass;
    }

    public Map<Integer, ArrayList<String>> getEventAttributeClassDetails() {
        return eventAttributeClassDetails;
    }

    public String getNotificationIconName() {
        return notificationIconName;
    }

    public String getNotificationColor() {
        return notificationColor;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public String getDomain() {
        return domain;
    }

    public boolean getEnableTags() {
        return enableTags;
    }

    public MParticle.IdentityType getUserIdField() {
        return userIdField;
    }

    public boolean getIncludeUserAttributes() {
        return includeUserAttributes;
    }

    private void parseTagsJson(JSONArray tagsJson) {
        for (int i = 0; i < tagsJson.length(); i++) {
            try {
                JSONObject tagMap = tagsJson.getJSONObject(i);
                String mapType = tagMap.getString("maptype");
                String tagValue = tagMap.getString("value");
                int hash = tagMap.getInt("map");
                Map<Integer, ArrayList<String>> eventMap = null;
                if ("EventClass.Id".equals(mapType)) {
                    if (eventClass == null) {
                        eventClass = new HashMap<Integer, ArrayList<String>>();
                    }
                    eventMap = eventClass;
                } else if ("EventClassDetails.Id".equals(mapType)) {
                    if (eventClassDetails == null) {
                        eventClassDetails = new HashMap<Integer, ArrayList<String>>();
                    }
                    eventMap = eventClassDetails;
                } else if ("EventAttributeClass.Id".equals(mapType)) {
                    if (eventAttributeClass == null) {
                        eventAttributeClass = new HashMap<Integer, ArrayList<String>>();
                    }
                    eventMap = eventAttributeClass;
                } else if ("EventAttributeClassDetails.Id".equals(mapType)) {
                    if (eventAttributeClassDetails == null) {
                        eventAttributeClassDetails = new HashMap<Integer, ArrayList<String>>();
                    }
                    eventMap = eventAttributeClassDetails;
                } else {
                    eventMap = null;
                }
                if (eventMap != null) {
                    if (!eventMap.containsKey(hash)) {
                        eventMap.put(hash, new ArrayList<String>());
                    }
                    eventMap.get(hash).add(tagValue);
                }
            } catch (JSONException ignored) { }
        }
    }

    private static MParticle.IdentityType parseNamedUserIdentityType(String config) {
        if (config == null) {
            return null;
        }

        switch (config) {
            case NAMED_USER_TYPE_OTHER:
                return MParticle.IdentityType.Other;

            case NAMED_USER_TYPE_EMAIL:
                return MParticle.IdentityType.Email;

            case NAMED_USER_TYPE_CUSTOMER_ID:
                return MParticle.IdentityType.CustomerId;

            case NAMED_USER_TYPE_NONE:
            default:
                return null;
        }
    }
}