/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.elkm1.internal.handler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.elkm1.internal.ElkM1BindingConstants;
import org.openhab.binding.elkm1.internal.ElkM1HandlerListener;
import org.openhab.binding.elkm1.internal.config.ElkAlarmConfig;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmArmedState;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmConnection;
import org.openhab.binding.elkm1.internal.elk.ElkDefinition;
import org.openhab.binding.elkm1.internal.elk.ElkListener;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkMessageFactory;
import org.openhab.binding.elkm1.internal.elk.ElkTypeToRequest;
import org.openhab.binding.elkm1.internal.elk.message.ArmAway;
import org.openhab.binding.elkm1.internal.elk.message.ArmToNight;
import org.openhab.binding.elkm1.internal.elk.message.ArmToNightInstant;
import org.openhab.binding.elkm1.internal.elk.message.ArmToStayHome;
import org.openhab.binding.elkm1.internal.elk.message.ArmToStayInstant;
import org.openhab.binding.elkm1.internal.elk.message.ArmToVacation;
import org.openhab.binding.elkm1.internal.elk.message.ArmingStatus;
import org.openhab.binding.elkm1.internal.elk.message.ArmingStatusReply;
import org.openhab.binding.elkm1.internal.elk.message.Disarm;
import org.openhab.binding.elkm1.internal.elk.message.StringTextDescription;
import org.openhab.binding.elkm1.internal.elk.message.StringTextDescriptionReply;
import org.openhab.binding.elkm1.internal.elk.message.SystemTroubleStatus;
import org.openhab.binding.elkm1.internal.elk.message.SystemTroubleStatusReply;
import org.openhab.binding.elkm1.internal.elk.message.Version;
import org.openhab.binding.elkm1.internal.elk.message.VersionReply;
import org.openhab.binding.elkm1.internal.elk.message.ZoneChangeUpdate;
import org.openhab.binding.elkm1.internal.elk.message.ZoneDefinition;
import org.openhab.binding.elkm1.internal.elk.message.ZoneDefitionReply;
import org.openhab.binding.elkm1.internal.elk.message.ZonePartition;
import org.openhab.binding.elkm1.internal.elk.message.ZonePartitionReply;
import org.openhab.binding.elkm1.internal.elk.message.ZoneStatus;
import org.openhab.binding.elkm1.internal.elk.message.ZoneStatusReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElkM1BridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Bennett - Initial contribution
 */
public class ElkM1BridgeHandler extends BaseBridgeHandler implements ElkListener {
    private Logger logger = LoggerFactory.getLogger(ElkM1BridgeHandler.class);

    private ElkAlarmConnection connection;
    private ElkMessageFactory messageFactory;
    private boolean[] areas = new boolean[ElkMessageFactory.MAX_AREAS];
    private List<ElkM1HandlerListener> listeners = new ArrayList<ElkM1HandlerListener>();
    private ScheduledFuture<?> reconnectFuture;
    private long lastConnectionInEpochSeconds;
    private int RESCHEDULE_LAG_SECONDS = 10;

    public ElkM1BridgeHandler(Bridge thing) {
        super(thing);
        for (int i = 0; i < areas.length; i++) {
            areas[i] = false;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            connection.sendCommand(new SystemTroubleStatus());
        }
    }

    /**
     * Initialize the bridge to do stuff.
     */
    @Override
    public void initialize() {
        logger.debug("Intializing Elk M1");
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Opening alarm connection");

        // Load up the config and then get the connection to the elk setup.
        messageFactory = new ElkMessageFactory();
        ElkAlarmConfig config = getConfigAs(ElkAlarmConfig.class);
        connection = new ElkAlarmConnection(config, messageFactory);
        connection.addElkListener(this);
        if (connection.initialize()) {
            updateStatus(ThingStatus.ONLINE);
            refreshBridge();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to open socket to alarm");
            // May be temporary, try to reconnect
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (this.reconnectFuture != null) {
            // If we have a reconnect already running, cancel it out and schedule a new one for later
            this.reconnectFuture.cancel(false);
        }
        this.reconnectFuture = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                logger.info("Reconnecting to Elk M1 due to initialization or heartbeat failure.");
                reconnect();
            }
            // We should get at minimum an Ethernet Test every thirty seconds, give it a bit longer just in case
        }, 60, TimeUnit.SECONDS);

    }

    /**
     * Called when the configuration is updated. We will reconnect to the elk at this point.
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Reconnecting to Elk M1 after configuration update");
        super.handleConfigurationUpdate(configurationParameters);
        reconnect();
    }

    private void reconnect() {
        this.connection.removeElkListener(this);
        this.connection.shutdown();
        this.connection = new ElkAlarmConnection(getConfigAs(ElkAlarmConfig.class), messageFactory);
        connection.addElkListener(this);
        if (connection.initialize()) {
            updateStatus(ThingStatus.ONLINE);
            refreshBridge();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to open socket to alarm");
            // May be temporary, try to reconnect
            scheduleReconnect();
        }
    }

    private void refreshBridge() {
        connection.sendCommand(new Version());
        connection.sendCommand(new ZoneDefinition());
        connection.sendCommand(new ZonePartition());
        connection.sendCommand(new ZoneStatus());
        connection.sendCommand(new ArmingStatus());
        connection.sendCommand(new SystemTroubleStatus());
    }

    /**
     * Shutdown the bridge.
     */
    @Override
    public void dispose() {
        connection.shutdown();
        areas = null;
        connection = null;
        messageFactory = null;
        assert (listeners.isEmpty());
        super.dispose();
    }

    /**
     * Handlers an incoming message from the elk system.
     *
     * @param message The message from the elk to handle
     */
    @Override
    public void handleElkMessage(ElkMessage message) {
        logger.debug("Got Elk Message: {}", message.toString());

        // We're still alive, reset the heatbeat listener
        handleHeartbeat();

        if (message instanceof VersionReply) {
            VersionReply reply = (VersionReply) message;
            // Set the property.
            getThing().setProperty(ElkM1BindingConstants.PROPERTY_VERSION, reply.getElkVersion());
        }
        if (message instanceof ZoneStatusReply) {
            ZoneStatusReply reply = (ZoneStatusReply) message;
            for (int i = 0; i < ElkMessageFactory.MAX_ZONES; i++) {
                Thing thing = getThingForType(ElkTypeToRequest.Zone, i + 1);
                if (thing != null) {
                    ElkM1ZoneHandler handler = (ElkM1ZoneHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateZoneConfig(reply.getConfig()[i], reply.getStatus()[i]);
                    }
                }
            }
        }
        if (message instanceof ZonePartitionReply) {
            ZonePartitionReply reply = (ZonePartitionReply) message;
            for (int i = 0; i < ElkMessageFactory.MAX_ZONES; i++) {
                Thing thing = getThingForType(ElkTypeToRequest.Area, reply.getAreas()[i]);
                if (thing == null && reply.getAreas()[i] != 0 && !areas[reply.getAreas()[i] - 1]) {
                    // Request the area.
                    connection.sendCommand(new StringTextDescription(ElkTypeToRequest.Area, reply.getAreas()[i]));
                    areas[reply.getAreas()[i] - 1] = true;
                    logger.debug("Requesting Elk Area: {}", reply.getAreas()[i]);
                }
                thing = getThingForType(ElkTypeToRequest.Zone, i + 1);
                if (thing != null) {
                    ElkM1ZoneHandler handler = (ElkM1ZoneHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateZoneArea(reply.getAreas()[i]);
                    }
                }
            }
        }
        if (message instanceof ZoneDefitionReply) {
            ZoneDefitionReply reply = (ZoneDefitionReply) message;
            for (int i = 0; i < ElkMessageFactory.MAX_ZONES; i++) {
                if (reply.getDefinition()[i] != ElkDefinition.Disabled) {
                    connection.sendCommand(new StringTextDescription(ElkTypeToRequest.Zone, i + 1));
                    logger.debug("Requesting Elk Zone: {}", i);
                }
                Thing thing = getThingForType(ElkTypeToRequest.Zone, i + 1);
                if (thing != null) {
                    ElkM1ZoneHandler handler = (ElkM1ZoneHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateZoneDefinition(reply.getDefinition()[i]);
                    }
                }
            }
        }
        if (message instanceof ZoneChangeUpdate) {
            ZoneChangeUpdate reply = (ZoneChangeUpdate) message;
            Thing thing = getThingForType(ElkTypeToRequest.Zone, reply.getZoneNumber());
            if (thing != null) {
                ElkM1ZoneHandler handler = (ElkM1ZoneHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateZoneConfig(reply.getConfig(), reply.getStatus());
                }
            }
        }
        if (message instanceof ArmingStatusReply) {
            ArmingStatusReply reply = (ArmingStatusReply) message;
            // Do stuff.
            for (int i = 0; i < ElkMessageFactory.MAX_AREAS; i++) {
                Thing thing = getThingForType(ElkTypeToRequest.Area, i + 1);
                if (thing != null) {
                    ElkM1AreaHandler handler = (ElkM1AreaHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateArea(reply.getState()[i], reply.getArmed()[i], reply.getArmedUp()[i]);
                    }
                }
            }
        }
        if (message instanceof StringTextDescriptionReply) {
            StringTextDescriptionReply reply = (StringTextDescriptionReply) message;
            switch (reply.getTypeResponse()) {
                case Zone:
                    // Once we have a description, see if this zone exists.
                    Thing thing = getThingForType(ElkTypeToRequest.Zone, reply.getThingNum());
                    if (thing == null) {
                        for (ElkM1HandlerListener listener : this.listeners) {
                            listener.onZoneDiscovered(reply.getThingNum(), reply.getText());
                        }
                    }
                    break;
                case Area:
                    thing = getThingForType(ElkTypeToRequest.Area, reply.getThingNum());
                    if (thing == null) {
                        for (ElkM1HandlerListener listener : this.listeners) {
                            listener.onAreaDiscovered(reply.getThingNum(), reply.getText());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        if (message instanceof SystemTroubleStatusReply) {
            updateTroubleStatus((SystemTroubleStatusReply) message);
        }
    }

    /**
     * Handle heartbeat communications
     */
    private void handleHeartbeat() {
        // We sometimes get waves of updates, no need to reset the future for each message. Give it a lag time before
        // we go ahead and reschedule
        if (Instant.now().getEpochSecond() - this.lastConnectionInEpochSeconds < RESCHEDULE_LAG_SECONDS) {
            return;
        } else {
            this.lastConnectionInEpochSeconds = Instant.now().getEpochSecond();
            scheduleReconnect();
        }
    }

    /**
     * Adds a listener to this bridge.
     */
    public void addListener(ElkM1HandlerListener listener) {
        synchronized (listeners) {
            this.listeners.add(listener);
        }
    }

    /**
     * Removes a listener from this bridge.
     */
    public void removeListener(ElkM1HandlerListener listener) {
        synchronized (listeners) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Gets the thing associated with the type/number.
     *
     * @param type the type to look for
     * @param num  the number of the type to look for
     * @return the thing, null if not found
     */
    Thing getThingForType(ElkTypeToRequest type, int num) {
        for (Thing thing : getThing().getThings()) {
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler instanceof ElkM1Handler) {
                ElkM1Handler baseHandler = (ElkM1Handler) thingHandler;
                if (baseHandler.isThingForType(type, num)) {
                    logger.debug("Found thing for type {} and num {}", type.toString(), num);
                    return thing;
                }
            }
        }
        return null;
    }

    /**
     * Starts a scan by asking for the zone status. This is called from the discovery handler.
     */
    public void startScan() {
        connection.sendCommand(new ZoneStatus());
    }

    /**
     * Refreshes the zones.
     */
    public void refreshZones() {
        if (!connection.isSendingClass(ZoneStatus.class)) {
            connection.sendCommand(new ZoneStatus());
        }
    }

    /**
     * Refreshes the areas.
     */
    public void refreshArea() {
        if (!connection.isSendingClass(ArmingStatus.class)) {
            connection.sendCommand(new ArmingStatus());
        }
    }

    /**
     * Sends the right command to the elk to change the alarmed state for the m1 gold.
     *
     * @param area  The area to alarm
     * @param armed The state to set it to
     */
    public void updateArmedState(int area, ElkAlarmArmedState armed) {
        ElkAlarmConfig config = getConfigAs(ElkAlarmConfig.class);
        String pincode = String.format("%06d", config.pincode);
        switch (armed) {
            case ArmedAway:
                connection.sendCommand(new ArmAway(area, pincode));
                break;
            case Disarmed:
                connection.sendCommand(new Disarm(area, pincode));
                break;
            case ArmedStay:
                connection.sendCommand(new ArmToStayHome(area, pincode));
                break;
            case ArmedStayInstant:
                connection.sendCommand(new ArmToStayInstant(area, pincode));
                break;
            case ArmedToNight:
                connection.sendCommand(new ArmToNight(area, pincode));
                break;
            case ArmedToNightInstant:
                connection.sendCommand(new ArmToNightInstant(area, pincode));
                break;
            case ArmedToVacation:
                connection.sendCommand(new ArmToVacation(area, pincode));
                break;
        }
    }

    public void updateTroubleStatus(SystemTroubleStatusReply message) {
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_GENERAL_TROUBLE, message.isGeneralTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_AC_TROUBLE, message.isAcFailTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_BOX_TAMPER_TROUBLE, message.isBoxTamperTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_BOX_TAMPER_ZONE, message.getBoxTamperZone());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_FAIL_COMMUNICATE_TROUBLE,
                message.isFailToCommunicate());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_EEPROM_MEMORY_TROUBLE,
                message.isEepromMemoryErrorTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_LOW_BATTERY_TROUBLE,
                message.isLowBatteryControlTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_TRANSMITTER_LOW_BATTERY_TROUBLE,
                message.isTransmitterLowBatteryTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_TRANSMITTER_LOW_BATTERY_ZONE,
                message.getTransmitterLowBatteryZone());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_OVER_CURRENT_TROUBLE, message.isOverCurrentTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_TELEPHONE_FAULT_TROUBLE,
                message.isTelephoneFaultTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_OUTPUT_2_TROUBLE, message.isOutput2Trouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_MISSING_KEYPAD_TROUBLE,
                message.isMissingKeypadTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_ZONE_EXPANDER_TROUBLE,
                message.isZoneExpanderTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_OUTPUT_EXPANDER_TROUBLE,
                message.isOutputExpanderTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_ELKRP_REMOTE_ACCESS_TROUBLE,
                message.isELKRPRemoteAccessTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_COMMON_AREA_NOT_ARMED_TROUBLE,
                message.isCommonAreaNotArmedTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_FLASH_MEMORY_ERROR_TROUBLE,
                message.isFlashMemoryErrorTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_SECURITY_ALERT_TROUBLE,
                message.isSecurityAlertTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_SECURITY_ALERT_ZONE, message.getSecurityAlertZone());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_SERIAL_PORT_EXPANDER_TROUBLE,
                message.isSerialPortExpanderTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_LOST_TRANSMITTER_TROUBLE,
                message.isLostTransmitterTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_LOST_TRANSMITTER_ZONE,
                message.getLostTransmitterZone());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_GE_SMOKE_CLEANME_TROUBLE,
                message.isGESmokeCleanMeTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_ETHERNET_TROUBLE, message.isEthernetTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_DISPLAY_MESSAGE_LINE1,
                message.isDisplayMessageLine1());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_DISPLAY_MESSAGE_LINE2,
                message.isDisplayMessageLine2());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_FIRE_TROUBLE, message.isFireTrouble());
        updateBridgeChannel(ElkM1BindingConstants.CHANNEL_BRIDGE_FIRE_TROUBLE_ZONE, message.getFireTroubleZone());
    }

    private void updateBridgeChannel(String channel, boolean isTrouble) {
        Channel chan = getThing().getChannel(channel);
        if (chan != null) {
            updateState(chan.getUID(), OnOffType.from(isTrouble));
        }
    }

    private void updateBridgeChannel(String channel, String zone) {
        Channel chan = getThing().getChannel(channel);
        if (chan != null) {
            updateState(chan.getUID(), StringType.valueOf(zone));
        }
    }

}
