package org.apache.aries.tx.control.jpa.xa.impl;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.withSettings;
import static org.osgi.service.transaction.control.TransactionStatus.ACTIVE;
import static org.osgi.service.transaction.control.TransactionStatus.NO_TRANSACTION;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.apache.aries.tx.control.jdbc.xa.connection.impl.XAConnectionWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;

@RunWith(MockitoJUnitRunner.class)
public class XATxContextBindingEntityManagerTest {

	@Mock
	TransactionControl control;
	
	@Mock
	TransactionContext context;
	
	@Mock
	EntityManagerFactory emf;
	
	@Mock
	EntityManager rawEm;

	@Mock
	XAResource xaResource;

	Map<Object, Object> variables = new HashMap<>();
	
	UUID id = UUID.randomUUID();
	
	XATxContextBindingEntityManager em;
	
	@Before
	public void setUp() throws SQLException {
		Mockito.when(emf.createEntityManager()).thenReturn(rawEm).thenReturn(null);
		
		Mockito.doAnswer(i -> variables.put(i.getArguments()[0], i.getArguments()[1]))
			.when(context).putScopedValue(Mockito.any(), Mockito.any());
		Mockito.when(context.getScopedValue(Mockito.any()))
			.thenAnswer(i -> variables.get(i.getArguments()[0]));
		
		em = new XATxContextBindingEntityManager(control, emf, id);
	}
	
	private void setupNoTransaction() {
		Mockito.when(control.getCurrentContext()).thenReturn(context);
		Mockito.when(context.getTransactionStatus()).thenReturn(NO_TRANSACTION);
	}

	private void setupActiveTransaction() {
		Mockito.when(control.getCurrentContext()).thenReturn(context);
		Mockito.when(context.supportsXA()).thenReturn(true);
		Mockito.when(context.getTransactionStatus()).thenReturn(ACTIVE);
	}
	
	
	@Test(expected=TransactionException.class)
	public void testUnscoped() throws SQLException {
		em.isOpen();
	}

	@Test
	public void testNoTransaction() throws SQLException {
		setupNoTransaction();
		
		em.isOpen();
		em.isOpen();
		
		Mockito.verify(rawEm, times(2)).isOpen();
		Mockito.verify(rawEm, times(0)).getTransaction();
		Mockito.verify(context, times(0)).registerXAResource(Mockito.any());
		
		Mockito.verify(context).postCompletion(Mockito.any());
	}

	@Test
	public void testActiveTransactionStraightXAConnection() throws SQLException {
		
		Connection con = Mockito.mock(Connection.class, withSettings().extraInterfaces(XAConnection.class));
		Mockito.when(((XAConnection)con).getXAResource()).thenReturn(xaResource);
		
		Mockito.when(rawEm.unwrap(Connection.class)).thenReturn(con);
		
		setupActiveTransaction();
		
		em.isOpen();
		em.isOpen();
		
		Mockito.verify(rawEm, times(2)).isOpen();
		Mockito.verify(rawEm).joinTransaction();
		
		Mockito.verify(context).postCompletion(Mockito.any());
	}

	@Test
	public void testActiveTransactionWrappedXAConnection() throws SQLException {
		
		XAConnection con = Mockito.mock(XAConnection.class);
		Connection raw = Mockito.mock(Connection.class);
		Mockito.when(con.getXAResource()).thenReturn(xaResource);
		Mockito.when(con.getConnection()).thenReturn(raw);
		
		XAConnectionWrapper value = new XAConnectionWrapper(con);
		
		Mockito.when(rawEm.unwrap(Connection.class)).thenReturn(value);
		
		setupActiveTransaction();
		
		em.isOpen();
		em.isOpen();
		
		Mockito.verify(rawEm, times(2)).isOpen();
		Mockito.verify(rawEm).joinTransaction();
		
		Mockito.verify(context).postCompletion(Mockito.any());
	}

	@Test
	public void testActiveTransactionUnwrappableXAConnection() throws SQLException {
		
		XAConnection xaCon = Mockito.mock(XAConnection.class);
		Mockito.when(xaCon.getXAResource()).thenReturn(xaResource);
		Connection con = Mockito.mock(Connection.class);
		Mockito.when(con.unwrap(XAConnection.class)).thenReturn(xaCon);
		Mockito.when(con.isWrapperFor(XAConnection.class)).thenReturn(true);
		
		Mockito.when(rawEm.unwrap(Connection.class)).thenReturn(con);
		
		setupActiveTransaction();
		
		em.isOpen();
		em.isOpen();
		
		Mockito.verify(rawEm, times(2)).isOpen();
		Mockito.verify(rawEm).joinTransaction();
		
		Mockito.verify(context).postCompletion(Mockito.any());
	}

	@Test
	public void testActiveTransactionUnwrappableXAConnectionWrapper() throws SQLException {
		
		XAConnection xaCon = Mockito.mock(XAConnection.class);
		Mockito.when(xaCon.getXAResource()).thenReturn(xaResource);
		Connection con = Mockito.mock(Connection.class);
		XAConnectionWrapper toReturn = new XAConnectionWrapper(xaCon);
		Mockito.when(con.unwrap(XAConnectionWrapper.class)).thenReturn(toReturn);
		Mockito.when(con.isWrapperFor(XAConnectionWrapper.class)).thenReturn(true);
		
		Mockito.when(rawEm.unwrap(Connection.class)).thenReturn(con);
		
		setupActiveTransaction();
		
		em.isOpen();
		em.isOpen();
		
		Mockito.verify(rawEm, times(2)).isOpen();
		Mockito.verify(rawEm).joinTransaction();
		
		Mockito.verify(context).postCompletion(Mockito.any());
	}

	@Test(expected=TransactionException.class)
	public void testActiveTransactionNoXA() throws SQLException {
		setupActiveTransaction();
		
		Mockito.when(context.supportsXA()).thenReturn(false);
		em.isOpen();
	}

}
