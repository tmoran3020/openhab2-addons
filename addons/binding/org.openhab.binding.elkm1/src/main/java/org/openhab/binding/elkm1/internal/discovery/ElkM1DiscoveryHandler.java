/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.elkm1.internal.ElkM1BindingConstants;
import org.openhab.binding.elkm1.internal.ElkM1HandlerListener;
import org.openhab.binding.elkm1.internal.elk.ElkTypeToRequest;
import org.openhab.binding.elkm1.internal.handler.ElkM1BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the discovery results and details from the elk m1 when it is found.
 *
 * @author David Bennett - Initial Contribution
 */
public class ElkM1DiscoveryHandler extends AbstractDiscoveryService implements ElkM1HandlerListener {
    private Logger logger = LoggerFactory.getLogger(ElkM1DiscoveryHandler.class);

    private ElkM1BridgeHandler bridge;

    public ElkM1DiscoveryHandler(ElkM1BridgeHandler bridge) throws IllegalArgumentException {
        super(60);
        this.bridge = bridge;
    }

    @Override
    public void activate(Map<String, Object> configProperties) {
        logger.info("Elk M1 Binding Activated");
        super.activate(configProperties);
        this.bridge.addListener(this);
    }

    @Override
    public void deactivate() {
        logger.info("Elk M1 Binding Deactivated");
        super.deactivate();
        this.bridge.removeListener(this);
    }

    @Override
    public void onZoneDiscovered(int zoneNum, String label) {
        logger.info("Elk Zone {} Discovered: {}", zoneNum, label);
        ThingUID thingUID = new ThingUID(ElkM1BindingConstants.THING_TYPE_ZONE, bridge.getThing().getUID(),
                Integer.toString(zoneNum));
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(ElkM1BindingConstants.PROPERTY_TYPE_ID, ElkTypeToRequest.Zone.toString());
        properties.put(ElkM1BindingConstants.PROPERTY_ZONE_NUM, Integer.toString(zoneNum));
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridge.getThing().getUID())
                .withLabel(label).withProperties(properties).build();
        thingDiscovered(result);
    }

    @Override
    protected void startScan() {
        this.bridge.startScan();
    }

    @Override
    public void onAreaDiscovered(int areaNum, String label) {
        logger.info("Elk Area {} Discovered: {}", areaNum, label);
        ThingUID thingUID = new ThingUID(ElkM1BindingConstants.THING_TYPE_AREA, bridge.getThing().getUID(),
                Integer.toString(areaNum));
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(ElkM1BindingConstants.PROPERTY_TYPE_ID, ElkTypeToRequest.Area.toString());
        properties.put(ElkM1BindingConstants.PROPERTY_ZONE_NUM, Integer.toString(areaNum));
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridge.getThing().getUID())
                .withLabel(label).withProperties(properties).build();
        thingDiscovered(result);
    }

}
