package com.mparticle.kits;

import android.content.Context;

import com.mparticle.MPEvent;
import com.mparticle.MParticle;
import com.mparticle.commerce.CommerceEvent;
import com.mparticle.commerce.Product;
import com.mparticle.commerce.TransactionAttributes;
import com.mparticle.internal.ConfigManager;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UrbanAirshipKitTest {

    private KitIntegration getKit() {
        return new UrbanAirshipKit();
    }

    @Test
    public void testGetName() throws Exception {
        String name = getKit().getName();
        assertTrue(name != null && name.length() > 0);
    }

    /**
     * Kit *should* throw an exception when they're initialized with the wrong settings.
     *
     */
    @Test
    public void testOnKitCreate() throws Exception{
        Exception e = null;
        try {
            KitIntegration kit = getKit();
            Map settings = new HashMap<>();
            settings.put("fake setting", "fake");
            kit.onKitCreate(settings, Mockito.mock(Context.class));
        }catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    public void testClassName() throws Exception {
        KitIntegrationFactory factory = new KitIntegrationFactory();
        Map<Integer, String> integrations = factory.getKnownIntegrations();
        String className = getKit().getClass().getName();
        for (Map.Entry<Integer, String> entry : integrations.entrySet()) {
            if (entry.getValue().equals(className)) {
                return;
            }
        }
        fail(className + " not found as a known integration.");
    }

    @Test
    public void testParsing() throws Exception {
        JSONObject config = new JSONObject("{ \"id\": 25, \"as\": { \"applicationKey\": \"this is the app key\", \"applicationSecret\": \"this is the app secret\", \"applicationMasterSecret\": \"mySecret\", \"enableTags\": \"True\", \"includeUserAttributes\": \"False\", \"notificationIconName\": \"Application Icon\", \"notificationColor\": \"System default\", \"namedUserIdField\": \"customerId\", \"eventUserTags\": \"[{\\\"map\\\":\\\"847138800\\\",\\\"value\\\":\\\"pressed\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"},{\\\"map\\\":\\\"-1394780343\\\",\\\"value\\\":\\\"screen1\\\",\\\"maptype\\\":\\\"EventClass.Id\\\"},{\\\"map\\\":\\\"-2010155734\\\",\\\"value\\\":\\\"cart\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"}]\", \"eventAttributeUserTags\": \"[{\\\"map\\\":\\\"245922523\\\",\\\"value\\\":\\\"gesture\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"245922523\\\",\\\"value\\\":\\\"a2ctid\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"1112195452\\\",\\\"value\\\":\\\"hello\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-897761755\\\",\\\"value\\\":\\\"a\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-635338283\\\",\\\"value\\\":\\\"b\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1165857198\\\",\\\"value\\\":\\\"c\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-2093257886\\\",\\\"value\\\":\\\"d\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"-599719438\\\",\\\"value\\\":\\\"e\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"}]\" }, \"hs\": {}, \"pr\": [] }");
        KitConfiguration kitConfig = MockKitConfiguration.createKitConfiguration(config);
        UrbanAirshipConfiguration urbanAirshipConfiguration = new UrbanAirshipConfiguration(kitConfig.getSettings());
        assertEquals("this is the app key", urbanAirshipConfiguration.getApplicationKey());
        assertEquals("this is the app secret", urbanAirshipConfiguration.getApplicationSecret());
        assertEquals(true, urbanAirshipConfiguration.getEnableTags());
        assertEquals(false, urbanAirshipConfiguration.getIncludeUserAttributes());
        assertEquals("Application Icon", urbanAirshipConfiguration.getNotificationIconName());
        assertEquals("System default", urbanAirshipConfiguration.getNotificationColor());
        assertEquals(MParticle.IdentityType.CustomerId, urbanAirshipConfiguration.getUserIdField());
        Map<Integer, ArrayList<String>> eventTags = urbanAirshipConfiguration.getEventClass();
        assertTrue(eventTags.get(-1394780343).get(0).equals("screen1"));

        eventTags = urbanAirshipConfiguration.getEventAttributeClass();
        assertTrue(eventTags.get(245922523).contains("gesture") && eventTags.get(245922523).contains("a2ctid"));
        assertTrue(eventTags.get(-2093257886).get(0).equals("d"));

        eventTags = urbanAirshipConfiguration.getEventClassDetails();
        assertTrue(eventTags.get(-2010155734).get(0).equals("cart"));
        assertTrue(eventTags.get(847138800).get(0).equals("pressed"));

        eventTags = urbanAirshipConfiguration.getEventAttributeClassDetails();
        assertTrue(eventTags.get(1112195452).get(0).equals("hello"));
        assertTrue(eventTags.get(-897761755).get(0).equals("a"));
        assertTrue(eventTags.get(-635338283).get(0).equals("b"));
        assertTrue(eventTags.get(-1165857198).get(0).equals("c"));
        assertTrue(eventTags.get(-599719438).get(0).equals("e"));
    }

    @Test
    public void testExtractEventName() throws Exception {
        MParticle.setInstance(Mockito.mock(MParticle.class));
        Mockito.when(MParticle.getInstance().getConfigManager()).thenReturn(Mockito.mock(ConfigManager.class));
        JSONObject config = new JSONObject("{ \"id\": 25, \"as\": { \"applicationKey\": \"1234456\", \"applicationSecret\": \"123456\", \"applicationMasterSecret\": \"123456\", \"enableTags\": \"True\", \"includeUserAttributes\": \"True\", \"notificationIconName\": \"Application Icon\", \"notificationColor\": \"System default\", \"namedUserIdField\": \"customerId\", \"eventUserTags\": \"[{\\\"map\\\":\\\"1824528343\\\",\\\"value\\\":\\\"test even tag\\\",\\\"maptype\\\":\\\"EventClass.Id\\\"},{\\\"map\\\":\\\"847138800\\\",\\\"value\\\":\\\"test screen tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"},{\\\"map\\\":\\\"1567\\\",\\\"value\\\":\\\"test ecomm add to cart tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"}]\", \"eventAttributeUserTags\": \"[{\\\"map\\\":\\\"-241024017\\\",\\\"value\\\":\\\"test event attribute\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"861397237\\\",\\\"value\\\":\\\"test screen attribute\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1854578855\\\",\\\"value\\\":\\\"test eComm attribute total amount\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1001670849\\\",\\\"value\\\":\\\"test eComm checkout promo code\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"}]\" }, \"hs\": { \"et\": { \"50\": 0, \"51\": 0 }, \"ec\": { \"-460386492\": 0, \"476338248\": 0, \"-1229406110\": 0, \"-1528980234\": 0, \"-138049017\": 0, \"360094366\": 0, \"-1711952615\": 0, \"1238657721\": 0, \"1057880655\": 0, \"-1415615126\": 0, \"-1573616412\": 0, \"-1417002190\": 0, \"1794482897\": 0, \"-1471969403\": 0, \"1981524391\": 0, \"1025548221\": 0, \"-956692642\": 0, \"-1535298586\": 0 }, \"ea\": { \"-1034789330\": 0, \"-820700541\": 0, \"454072115\": 0, \"1283264677\": 0, \"2132567239\": 0, \"644132244\": 0, \"-576148370\": 0, \"6478943\": 0, \"-1676187368\": 0, \"535860203\": 0, \"260811952\": 0, \"-2143124485\": 0, \"526806372\": 0, \"-261733467\": 0, \"-1809553213\": 0, \"1850278251\": 0 } }, \"pr\": [] }");
        KitConfiguration kitConfig = MockKitConfiguration.createKitConfiguration(config);
        UrbanAirshipConfiguration urbanAirshipConfiguration = new UrbanAirshipConfiguration(kitConfig.getSettings());
        UrbanAirshipKit kit = new UrbanAirshipKit();
        kit.setConfiguration(kitConfig);
        kit.setUrbanConfiguration(urbanAirshipConfiguration);
        MPEvent event = new MPEvent.Builder("Navigation 2").eventType(MParticle.EventType.Navigation).build();
        Set<String> set = kit.extractTags(event);
        assertEquals(1, set.size());
        assertEquals("test even tag", set.iterator().next());
    }

    @Test
    public void testExtractEventAttributes() throws Exception {
        MParticle.setInstance(Mockito.mock(MParticle.class));
        Mockito.when(MParticle.getInstance().getConfigManager()).thenReturn(Mockito.mock(ConfigManager.class));
        JSONObject config = new JSONObject("{ \"id\": 25, \"as\": { \"applicationKey\": \"1234456\", \"applicationSecret\": \"123456\", \"applicationMasterSecret\": \"123456\", \"enableTags\": \"True\", \"includeUserAttributes\": \"True\", \"notificationIconName\": \"Application Icon\", \"notificationColor\": \"System default\", \"namedUserIdField\": \"customerId\", \"eventUserTags\": \"[{\\\"map\\\":\\\"1824528343\\\",\\\"value\\\":\\\"test even tag\\\",\\\"maptype\\\":\\\"EventClass.Id\\\"},{\\\"map\\\":\\\"847138800\\\",\\\"value\\\":\\\"test screen tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"},{\\\"map\\\":\\\"1567\\\",\\\"value\\\":\\\"test ecomm add to cart tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"}]\", \"eventAttributeUserTags\": \"[{\\\"map\\\":\\\"-241024017\\\",\\\"value\\\":\\\"test event attribute\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"861397237\\\",\\\"value\\\":\\\"test screen attribute\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1854578855\\\",\\\"value\\\":\\\"test eComm attribute total amount\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1001670849\\\",\\\"value\\\":\\\"test eComm checkout promo code\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"}]\" }, \"hs\": { \"et\": { \"50\": 0, \"51\": 0 }, \"ec\": { \"-460386492\": 0, \"476338248\": 0, \"-1229406110\": 0, \"-1528980234\": 0, \"-138049017\": 0, \"360094366\": 0, \"-1711952615\": 0, \"1238657721\": 0, \"1057880655\": 0, \"-1415615126\": 0, \"-1573616412\": 0, \"-1417002190\": 0, \"1794482897\": 0, \"-1471969403\": 0, \"1981524391\": 0, \"1025548221\": 0, \"-956692642\": 0, \"-1535298586\": 0 }, \"ea\": { \"-1034789330\": 0, \"-820700541\": 0, \"454072115\": 0, \"1283264677\": 0, \"2132567239\": 0, \"644132244\": 0, \"-576148370\": 0, \"6478943\": 0, \"-1676187368\": 0, \"535860203\": 0, \"260811952\": 0, \"-2143124485\": 0, \"526806372\": 0, \"-261733467\": 0, \"-1809553213\": 0, \"1850278251\": 0 } }, \"pr\": [] }");
        KitConfiguration kitConfig = MockKitConfiguration.createKitConfiguration(config);
        UrbanAirshipConfiguration urbanAirshipConfiguration = new UrbanAirshipConfiguration(kitConfig.getSettings());
        UrbanAirshipKit kit = new UrbanAirshipKit();
        kit.setConfiguration(kitConfig);
        kit.setUrbanConfiguration(urbanAirshipConfiguration);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("searchTerm", "anything");
        MPEvent event = new MPEvent.Builder("search").eventType(MParticle.EventType.Search).info(attributes).build();
        Set<String> set = kit.extractTags(event);
        assertEquals(2, set.size());
        assertTrue(set.contains("test event attribute"));
        assertTrue(set.contains("test event attribute-anything"));
    }

    @Test
    public void testExtractScreenName() throws Exception {
        MParticle.setInstance(Mockito.mock(MParticle.class));
        Mockito.when(MParticle.getInstance().getConfigManager()).thenReturn(Mockito.mock(ConfigManager.class));
        JSONObject config = new JSONObject("{ \"id\": 25, \"as\": { \"applicationKey\": \"1234456\", \"applicationSecret\": \"123456\", \"applicationMasterSecret\": \"123456\", \"enableTags\": \"True\", \"includeUserAttributes\": \"True\", \"notificationIconName\": \"Application Icon\", \"notificationColor\": \"System default\", \"namedUserIdField\": \"customerId\", \"eventUserTags\": \"[{\\\"map\\\":\\\"1824528343\\\",\\\"value\\\":\\\"test even tag\\\",\\\"maptype\\\":\\\"EventClass.Id\\\"},{\\\"map\\\":\\\"847138800\\\",\\\"value\\\":\\\"test screen tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"},{\\\"map\\\":\\\"1567\\\",\\\"value\\\":\\\"test ecomm add to cart tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"}]\", \"eventAttributeUserTags\": \"[{\\\"map\\\":\\\"-241024017\\\",\\\"value\\\":\\\"test event attribute\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"861397237\\\",\\\"value\\\":\\\"test screen attribute\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1854578855\\\",\\\"value\\\":\\\"test eComm attribute total amount\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1001670849\\\",\\\"value\\\":\\\"test eComm checkout promo code\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"}]\" }, \"hs\": { \"et\": { \"50\": 0, \"51\": 0 }, \"ec\": { \"-460386492\": 0, \"476338248\": 0, \"-1229406110\": 0, \"-1528980234\": 0, \"-138049017\": 0, \"360094366\": 0, \"-1711952615\": 0, \"1238657721\": 0, \"1057880655\": 0, \"-1415615126\": 0, \"-1573616412\": 0, \"-1417002190\": 0, \"1794482897\": 0, \"-1471969403\": 0, \"1981524391\": 0, \"1025548221\": 0, \"-956692642\": 0, \"-1535298586\": 0 }, \"ea\": { \"-1034789330\": 0, \"-820700541\": 0, \"454072115\": 0, \"1283264677\": 0, \"2132567239\": 0, \"644132244\": 0, \"-576148370\": 0, \"6478943\": 0, \"-1676187368\": 0, \"535860203\": 0, \"260811952\": 0, \"-2143124485\": 0, \"526806372\": 0, \"-261733467\": 0, \"-1809553213\": 0, \"1850278251\": 0 } }, \"pr\": [] }");
        KitConfiguration kitConfig = MockKitConfiguration.createKitConfiguration(config);
        UrbanAirshipConfiguration urbanAirshipConfiguration = new UrbanAirshipConfiguration(kitConfig.getSettings());
        UrbanAirshipKit kit = new UrbanAirshipKit();
        kit.setConfiguration(kitConfig);
        kit.setUrbanConfiguration(urbanAirshipConfiguration);
        Set<String> set = kit.extractScreenTags("Screen Layout B", new HashMap<String, String>());
        assertEquals(1, set.size());
        assertEquals("test screen tag", set.iterator().next());
    }

    @Test
    public void testExtractScreenAttribute() throws Exception {
        MParticle.setInstance(Mockito.mock(MParticle.class));
        Mockito.when(MParticle.getInstance().getConfigManager()).thenReturn(Mockito.mock(ConfigManager.class));
        JSONObject config = new JSONObject("{ \"id\": 25, \"as\": { \"applicationKey\": \"1234456\", \"applicationSecret\": \"123456\", \"applicationMasterSecret\": \"123456\", \"enableTags\": \"True\", \"includeUserAttributes\": \"True\", \"notificationIconName\": \"Application Icon\", \"notificationColor\": \"System default\", \"namedUserIdField\": \"customerId\", \"eventUserTags\": \"[{\\\"map\\\":\\\"1824528343\\\",\\\"value\\\":\\\"test even tag\\\",\\\"maptype\\\":\\\"EventClass.Id\\\"},{\\\"map\\\":\\\"847138800\\\",\\\"value\\\":\\\"test screen tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"},{\\\"map\\\":\\\"1567\\\",\\\"value\\\":\\\"test ecomm add to cart tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"}]\", \"eventAttributeUserTags\": \"[{\\\"map\\\":\\\"-241024017\\\",\\\"value\\\":\\\"test event attribute\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"861397237\\\",\\\"value\\\":\\\"test screen attribute\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1854578855\\\",\\\"value\\\":\\\"test eComm attribute total amount\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1001670849\\\",\\\"value\\\":\\\"test eComm checkout promo code\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"}]\" }, \"hs\": { \"et\": { \"50\": 0, \"51\": 0 }, \"ec\": { \"-460386492\": 0, \"476338248\": 0, \"-1229406110\": 0, \"-1528980234\": 0, \"-138049017\": 0, \"360094366\": 0, \"-1711952615\": 0, \"1238657721\": 0, \"1057880655\": 0, \"-1415615126\": 0, \"-1573616412\": 0, \"-1417002190\": 0, \"1794482897\": 0, \"-1471969403\": 0, \"1981524391\": 0, \"1025548221\": 0, \"-956692642\": 0, \"-1535298586\": 0 }, \"ea\": { \"-1034789330\": 0, \"-820700541\": 0, \"454072115\": 0, \"1283264677\": 0, \"2132567239\": 0, \"644132244\": 0, \"-576148370\": 0, \"6478943\": 0, \"-1676187368\": 0, \"535860203\": 0, \"260811952\": 0, \"-2143124485\": 0, \"526806372\": 0, \"-261733467\": 0, \"-1809553213\": 0, \"1850278251\": 0 } }, \"pr\": [] }");
        KitConfiguration kitConfig = MockKitConfiguration.createKitConfiguration(config);
        UrbanAirshipConfiguration urbanAirshipConfiguration = new UrbanAirshipConfiguration(kitConfig.getSettings());
        UrbanAirshipKit kit = new UrbanAirshipKit();
        kit.setConfiguration(kitConfig);
        kit.setUrbanConfiguration(urbanAirshipConfiguration);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("version", "anything");

        Set<String> set = kit.extractScreenTags("Main Screen", attributes);
        assertEquals(2, set.size());
        assertTrue(set.contains("test screen attribute"));
        assertTrue(set.contains("test screen attribute-anything"));
    }

    @Test
    public void testExtractEcommEventType() throws Exception {
        MParticle.setInstance(Mockito.mock(MParticle.class));
        Mockito.when(MParticle.getInstance().getConfigManager()).thenReturn(Mockito.mock(ConfigManager.class));
        Mockito.when(MParticle.getInstance().getEnvironment()).thenReturn(MParticle.Environment.Development);
        JSONObject config = new JSONObject("{ \"id\": 25, \"as\": { \"applicationKey\": \"1234456\", \"applicationSecret\": \"123456\", \"applicationMasterSecret\": \"123456\", \"enableTags\": \"True\", \"includeUserAttributes\": \"True\", \"notificationIconName\": \"Application Icon\", \"notificationColor\": \"System default\", \"namedUserIdField\": \"customerId\", \"eventUserTags\": \"[{\\\"map\\\":\\\"1824528343\\\",\\\"value\\\":\\\"test even tag\\\",\\\"maptype\\\":\\\"EventClass.Id\\\"},{\\\"map\\\":\\\"847138800\\\",\\\"value\\\":\\\"test screen tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"},{\\\"map\\\":\\\"1567\\\",\\\"value\\\":\\\"test ecomm add to cart tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"}]\", \"eventAttributeUserTags\": \"[{\\\"map\\\":\\\"-241024017\\\",\\\"value\\\":\\\"test event attribute\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"861397237\\\",\\\"value\\\":\\\"test screen attribute\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1854578855\\\",\\\"value\\\":\\\"test eComm attribute total amount\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1001670849\\\",\\\"value\\\":\\\"test eComm checkout promo code\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"}]\" }, \"hs\": { \"et\": { \"50\": 0, \"51\": 0 }, \"ec\": { \"-460386492\": 0, \"476338248\": 0, \"-1229406110\": 0, \"-1528980234\": 0, \"-138049017\": 0, \"360094366\": 0, \"-1711952615\": 0, \"1238657721\": 0, \"1057880655\": 0, \"-1415615126\": 0, \"-1573616412\": 0, \"-1417002190\": 0, \"1794482897\": 0, \"-1471969403\": 0, \"1981524391\": 0, \"1025548221\": 0, \"-956692642\": 0, \"-1535298586\": 0 }, \"ea\": { \"-1034789330\": 0, \"-820700541\": 0, \"454072115\": 0, \"1283264677\": 0, \"2132567239\": 0, \"644132244\": 0, \"-576148370\": 0, \"6478943\": 0, \"-1676187368\": 0, \"535860203\": 0, \"260811952\": 0, \"-2143124485\": 0, \"526806372\": 0, \"-261733467\": 0, \"-1809553213\": 0, \"1850278251\": 0 } }, \"pr\": [] }");
        KitConfiguration kitConfig = MockKitConfiguration.createKitConfiguration(config);
        UrbanAirshipConfiguration urbanAirshipConfiguration = new UrbanAirshipConfiguration(kitConfig.getSettings());
        UrbanAirshipKit kit = new UrbanAirshipKit();
        kit.setConfiguration(kitConfig);
        kit.setUrbanConfiguration(urbanAirshipConfiguration);

        CommerceEvent event = new CommerceEvent.Builder(Product.ADD_TO_CART, new Product.Builder("name", "sku", 10).build()).build();
        Set<String> set = kit.extractCommerceTags(event);
        assertEquals(1, set.size());
        assertEquals("test ecomm add to cart tag", set.iterator().next());
    }

    @Test
    public void testExtractEcommAttribute() throws Exception {
        MParticle.setInstance(Mockito.mock(MParticle.class));
        Mockito.when(MParticle.getInstance().getConfigManager()).thenReturn(Mockito.mock(ConfigManager.class));
        Mockito.when(MParticle.getInstance().getEnvironment()).thenReturn(MParticle.Environment.Development);
        JSONObject config = new JSONObject("{ \"id\": 25, \"as\": { \"applicationKey\": \"1234456\", \"applicationSecret\": \"123456\", \"applicationMasterSecret\": \"123456\", \"enableTags\": \"True\", \"includeUserAttributes\": \"True\", \"notificationIconName\": \"Application Icon\", \"notificationColor\": \"System default\", \"namedUserIdField\": \"customerId\", \"eventUserTags\": \"[{\\\"map\\\":\\\"1824528343\\\",\\\"value\\\":\\\"test even tag\\\",\\\"maptype\\\":\\\"EventClass.Id\\\"},{\\\"map\\\":\\\"847138800\\\",\\\"value\\\":\\\"test screen tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"},{\\\"map\\\":\\\"1567\\\",\\\"value\\\":\\\"test ecomm add to cart tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"}]\", \"eventAttributeUserTags\": \"[{\\\"map\\\":\\\"-241024017\\\",\\\"value\\\":\\\"test event attribute\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"861397237\\\",\\\"value\\\":\\\"test screen attribute\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1854578855\\\",\\\"value\\\":\\\"test eComm attribute total amount\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1001670849\\\",\\\"value\\\":\\\"test eComm checkout promo code\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"}]\" }, \"hs\": { \"et\": { \"50\": 0, \"51\": 0 }, \"ec\": { \"-460386492\": 0, \"476338248\": 0, \"-1229406110\": 0, \"-1528980234\": 0, \"-138049017\": 0, \"360094366\": 0, \"-1711952615\": 0, \"1238657721\": 0, \"1057880655\": 0, \"-1415615126\": 0, \"-1573616412\": 0, \"-1417002190\": 0, \"1794482897\": 0, \"-1471969403\": 0, \"1981524391\": 0, \"1025548221\": 0, \"-956692642\": 0, \"-1535298586\": 0 }, \"ea\": { \"-1034789330\": 0, \"-820700541\": 0, \"454072115\": 0, \"1283264677\": 0, \"2132567239\": 0, \"644132244\": 0, \"-576148370\": 0, \"6478943\": 0, \"-1676187368\": 0, \"535860203\": 0, \"260811952\": 0, \"-2143124485\": 0, \"526806372\": 0, \"-261733467\": 0, \"-1809553213\": 0, \"1850278251\": 0 } }, \"pr\": [] }");
        KitConfiguration kitConfig = MockKitConfiguration.createKitConfiguration(config);
        UrbanAirshipConfiguration urbanAirshipConfiguration = new UrbanAirshipConfiguration(kitConfig.getSettings());
        UrbanAirshipKit kit = new UrbanAirshipKit();
        kit.setConfiguration(kitConfig);
        kit.setUrbanConfiguration(urbanAirshipConfiguration);

        CommerceEvent event = new CommerceEvent.Builder(Product.PURCHASE, new Product.Builder("name", "sku", 10).build()).transactionAttributes(
                new TransactionAttributes("id").setRevenue(10.0)
        ).build();
        Set<String> set = kit.extractCommerceTags(event);
        assertEquals(2, set.size());
        assertTrue(set.contains("test eComm attribute total amount"));
        assertTrue(set.toString(), set.contains("test eComm attribute total amount-10.0"));
    }

    @Test
    public void testExtractEcommAttribute2() throws Exception {
        MParticle.setInstance(Mockito.mock(MParticle.class));
        Mockito.when(MParticle.getInstance().getConfigManager()).thenReturn(Mockito.mock(ConfigManager.class));
        Mockito.when(MParticle.getInstance().getEnvironment()).thenReturn(MParticle.Environment.Development);
        JSONObject config = new JSONObject("{ \"id\": 25, \"as\": { \"applicationKey\": \"1234456\", \"applicationSecret\": \"123456\", \"applicationMasterSecret\": \"123456\", \"enableTags\": \"True\", \"includeUserAttributes\": \"True\", \"notificationIconName\": \"Application Icon\", \"notificationColor\": \"System default\", \"namedUserIdField\": \"customerId\", \"eventUserTags\": \"[{\\\"map\\\":\\\"1824528343\\\",\\\"value\\\":\\\"test even tag\\\",\\\"maptype\\\":\\\"EventClass.Id\\\"},{\\\"map\\\":\\\"847138800\\\",\\\"value\\\":\\\"test screen tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"},{\\\"map\\\":\\\"1567\\\",\\\"value\\\":\\\"test ecomm add to cart tag\\\",\\\"maptype\\\":\\\"EventClassDetails.Id\\\"}]\", \"eventAttributeUserTags\": \"[{\\\"map\\\":\\\"-241024017\\\",\\\"value\\\":\\\"test event attribute\\\",\\\"maptype\\\":\\\"EventAttributeClass.Id\\\"},{\\\"map\\\":\\\"861397237\\\",\\\"value\\\":\\\"test screen attribute\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1854578855\\\",\\\"value\\\":\\\"test eComm attribute total amount\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"},{\\\"map\\\":\\\"-1001670849\\\",\\\"value\\\":\\\"test eComm checkout promo code\\\",\\\"maptype\\\":\\\"EventAttributeClassDetails.Id\\\"}]\" }, \"hs\": { \"et\": { \"50\": 0, \"51\": 0 }, \"ec\": { \"-460386492\": 0, \"476338248\": 0, \"-1229406110\": 0, \"-1528980234\": 0, \"-138049017\": 0, \"360094366\": 0, \"-1711952615\": 0, \"1238657721\": 0, \"1057880655\": 0, \"-1415615126\": 0, \"-1573616412\": 0, \"-1417002190\": 0, \"1794482897\": 0, \"-1471969403\": 0, \"1981524391\": 0, \"1025548221\": 0, \"-956692642\": 0, \"-1535298586\": 0 }, \"ea\": { \"-1034789330\": 0, \"-820700541\": 0, \"454072115\": 0, \"1283264677\": 0, \"2132567239\": 0, \"644132244\": 0, \"-576148370\": 0, \"6478943\": 0, \"-1676187368\": 0, \"535860203\": 0, \"260811952\": 0, \"-2143124485\": 0, \"526806372\": 0, \"-261733467\": 0, \"-1809553213\": 0, \"1850278251\": 0 } }, \"pr\": [] }");
        KitConfiguration kitConfig = MockKitConfiguration.createKitConfiguration(config);
        UrbanAirshipConfiguration urbanAirshipConfiguration = new UrbanAirshipConfiguration(kitConfig.getSettings());
        UrbanAirshipKit kit = new UrbanAirshipKit();
        kit.setConfiguration(kitConfig);
        kit.setUrbanConfiguration(urbanAirshipConfiguration);


        Map<String, String> map = new HashMap<>();
        map.put("Promo Code", "this is a promo code");
        CommerceEvent event = new CommerceEvent.Builder(Product.CHECKOUT, new Product.Builder("name", "sku", 10).customAttributes(map).build()).build();
        Set<String> set = kit.extractCommerceTags(event);
        assertEquals(2, set.size());
        assertTrue(set.contains("test eComm checkout promo code"));
        assertTrue(set.toString(), set.contains("test eComm checkout promo code-this is a promo code"));
    }
}