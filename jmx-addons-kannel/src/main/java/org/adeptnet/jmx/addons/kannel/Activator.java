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
package org.adeptnet.jmx.addons.kannel;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Francois Steyn - Adept Internet (PTY) LTD <francois.s@adept.co.za>
 */
public class Activator implements BundleActivator {

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());
    private MBeanServer mbs = null;
    private ObjectName name = null;

    public String getObjectName(final BeanInterface bean) {
        return String.format("%s:type=%s,name=%s", bean.getClass().getPackage().getName(), bean.getClass().getSimpleName(), "bean");
    }

    @Override
    public void start(BundleContext context) throws Exception {
        LOG.log(Level.INFO, "ADEPTNET Starting: {0}", Activator.class);
        mbs = ManagementFactory.getPlatformMBeanServer();
        final Bean bean = new Bean();
        name = new ObjectName(getObjectName(bean));
        final StandardMBean mbean = new StandardMBean(bean, BeanInterface.class, true);
        mbs.registerMBean(mbean, name);
        LOG.log(Level.INFO, "ADEPTNET Started: {0}", Activator.class);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOG.log(Level.INFO, "ADEPTNET Stopping: {0}", Activator.class);
        if (name != null) {
            mbs.unregisterMBean(name);
        }
        LOG.log(Level.INFO, "ADEPTNET Stopped: {0}", Activator.class);
    }

}
