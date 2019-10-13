package se.hackney.vittfaren.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import se.hackney.vittfaren.internal.Accessor;
import se.hackney.vittfaren.internal.todos.Purge;
import se.hackney.vittfaren.internal.todos.Todo;
import se.hackney.vittfaren.internal.todos.Write;
import sun.security.x509.FreshestCRLExtension;

public abstract class Store {
	private static final Logger logger = LoggerFactory.getLogger( Store.class );
	
	protected SortedSet< Todo > todos = new ConcurrentSkipListSet< Todo >();
	protected Map< String, Accessor > map = new HashMap< String, Accessor >();
	
	private long KEEP_IN_CACHE = 2000;
	private long MIN_WAIT_BEFORE_WRITE = 2000;
	
	@SuppressWarnings("unchecked")
	public static < T > T getInstance( Class< T > type ) {
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass( type );
		
		enhancer.setCallback( ( MethodInterceptor ) ( obj, method, args, proxy ) -> {
			Object result = null;
			Store store = ( Store ) obj;
			String key = null;
			
			if( method.getName().equals( "hit" ) || method.getName().equals( "get" ) || method.getName().equals( "put" ) ) {
				
				// Perform flushes, generation switches, pre-loading, etc.
				store.manage();
				
				if( method.getName().equals( "hit" ) ) {
					return null;
				} else {
					key = ( String ) args[0];
				}
				
				Accessor< ? > accessor = store.map.get( key );
				
				if( accessor == null ) {
					accessor = new Accessor( key );
					long deadline = new Date().getTime() + store.KEEP_IN_CACHE;
					store.todos.add( new Purge( deadline, store, accessor ) );
					
					synchronized( accessor ) {
						store.map.put(key, accessor );

						if( method.getName().equalsIgnoreCase( "get" ) ) {
							logger.debug( "[ GET ]" );
							result = proxy.invokeSuper(obj, args);
							accessor.set( result );
						}

						if( method.getName().equalsIgnoreCase( "put" ) ) {
							Object freshObject = args[1];
							logger.debug( "[ PUT - KEY = \"{}\", HASH = \"{}\" ]", key, freshObject.hashCode() );
							accessor.set( freshObject );
							
							long now = new Date().getTime();
							long writeDeadline = now + store.MIN_WAIT_BEFORE_WRITE;
							
							store.todos.add( new Write( writeDeadline, proxy, obj, args, accessor ) );
							accessor.setLastWritten( writeDeadline );
						}
					}
					
				} else {
					
					synchronized( accessor ) {

						if( method.getName().equalsIgnoreCase( "get" ) ) {
							logger.debug( "[ GET - CACHED ]" );
							result = accessor.get();
						}

						if( method.getName().equalsIgnoreCase( "put" ) ) {
							Object freshObject = args[1];
							
							if( freshObject.hashCode() != accessor.hashCode() ) {
								logger.debug( "[ PUT - CHANGED OBJECT - KEY = \"{}\", HASH = \"{}\" ]", key, freshObject.hashCode() );
								accessor.set( freshObject );
								
								long now = new Date().getTime();
								
								if( now < accessor.getScheduledWrite() ) {
									// Future write already scheduled - NOOP
									logger.debug( "[ WRITE - NOOP - ALREADY SCHEDULED ]" );
								} else {
									long writeDeadline = now + store.MIN_WAIT_BEFORE_WRITE;
									store.todos.add( new Write( writeDeadline, proxy, obj, args, accessor ) );
									accessor.setLastWritten( writeDeadline );
									logger.debug( "[ WRITE - SCHEDULED ]" );
								}
								
							} else {
								logger.debug( "[ PUT - NOOP - KEY = \"{}\", HASH = \"{}\" ]", key, freshObject.hashCode() );
							}
							
						}
					}
					
				}
				
			} else {
				result = proxy.invokeSuper(obj, args);				
			}

			return result;
		});

		return ( T ) enhancer.create();

	}
	
	public void hit() {}
	
	public void remove( String key ) {
		map.remove( key );
	}
	
	private void manage() {
		
		int todosPerCall = 2;
		int todosDone = 0;
		
//		logger.debug( "[ MANAGE : KEYS = {} ]", map.keySet().size() );
		
		Iterator< Todo > todoIterator = todos.iterator();
		long now = new Date().getTime();
		
		while( todosDone < todosPerCall && todoIterator.hasNext() ) {
			Todo todo = todoIterator.next();
			
			if( now >= todo.getDeadline() ) {
				boolean done = todo.action();
				todoIterator.remove();
				todosDone++;
				
				if( !done && todo instanceof Purge ) {
					long newDeadline = new Date().getTime() + KEEP_IN_CACHE;
					Purge oldPurge = ( Purge ) todo;
					todos.add( new Purge( newDeadline, oldPurge ) );
				}
				
			} else {
				// Since todos are ordered by deadline, if last todo
				// checked was not yet due, nothing is due.
				break;
			}
		}
		
	}
}
