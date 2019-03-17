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
package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * Acknowledge the socket is connected.
 *
 * @author Tim Moran - Initial Contribution
 */
public class SystemTroubleStatusReply extends ElkMessage {
    String data;

    public SystemTroubleStatusReply(String data) {
        super(ElkCommand.SystemTroubleStatusReply);
        this.data = data;
    }

    @Override
    protected String getData() {
        return data;
    }

    private boolean getBooleanFromData(int positionZeroBased) {
        if (data == null || data.length() <= positionZeroBased) {
            return true; // Data wasn't received
        }

        // return true only if non-zero (there is no trouble if 0)
        return ('0' != data.charAt(positionZeroBased));
    }

    private String getZone(int positionZeroBased) {
        if (data == null || data.length() <= positionZeroBased) {
            return ""; // Data wasn't received
        }

        int value = data.charAt(positionZeroBased);

        return String.valueOf(value - '0');
    }

    public boolean isGeneralTrouble() {
        for (char troubleStatus : data.toCharArray()) {
            if ('0' != troubleStatus) {
                return true;
            }
        }
        return false;
    }

    public boolean isAcFailTrouble() {
        return getBooleanFromData(0);
    }

    public boolean isBoxTamperTrouble() {
        return getBooleanFromData(1);
    }

    public String getBoxTamperZone() {
        return getZone(1);
    }

    public boolean isFailToCommunicate() {
        return getBooleanFromData(2);
    }

    public boolean isEepromMemoryErrorTrouble() {
        return getBooleanFromData(3);
    }

    public boolean isLowBatteryControlTrouble() {
        return getBooleanFromData(4);
    }

    public boolean isTransmitterLowBatteryTrouble() {
        return getBooleanFromData(5);
    }

    public String getTransmitterLowBatteryZone() {
        return getZone(5);
    }

    public boolean isOverCurrentTrouble() {
        return getBooleanFromData(6);
    }

    public boolean isTelephoneFaultTrouble() {
        return getBooleanFromData(7);
    }

    public boolean isOutput2Trouble() {
        return getBooleanFromData(9);
    }

    public boolean isMissingKeypadTrouble() {
        return getBooleanFromData(10);
    }

    public boolean isZoneExpanderTrouble() {
        return getBooleanFromData(11);
    }

    public boolean isOutputExpanderTrouble() {
        return getBooleanFromData(12);
    }

    public boolean isELKRPRemoteAccessTrouble() {
        return getBooleanFromData(14);
    }

    public boolean isCommonAreaNotArmedTrouble() {
        return getBooleanFromData(16);
    }

    public boolean isFlashMemoryErrorTrouble() {
        return getBooleanFromData(17);
    }

    public boolean isSecurityAlertTrouble() {
        return getBooleanFromData(18);
    }

    public String getSecurityAlertZone() {
        return getZone(18);
    }

    public boolean isSerialPortExpanderTrouble() {
        return getBooleanFromData(19);
    }

    public boolean isLostTransmitterTrouble() {
        return getBooleanFromData(20);
    }

    public String getLostTransmitterZone() {
        return getZone(20);
    }

    public boolean isGESmokeCleanMeTrouble() {
        return getBooleanFromData(21);
    }

    public boolean isEthernetTrouble() {
        return getBooleanFromData(22);
    }

    public boolean isDisplayMessageLine1() {
        return getBooleanFromData(31);
    }

    public boolean isDisplayMessageLine2() {
        return getBooleanFromData(32);
    }

    public boolean isFireTrouble() {
        return getBooleanFromData(33);
    }

    public String getFireTroubleZone() {
        return getZone(33);
    }
}
