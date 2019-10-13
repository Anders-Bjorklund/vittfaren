package se.hackney.vittfaren.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import se.hackney.vittfaren.internal.Accessor;
import se.hackney.vittfaren.internal.todos.CleaningTodo;
import se.hackney.vittfaren.internal.todos.Todo;

public abstract class Store {
	private static final Logger logger = LoggerFactory.getLogger( Store.class );
	
	protected SortedSet< Todo > todos = new TreeSet< Todo >();
	protected Map< String, Accessor > map = new HashMap< String, Accessor >();
	
	private long KEEP_IN_CACHE = 1000;
	private long minWaitBeforeWrite = 0;
	private long maxWaitBetweenRead = Long.MAX_VALUE;
	
	@SuppressWarnings("unchecked")
	public static < T > T getInstance( Class< T > type ) {
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass( type );
		
		enhancer.setCallback( ( MethodInterceptor ) ( obj, method, args, proxy ) -> {
			Object result = null;
			Store store = ( Store ) obj;
			String key = ( String ) args[0];
			
			if( method.getName().equals( "get" ) || method.getName().equals( "put" ) ) {
				
				// Perform flushes, generation switches, pre-loading, etc.
				store.manage();
				
				Accessor< ? > accessor = store.map.get( key );
				
				if( accessor == null ) {
					accessor = new Accessor( key );
					long deadline = new Date().getTime() + store.KEEP_IN_CACHE;
					store.todos.add( new CleaningTodo( deadline, store, accessor ) );
					
					synchronized( accessor ) {
						store.map.put(key, accessor );

						if( method.getName().equalsIgnoreCase( "get" ) ) {
							logger.debug( "[ GET ]" );
							result = proxy.invokeSuper(obj, args);
							accessor.set( result );
						}

						if( method.getName().equalsIgnoreCase( "put" ) ) {
							logger.debug( "[ PUT ]" );
							Object freshObject = args[1];
							accessor.set( freshObject );
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
								logger.debug( "[ PUT - NEW OBJECT ]" );
								accessor.set( freshObject );
							} else {
								logger.debug( "[ PUT - NOOP ]" );
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
	
	public void remove( String key ) {
		map.remove( key );
	}
	
	private void manage() {
		
		int todosPerCall = 2;
		int todosDone = 0;
		
		logger.debug( "[ MANAGE : KEYS = {} ]", map.keySet().size() );
		
		Iterator< Todo > todoIterator = todos.iterator();
		long now = new Date().getTime();
		
		while( todosDone < todosPerCall && todoIterator.hasNext() ) {
			Todo todo = todoIterator.next();
			
			if( now >= todo.getDeadline() ) {
				todo.action();
				todoIterator.remove();
				todosDone++;
				
			} else {
				// Since todos are ordered by deadline, if last todo
				// checked was not yet due, nothing is due.
				break;
			}
		}
		
	}
}
