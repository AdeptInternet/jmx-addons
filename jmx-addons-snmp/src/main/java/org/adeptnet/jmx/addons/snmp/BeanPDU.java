/*
 * Copyright 2014 Francois Steyn - Adept Internet (PTY) LTD (francois.s@adept.co.za).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.adeptnet.jmx.addons.snmp;

import java.util.HashMap;
import java.util.Map;
import org.snmp4j.PDU;

/**
 *
 * @author Francois Steyn - Adept Internet (PTY) LTD (francois.s@adept.co.za)
 */
public class BeanPDU {

    private final Map<String, Record> pdus = new HashMap<>();

    protected Map<String, Record> getMap() {
        return pdus;
    }

    public boolean hasRecord(final String reference) {
        return pdus.containsKey(reference);
    }

    public Record getRecord(final String reference) {
        return pdus.get(reference).withLastUsed();
    }

    public void setRecord(final String reference, final PDU pdu) {
        final Record record = new Record(pdu).withLastUsed();
        pdus.put(reference, record);
    }

    public class Record {

        private final PDU pdu;
        private long lastUsed = 0;

        public Record(PDU pdu) {
            this.pdu = pdu;
        }

        public PDU getPdu() {
            return pdu;
        }

        public long getLastUsed() {
            return lastUsed;
        }

        public Record withLastUsed() {
            lastUsed = System.currentTimeMillis();
            return this;
        }

    }

}
