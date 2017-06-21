/*
 * Copyright Guillaume Nodet, all rights reserved.
 */
package org.apache.aries.tx.control.jms.xa;

import org.ops4j.pax.transx.connector.ConnectionManagerFactory;
import org.ops4j.pax.transx.connector.TransactionManager;
import org.ops4j.pax.transx.jms.ManagedConnectionFactoryFactory;
import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.TransactionStatus;
import org.osgi.service.transaction.control.jms.JMSContextProvider;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Session;
import javax.jms.XAConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.Status;
import javax.transaction.xa.XAResource;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntConsumer;

public class JMSContextProviderImpl implements JMSContextProvider {

    private final ConnectionFactory cf;
    private final XAConnectionFactory xaCf;
    private final String name;
    private final Map<String, Object> resourceProviderProperties;

    private ConnectionFactory connectionFactory;
    private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

    public JMSContextProviderImpl(ConnectionFactory cf, XAConnectionFactory xaCf, Map<String, Object> resourceProviderProperties) {
        this.cf = cf;
        this.xaCf = xaCf;
        this.name = "myname";
        this.resourceProviderProperties = resourceProviderProperties;
    }

    @Override
    public JMSContext getResource(TransactionControl txControl) throws TransactionException {
        int mode = acknowledgeMode;
        TransactionContext ctx = txControl.getCurrentContext();
        if (ctx != null) {
            mode = Session.SESSION_TRANSACTED;
        }
        try {
            JMSContext context = getConnectionFactory(txControl).createContext(mode);
            if (ctx != null && !ctx.supportsXA()) {
                ctx.registerLocalResource(new LocalResource() {
                    @Override
                    public void commit() throws TransactionException {
                        context.commit();
                    }
                    @Override
                    public void rollback() throws TransactionException {
                        context.rollback();
                    }
                });
            }
            return context;
        } catch (Exception e) {
            throw new TransactionException("Unable to create connection factory", e);
        }
    }

    private ConnectionFactory getConnectionFactory(TransactionControl txControl) throws Exception {
        ManagedConnectionFactory mcf = ManagedConnectionFactoryFactory.create(cf, xaCf);
        ConnectionManager cm = ConnectionManagerFactory.builder()
                .transaction(ConnectionManagerFactory.TransactionSupportLevel.Xa)
                .transactionManager(() -> new TransactionManager.Transaction() {
                    @Override
                    public boolean isActive() {
                        TransactionContext txContext = txControl.getCurrentContext();
                        return txContext != null && txContext.supportsXA();
                    }
                    @Override
                    public void enlistResource(XAResource xares) throws Exception {
                        getContext().registerXAResource(xares, name);
                    }
                    @Override
                    public void delistResource(XAResource xares, int flags) throws Exception {
                    }
                    @Override
                    public void preCompletion(Runnable cb) {
                        getContext().preCompletion(cb);
                    }
                    @Override
                    public void postCompletion(IntConsumer cb) {
                        getContext().postCompletion(ts -> cb.accept(status(ts)));
                    }
                    private int status(TransactionStatus ts) {
                        switch (ts) {
                            case COMMITTED:
                                return Status.STATUS_COMMITTED;
                            case ROLLED_BACK:
                                return Status.STATUS_ROLLEDBACK;
                        }
                        return Status.STATUS_UNKNOWN;
                    }
                    private TransactionContext getContext() {
                        return Objects.requireNonNull(txControl.getCurrentContext(), "No active transaction context");
                    }
                })
                .managedConnectionFactory(mcf)
                .partition(ConnectionManagerFactory.Partition.ByConnectorProperties)
                .build();
        return (ConnectionFactory) mcf.createConnectionFactory(cm);
    }


}
