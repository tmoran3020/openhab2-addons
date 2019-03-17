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
package org.openhab.binding.elkm1.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ElkAlarmBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */

@NonNullByDefault
public class ElkM1BindingConstants {

    public static final String BINDING_ID = "elkm1";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_AREA = new ThingTypeUID(BINDING_ID, "area");

    // List of all Channel ids (zone)
    public static final String CHANNEL_ZONE_AREA = "area";
    public static final String CHANNEL_ZONE_CONFIG = "config";
    public static final String CHANNEL_ZONE_STATUS = "status";
    public static final String CHANNEL_ZONE_DEFINITION = "definition";

    // List of all Channel ids (area)
    public static final String CHANNEL_AREA_STATE = "state";
    public static final String CHANNEL_AREA_ARMUP = "armup";
    public static final String CHANNEL_AREA_ARMED = "armed";

    // List of all Channel ids (bridge)
    public static final String CHANNEL_BRIDGE_GENERAL_TROUBLE = "general_trouble";
    public static final String CHANNEL_BRIDGE_AC_TROUBLE = "ac_fail_trouble";
    public static final String CHANNEL_BRIDGE_BOX_TAMPER_TROUBLE = "box_tamper_trouble";
    public static final String CHANNEL_BRIDGE_BOX_TAMPER_ZONE = "box_tamper_zone";
    public static final String CHANNEL_BRIDGE_FAIL_COMMUNICATE_TROUBLE = "fail_communicate_trouble";
    public static final String CHANNEL_BRIDGE_EEPROM_MEMORY_TROUBLE = "eeprom_memory_error_trouble";
    public static final String CHANNEL_BRIDGE_LOW_BATTERY_TROUBLE = "low_battery_control_trouble";
    public static final String CHANNEL_BRIDGE_TRANSMITTER_LOW_BATTERY_TROUBLE = "transmitter_low_battery_trouble";
    public static final String CHANNEL_BRIDGE_TRANSMITTER_LOW_BATTERY_ZONE = "transmitter_low_battery_zone";
    public static final String CHANNEL_BRIDGE_OVER_CURRENT_TROUBLE = "over_current_trouble";
    public static final String CHANNEL_BRIDGE_TELEPHONE_FAULT_TROUBLE = "telephone_fault_trouble";
    public static final String CHANNEL_BRIDGE_OUTPUT_2_TROUBLE = "output_2_trouble";
    public static final String CHANNEL_BRIDGE_MISSING_KEYPAD_TROUBLE = "missing_keypad_trouble";
    public static final String CHANNEL_BRIDGE_ZONE_EXPANDER_TROUBLE = "zone_expander_trouble";
    public static final String CHANNEL_BRIDGE_OUTPUT_EXPANDER_TROUBLE = "output_expander_trouble";
    public static final String CHANNEL_BRIDGE_ELKRP_REMOTE_ACCESS_TROUBLE = "elkrp_remote_access_trouble";
    public static final String CHANNEL_BRIDGE_COMMON_AREA_NOT_ARMED_TROUBLE = "common_area_not_armed_trouble";
    public static final String CHANNEL_BRIDGE_FLASH_MEMORY_ERROR_TROUBLE = "flash_memory_error_trouble";
    public static final String CHANNEL_BRIDGE_SECURITY_ALERT_TROUBLE = "security_alert_trouble";
    public static final String CHANNEL_BRIDGE_SECURITY_ALERT_ZONE = "security_alert_zone";
    public static final String CHANNEL_BRIDGE_SERIAL_PORT_EXPANDER_TROUBLE = "serial_port_expander_trouble";
    public static final String CHANNEL_BRIDGE_LOST_TRANSMITTER_TROUBLE = "lost_transmitter_trouble";
    public static final String CHANNEL_BRIDGE_LOST_TRANSMITTER_ZONE = "lost_transmitter_zone";
    public static final String CHANNEL_BRIDGE_GE_SMOKE_CLEANME_TROUBLE = "ge_smoke_cleanme_trouble";
    public static final String CHANNEL_BRIDGE_ETHERNET_TROUBLE = "ethernet_trouble";
    public static final String CHANNEL_BRIDGE_DISPLAY_MESSAGE_LINE1 = "display_message_line1";
    public static final String CHANNEL_BRIDGE_DISPLAY_MESSAGE_LINE2 = "display_message_line2";
    public static final String CHANNEL_BRIDGE_FIRE_TROUBLE = "fire_trouble";
    public static final String CHANNEL_BRIDGE_FIRE_TROUBLE_ZONE = "fire_trouble_zone";

    // The properties associated with the thing
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_ZONE_NUM = "zoneId";
    public static final String PROPERTY_AREA_NUM = "areaId";

}
