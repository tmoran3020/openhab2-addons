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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.elkm1.internal.ElkM1BindingConstants;
import org.openhab.binding.elkm1.internal.config.ElkZoneThingConfiguration;
import org.openhab.binding.elkm1.internal.elk.ElkDefinition;
import org.openhab.binding.elkm1.internal.elk.ElkTypeToRequest;
import org.openhab.binding.elkm1.internal.elk.ElkZoneConfig;
import org.openhab.binding.elkm1.internal.elk.ElkZoneStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Zone handler to handle updates to the zones (locations)
 *
 * @author David Bennett - Initial Contribution
 */
public class ElkM1ZoneHandler extends BaseThingHandler implements ElkM1Handler {
    private Logger logger = LoggerFactory.getLogger(ElkM1ZoneHandler.class);
    private ElkZoneThingConfiguration config;

    public ElkM1ZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(ElkZoneThingConfiguration.class);

        if (validConfig(config)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Elk M1 zone configuration invalid");
        }
    }

    public boolean validConfig(ElkZoneThingConfiguration config) {
        if (config == null || config.zoneId <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            getElkM1BridgeHandler().refreshZones();
        }
    }

    /**
     * Updates the zone config with the new zone config.
     *
     * @param config the config to update
     * @param status the status to update
     */
    public void updateZoneConfig(ElkZoneConfig config, ElkZoneStatus status) {
        logger.debug("Update Elk Zone id {} config {} to: {}", getZoneId(), config, status);
        Channel chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_CONFIG);
        if (chan != null) {
            updateState(chan.getUID(), new StringType(config.toString()));
        }
        chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_STATUS);
        if (chan != null) {
            updateState(chan.getUID(), new StringType(status.toString()));
        }
        chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_DEFINITION);
    }

    public int getZoneId() {
        return config.zoneId;
    }

    /**
     * The definition of the zone to update.
     *
     * @param definition the new defintion
     */
    public void updateZoneDefinition(ElkDefinition definition) {
        logger.debug("Updated Elk zone id {} definition to: {}", getZoneId(), definition);
        Channel chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_DEFINITION);
        if (chan != null) {
            updateState(chan.getUID(), new StringType(definition.toString()));
        }
    }

    /**
     *
     * @param area the new area the zone is inside
     */
    public void updateZoneArea(int area) {
        logger.debug("Updated Elk zone id {} area to: {}", getZoneId(), area);
        Channel chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_AREA);
        if (chan != null) {
            updateState(chan.getUID(), new DecimalType(area));
        }
    }

    @Override
    public boolean isThingForType(ElkTypeToRequest type, int id) {
        if (ElkTypeToRequest.Zone != type) {
            return false;
        }
        return (getZoneId() == id);
    }

    @SuppressWarnings("null")
    private ElkM1BridgeHandler getElkM1BridgeHandler() {
        return (ElkM1BridgeHandler) getBridge().getHandler();
    }
}
