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
package org.adeptnet.jmx.addons.kannel.xml;

import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;

/**
 *
 * @author Francois Steyn - Adept Internet (PTY) LTD <francois.s@adept.co.za>
 */
public class JaxbManager {

    @SuppressWarnings("rawtypes")
    public static JAXBContext getJAXBContext() throws JAXBException {
        final java.util.List<Class<?>> _knownClasses = new java.util.ArrayList<>();
        _knownClasses.add(Gateway.class);
        return JAXBContext.newInstance(_knownClasses.toArray(new Class[_knownClasses.size()]));
    }

    public static Gateway toGateway(final File file) throws JAXBException {
        return (Gateway) getJAXBContext().createUnmarshaller().unmarshal(file);
    }

    public static Gateway toGateway(final Reader r) throws JAXBException {
        return (Gateway) getJAXBContext().createUnmarshaller().unmarshal(r);
    }

    public static String toXML(final Gateway gateway) throws JAXBException {
        final Marshaller marshal = getJAXBContext().createMarshaller();
        final Schema schema = null;
        marshal.setSchema(schema);
        marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshal.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        final StringWriter sw = new StringWriter();
        marshal.marshal(gateway, sw);
        return sw.toString();
    }

}
