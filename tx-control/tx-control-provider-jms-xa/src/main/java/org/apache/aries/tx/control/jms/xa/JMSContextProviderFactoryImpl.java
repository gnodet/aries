/*
 * Copyright Guillaume Nodet, all rights reserved.
 */
package org.apache.aries.tx.control.jms.xa;

import org.ops4j.pax.transx.connector.ConnectionManagerFactory;
import org.ops4j.pax.transx.connector.TransactionManager;
import org.ops4j.pax.transx.jms.ManagedConnectionFactoryFactory;
import org.osgi.framework.ServiceException;
import org.osgi.service.transaction.control.jms.JMSContextProvider;
import org.osgi.service.transaction.control.jms.JMSContextProviderFactory;

import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import java.util.Map;

public class JMSContextProviderFactoryImpl implements JMSContextProviderFactory {

    @Override
    public JMSContextProvider getProviderFor(ConnectionFactory cf, XAConnectionFactory xaCf, Map<String, Object> resourceProviderProperties) {
        try {
            return new JMSContextProviderImpl(cf, xaCf, resourceProviderProperties);
        } catch (Exception e) {
            throw new ServiceException("A failure occurred obtaining the resource provider", e);
        }
    }

    @Override
    public void releaseProvider(JMSContextProvider provider) {
    }

}
