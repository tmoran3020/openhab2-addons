/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.elk;

/**
 * The definition for a zone.
 *
 * @author David Bennett - Initial COntribution
 */
public enum ElkDefinition {
    Disabled,
    BurglarEntryExit1,
    BurglarEntryExit2,
    BurglarPerimeterInstant,
    BurglarInterior,
    BurglarInteriorFollower,
    BurglarInteriorNight,
    BurglarInteriorNightDelay,
    Burglar24Hour,
    BurglarBoxTamper,
    FireAlarm,
    FireVerified,
    FireSupervisory,
    AuxAlarm1,
    AuxAlarm2,
    Keyfob,
    NonAlarm,
    CarbonMonoxide,
    EmergencyAlarm,
    FreezeAlarm,
    GasAlarm,
    HeatAlarm,
    MedicalAlarm,
    PoliceAlarm,
    PoliceNoIndication,
    WaterAlarm,
    KeyMomentaryArmDisarm,
    KeyMomentaryArmAway,
    KeyMomentaryArmStay,
    KeyMomentaryDisarm,
    KeyOnOff,
    MuteAudibles,
    PowerSupervisory,
    Temperature,
    AnalogZone,
    PhoneKey,
    IntercomKey,
}
