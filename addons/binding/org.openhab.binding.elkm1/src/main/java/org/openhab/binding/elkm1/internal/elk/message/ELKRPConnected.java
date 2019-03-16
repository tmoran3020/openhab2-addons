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
public class ELKRPConnected extends ElkMessage {
    private SocketReplyState state = SocketReplyState.UNKNOWN;

    public ELKRPConnected(String data) {
        super(ElkCommand.ELKRPConnected);
        state = SocketReplyState.fromTypeString(data);
    }

    @Override
    protected String getData() {
        return "";
    }

    public SocketReplyState getState() {
        return state;
    }

    public enum SocketReplyState {
        DISCONNECTED("00", "Elk disconnected"),
        CONNECTED("01", "Elk connected"),
        POWERUP("02", "Elk initializing after powerup or reboot"),
        UNKNOWN("", "Unknown reply");

        private String typeString;
        private String reason;

        SocketReplyState(String typeString, String reason) {
            this.typeString = typeString;
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }

        public static SocketReplyState fromTypeString(String typeString) {
            for (SocketReplyState state : values()) {
                if (state.typeString.equals(typeString)) {
                    return state;
                }
            }
            return UNKNOWN;
        }
    }
}
