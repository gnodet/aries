/*
 * Copyright Guillaume Nodet, all rights reserved.
 */
package org.osgi.service.transaction.control.jms;

import org.osgi.service.transaction.control.ResourceProvider;

import javax.jms.JMSContext;

/**
 * A specialised {@link ResourceProvider} suitable for obtaining JMS
 * {@link JMSContext} instances.
 * <p>
 * Instances of this interface may be available in the Service Registry, or can
 * be created using a {@link JMSContextProviderFactory}.
 */
public interface JMSContextProvider extends ResourceProvider<JMSContext> {
    /**
     * This interface specialises the ResourceProvider for creating JMS
     * contexts
     */
}
