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
package org.adeptnet.jmx.addons.kannel;

import org.adeptnet.jmx.addons.kannel.xml.Gateway;
import org.adeptnet.jmx.addons.kannel.xml.JaxbManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Francois Steyn - Adept Internet (PTY) LTD (francois.s@adept.co.za)
 */
public class Bean implements BeanInterface {

    private Gateway gateway = null;

    public Bean() {
    }

    @Override
    public void loadFromURL(final String url) throws JAXBException, MalformedURLException, IOException {
        try (InputStream in = new URL(url).openStream()) {
            final String data = IOUtils.toString(in).replace("<gateway>", "<gateway xmlns=\"urn:ietf:params:xml:ns:kannel-1.0\">");
            gateway = JaxbManager.toGateway(new InputStreamReader(IOUtils.toInputStream(data)));
        }
    }

    @Override
    public Gateway getGateway() {
        return gateway;
    }
}
