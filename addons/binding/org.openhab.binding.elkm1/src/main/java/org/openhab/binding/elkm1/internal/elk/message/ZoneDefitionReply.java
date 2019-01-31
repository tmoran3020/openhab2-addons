/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkDefinition;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkMessageFactory;

/**
 * Returns the definitions of all the zones from the elk.
 *
 * @author David Bennett - Initial COntribution
 */
public class ZoneDefitionReply extends ElkMessage {
    private ElkDefinition[] definition;

    public ZoneDefitionReply(String data) {
        super(ElkCommand.ZoneDefinitionReply);
        if (data.length() >= ElkMessageFactory.MAX_ZONES) {
            byte[] dataBytes = data.getBytes();
            definition = new ElkDefinition[ElkMessageFactory.MAX_ZONES];
            for (int i = 0; i < dataBytes.length && i < ElkMessageFactory.MAX_ZONES; i++) {
                int val = dataBytes[i] - 0x30;
                if (val > ElkDefinition.values().length) {
                    definition[i] = ElkDefinition.Disabled;
                } else {
                    definition[i] = ElkDefinition.values()[val];
                }
            }
        }
    }

    public ElkDefinition[] getDefinition() {
        return definition;
    }

    @Override
    protected String getData() {
        return null;
    }

}
