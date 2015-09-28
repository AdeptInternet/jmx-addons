/*
 * Copyright 2015 Francois Steyn - Adept Internet (PTY) LTD (francois.s@adept.co.za).
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
package org.adeptnet.jmx.addons.mikrotik;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

/**
 *
 * @author Francois Steyn - Adept Internet (PTY) LTD (francois.s@adept.co.za)
 */
public class Bean implements BeanInterface {

    private static final Logger LOG = Logger.getLogger(Bean.class.getName());
    private static final BeanData datas = new BeanData();

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
            BeanData.Record record;
            try {
                while (isRunning()) {
                    synchronized (datas) {
                        current = System.currentTimeMillis();
                        LOG.log(Level.FINE, "mapThread Running: {0}", datas.getMap().size());
                        for (Iterator<String> keys = datas.getMap().keySet().iterator(); keys.hasNext();) {
                            final String key = keys.next();
                            record = datas.getMap().get(key);
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

    public Bean() {
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

    private BeanData.Record getRecord(final String reference) {
        synchronized (datas) {
            if (!datas.hasRecord(reference)) {
                throw new java.lang.NullPointerException("record is NULL, call loadFromAPI()");
            }
            return datas.getRecord(reference);
        }

    }

    private void setRecord(final String reference, final List<Map<String, String>> data) {
        synchronized (data) {
            datas.setRecord(reference, data);
        }
    }

    private void loadFromConnection(final String reference, final ApiConnection con, final String user, final String password, final String cmd) throws MikrotikApiException {
        try {
            con.login(user, password);
        } catch (InterruptedException ex) {
            throw new MikrotikApiException("login", ex);
        }

        final List<Map<String, String>> data = con.execute(cmd);
        setRecord(reference, data);
    }

    @Override
    public void loadFromAPI(final String reference, final String host, final String user, final String password, final String cmd) throws IOException {
        loadFromAPI(reference, host, user, password, cmd, ApiConnection.DEFAULT_PORT);
    }

    @Override
    public void loadFromAPI(final String reference, final String host, final String user, final String password, final String cmd, final int port) throws IOException {
        loadFromAPI(reference, host, user, password, cmd, port, ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
    }

    @Override
    public void loadFromAPI(final String reference, final String host, final String user, final String password, final String cmd, final int port, final int timeout) throws IOException {
        try {
            try (final ApiConnection con = ApiConnection.connect(host, port, timeout)) {
                loadFromConnection(reference, con, user, password, cmd);
            }
        } catch (MikrotikApiException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void loadFromAPITLS(final String reference, final String host, final String user, final String password, final String cmd) throws IOException {
        loadFromAPITLS(reference, host, user, password, cmd, ApiConnection.DEFAULT_TLS_PORT);
    }

    @Override
    public void loadFromAPITLS(final String reference, final String host, final String user, final String password, final String cmd, final int port) throws IOException {
        loadFromAPITLS(reference, host, user, password, cmd, port, ApiConnection.DEFAULT_CONNECTION_TIMEOUT);
    }

    @Override
    public void loadFromAPITLS(final String reference, final String host, final String user, final String password, final String cmd, final int port, final int timeout) throws IOException {
        try {
            try (final ApiConnection con = ApiConnection.connectTLS(host, port, timeout)) {
                loadFromConnection(reference, con, user, password, cmd);
            }
        } catch (MikrotikApiException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String asString(final String reference, final String matchName, final String matchValue, final String returnName) {
        final Map<String, String> map = asListEntry(reference, matchName, matchValue);
        if (!map.containsKey(returnName)) {
            throw new java.lang.NullPointerException(String.format("Cannot find %s in map", returnName));
        }
        return map.get(returnName);
    }

    @Override
    public int asInt(final String reference, final String matchName, final String matchValue, final String returnName) {
        return Integer.valueOf(asString(reference, matchName, matchValue, returnName));
    }

    @Override
    public List<Map<String, String>> asList(final String reference) {
        return getRecord(reference).getList();
    }

    @Override
    public Map<String, String> asListEntry(final String reference, final String matchName, final String matchValue) {
        if (matchValue == null) {
            throw new java.lang.NullPointerException("matchValue is NULL");
        }
        final List<Map<String, String>> list = asList(reference);
        for (final Map<String, String> data : list) {
            if (!data.containsKey(matchName)) {
                continue;
            }
            if (!matchValue.equalsIgnoreCase(data.get(matchName))) {
                continue;
            }
            return data;
        }
        throw new java.lang.NullPointerException(String.format("Cannot find %s in list", matchName));
    }

}
