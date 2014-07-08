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

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());
    private PDU pdu;
    final private CamelContext context;

    public Bean(final CamelContext context) {
        this.context = context;
    }

    @Override
    public void loadFromURL(final String url) throws IOException {
        loadFromURLInternal(url, false);
    }

    @Override
    public void loadFromURLDebug(final String url) throws IOException {
        loadFromURLInternal(url, true);
    }

    public void loadFromURLInternal(final String url, final boolean debug) throws IOException {
        pdu = null;
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
            pdu = event.getResponse();
            if (debug) {
                LOG.log(Level.INFO, MessageFormat.format("URL: {0}", url));
                LOG.log(Level.INFO, MessageFormat.format("Map: {1}", asMap()));
            }
        } finally {
            transport.close();
        }
    }

    @Override
    public String asString(final String oid) {
        if (pdu == null) {
            throw new java.lang.NullPointerException("pdu is NULL, call loadFromURL()");
        }
        return pdu.getVariable(new OID(oid)).toString();
    }

    @Override
    public int asInt(final String oid) {
        if (pdu == null) {
            throw new java.lang.NullPointerException("pdu is NULL, call loadFromURL()");
        }
        return pdu.getVariable(new OID(oid)).toInt();
    }

    @Override
    public long asLong(final String oid) {
        if (pdu == null) {
            throw new java.lang.NullPointerException("pdu is NULL, call loadFromURL()");
        }
        return pdu.getVariable(new OID(oid)).toLong();
    }

    @Override
    public Map<String, String> asMap() {
        if (pdu == null) {
            throw new java.lang.NullPointerException("pdu is NULL, call loadFromURL()");
        }
        final Map<String, String> map = new HashMap<>();
        for (final VariableBinding vb : pdu.toArray()) {
            map.put(vb.getOid().toString(), vb.getVariable().toString());
        }
        return map;
    }

}
