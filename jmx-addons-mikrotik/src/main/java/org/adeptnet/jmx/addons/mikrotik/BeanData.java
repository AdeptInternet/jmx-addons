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
package org.adeptnet.jmx.addons.mikrotik;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Francois Steyn - Adept Internet (PTY) LTD (francois.s@adept.co.za)
 */
public class BeanData {

    private final Map<String, Record> datas = new HashMap<>();

    protected Map<String, Record> getMap() {
        return datas;
    }

    public boolean hasRecord(final String reference) {
        return datas.containsKey(reference);
    }

    public Record getRecord(final String reference) {
        return datas.get(reference).withLastUsed();
    }

    public void setRecord(final String reference, final List<Map<String, String>> list) {
        final Record record = new Record(list).withLastUsed();
        datas.put(reference, record);
    }

    public class Record {

        private final List<Map<String, String>> list;
        private long lastUsed = 0;

        public Record(final List<Map<String, String>> list) {
            this.list = list;
        }

        public List<Map<String, String>> getList() {
            return list;
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
