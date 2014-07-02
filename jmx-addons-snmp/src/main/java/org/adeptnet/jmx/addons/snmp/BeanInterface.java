/*
 * Copyright 2014 Francois Steyn - Adept Internet (PTY) LTD <francois.s@adept.co.za>.
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
import java.util.Map;

/**
 *
 * @author Francois Steyn - Adept Internet (PTY) LTD <francois.s@adept.co.za>
 */
public interface BeanInterface {

    public void loadFromURL(final String url) throws IOException;

    public String asString(final String oid);

    public int asInt(final String oid);

    public long asLong(final String oid);

    public Map<String, String> asMap();

}
