/*
 * Copyright Guillaume Nodet, all rights reserved.
 */
package org.osgi.service.transaction.control.jms;

import org.osgi.service.transaction.control.jpa.JPAEntityManagerProvider;

import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.persistence.EntityManagerFactory;
import java.util.Map;

public interface JMSContextProviderFactory {

    /**
     * Create a private {@link JMSContextProvider} using existing
     * {@link ConnectionFactory} and {@link XAConnectionFactory}.
     *
     * @param cf The {@link ConnectionFactory} to use in the {@link JMSContextProvider} for non xa connections
     * @param xaCf The {@link XAConnectionFactory} to use in the {@link JMSContextProvider} for xa connections
     * @param resourceProviderProperties Configuration properties to pass to the
     *            JMS Resource Provider runtime
     * @return A {@link JMSContextProvider} that can be used in
     *         transactions
     */
    JMSContextProvider getProviderFor(ConnectionFactory cf,
                                      XAConnectionFactory xaCf,
                                      Map<String,Object> resourceProviderProperties);

    /**
     * Release a {@link JMSContextProvider} instance that has been created
     * by this factory. Released instances are eligible to be shut down and have
     * any remaining open connections closed.
     * <p>
     * Note that all {@link JMSContextProvider} instances created by this
     * factory service are implicitly released when the factory service is
     * released by this bundle.
     *
     * @param provider the {@link JMSContextProvider} to release
     * @throws IllegalArgumentException if the supplied resource was not created
     *             by this factory service instance.
     */
    void releaseProvider(JMSContextProvider provider);
}
