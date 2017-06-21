/*
 * Copyright Guillaume Nodet, all rights reserved.
 */
package org.apache.aries.tx.control.jms.xa;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.aries.tx.control.service.xa.impl.TransactionControlImpl;
import org.apache.geronimo.transaction.manager.RecoveryWorkAroundTransactionManager;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.junit.Test;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jms.JMSContextProvider;

import javax.jms.JMSContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JMSTest {

    @Test
    public void testXA() throws Exception {
        String brokerUrl = "vm://broker?marshal=false&broker.persistent=false";
        RecoveryWorkAroundTransactionManager txmgr = new RecoveryWorkAroundTransactionManager(30, new XidFactoryImpl(), null);

        Map<String, Object> config = new HashMap<>();
        TransactionControl ctrl = new TransactionControlImpl(null, config);

        JMSContextProvider provider = new JMSContextProviderFactoryImpl().getProviderFor(
                new ActiveMQConnectionFactory(brokerUrl),
                new ActiveMQXAConnectionFactory(brokerUrl),
                new HashMap<>()
        );

        ctrl.required(() -> {
            try (JMSContext ctx = provider.getResource(ctrl)) {
                ctx.createProducer().send(ctx.createQueue("myqueue"), "mymessage");
            }
            ctrl.getCurrentContext().setRollbackOnly();
            return null;
        });

        try (JMSContext ctx = provider.getResource(ctrl)) {
            String txt = ctx.createConsumer(ctx.createQueue("myqueue")).receiveBody(String.class, 100);
            assertNull(txt);
        }

        ctrl.required(() -> {
            try (JMSContext ctx = provider.getResource(ctrl)) {
                ctx.createProducer().send(ctx.createQueue("myqueue"), "mymessage");
            }
            return null;
        });

        try (JMSContext ctx = provider.getResource(ctrl)) {
            String txt = ctx.createConsumer(ctx.createQueue("myqueue")).receiveBody(String.class, 100);
            assertNotNull(txt);
        }

    }
}
