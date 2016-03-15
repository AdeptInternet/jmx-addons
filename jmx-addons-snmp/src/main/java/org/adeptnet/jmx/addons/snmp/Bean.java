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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.snmp.SnmpEndpoint;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 *
 * @author Francois Steyn - Adept Internet (PTY) LTD (francois.s@adept.co.za)
 */
public class Bean implements BeanInterface {

    private static final Logger LOG = Logger.getLogger(Bean.class.getName());
    private static final BeanPDU pdus = new BeanPDU();
    final private CamelContext context;
    private final MapThread mapThread;
    private final Thread thread;

    public static class MapThread implements Runnable {

        private boolean running;

        public boolean isRunning() {
            return running;
        }

        public void setRunning(final boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            long current;
            long lastUsed;
            BeanPDU.Record record;
            try {
                while (isRunning()) {
                    synchronized (pdus) {
                        current = System.currentTimeMillis();
                        LOG.log(Level.FINE, "mapThread Running: {0}", pdus.getMap().size());
                        for (Iterator<String> keys = pdus.getMap().keySet().iterator(); keys.hasNext();) {
                            final String key = keys.next();
                            record = pdus.getMap().get(key);
                            lastUsed = current - record.getLastUsed();
                            //LOG.info(String.format("%s: %d", key, lastUsed));
                            if (lastUsed > 30 * 60 * 1000) {
                                LOG.log(Level.INFO, "Removing old Reference: {0}", key);
                                keys.remove();
                            }
                        }
                    }
                    Thread.sleep(60 * 1000);
                }
            } catch (InterruptedException ex) {
                if (isRunning()) {
                    LOG.log(Level.SEVERE, "mapThread", ex);
                }
            }
        }
    }

    public Bean(final CamelContext context) {
        this.context = context;
        mapThread = new MapThread();
        thread = new Thread(mapThread);
        thread.setDaemon(true);
        mapThread.setRunning(true);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        mapThread.setRunning(false);
        thread.interrupt();
    }

    private BeanPDU.Record getBeanPDU(final String reference) {
        synchronized (pdus) {
            if (!pdus.hasRecord(reference)) {
                throw new java.lang.NullPointerException("pdu is NULL, call loadFromURL()");
            }
            return pdus.getRecord(reference);
        }
    }

    private void setBeanPDU(final String reference, final PDU pdu) {
        synchronized (pdus) {
            pdus.setRecord(reference, pdu);
        }
    }

    @Override
    public void loadFromURL(final String reference, final String url) throws IOException {
        loadFromURLInternal(reference, url, false);
    }

    @Override
    public void loadFromURLDebug(final String reference, final String url) throws IOException {
        loadFromURLInternal(reference, url, true);
    }

    public void loadFromURLInternal(final String reference, final String url, final boolean debug) throws IOException {
        final Endpoint _endpoint = context.getEndpoint(url);

        if (!(_endpoint instanceof SnmpEndpoint)) {
            throw new IllegalArgumentException("Unknown SnmpEndpoint: " + _endpoint.getClass());
        }

        final SnmpEndpoint endpoint = (SnmpEndpoint) _endpoint;

        final Address targetAddress = GenericAddress.parse(endpoint.getAddress());

        final TransportMapping<? extends Address> transport;
        if ("tcp".equals(endpoint.getProtocol())) {
            transport = new DefaultTcpTransportMapping();
        } else if ("udp".equals(endpoint.getProtocol())) {
            transport = new DefaultUdpTransportMapping();
        } else {
            throw new IllegalArgumentException("Unknown protocol: " + endpoint.getProtocol());
        }

        try {
            final Snmp snmp = new Snmp(transport);

            final CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(endpoint.getSnmpCommunity()));
            target.setAddress(targetAddress);
            target.setRetries(endpoint.getRetries());
            target.setTimeout(endpoint.getTimeout());
            target.setVersion(endpoint.getSnmpVersion());

            final PDU _pdu = new PDU();
            _pdu.clear();
            _pdu.setType(PDU.GET);

            for (OID oid : endpoint.getOids()) {
                _pdu.add(new VariableBinding(oid));
            }

            transport.listen();
            final ResponseEvent event = snmp.send(_pdu, target, null);
            setBeanPDU(reference, event.getResponse());

            if (debug) {
                LOG.log(Level.INFO, MessageFormat.format("Reference: {0}, URL: {1} ", reference, url));
                LOG.log(Level.INFO, MessageFormat.format("Map: {0}", asMap(reference)));
            }
        } finally {
            transport.close();
        }
    }

    @Override
    public String asString(final String reference, final String oid) {
        return getBeanPDU(reference).getPdu().getVariable(new OID(oid)).toString();
    }

    @Override
    public int asInt(final String reference, final String oid) {
        return getBeanPDU(reference).getPdu().getVariable(new OID(oid)).toInt();
    }

    @Override
    public long asLong(final String reference, final String oid) {
        return getBeanPDU(reference).getPdu().getVariable(new OID(oid)).toLong();
    }

    @Override
    public Map<String, String> asMap(final String reference) {
        final Map<String, String> map = new HashMap<>();
        for (final VariableBinding vb : getBeanPDU(reference).getPdu().toArray()) {
            map.put(vb.getOid().toString(), vb.getVariable().toString());
        }
        return map;
    }

    @Override
    public void backupData(final String fromReference, final String toReference) {
        synchronized (pdus) {
            if (!pdus.hasRecord(fromReference)) {
                return;
            }
            pdus.setRecord(toReference, pdus.getRecord(fromReference));
        }
    }

    @Override
    public long backupCreatedDiff(final String fromReference, final String toReference) {
        synchronized (pdus) {
            if (!pdus.hasRecord(fromReference)) {
                return 0;
            }
            if (!pdus.hasRecord(toReference)) {
                return 0;
            }
            return pdus.getRecord(fromReference).getCreated() - pdus.getRecord(toReference).getCreated();
        }
    }

}
