package se.hackney.vittfaren.api;

import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import se.hackney.vittfaren.internal.Accessor;

public abstract class Store {
	
	protected Map< String, Accessor > map = new HashMap< String, Accessor >();
	
	private long minWaitBeforeWrite = 0;
	private long maxWaitBetweenRead = Long.MAX_VALUE;
	
	@SuppressWarnings("unchecked")
	public static < T > T getInstance( Class< T > type ) {
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass( type );
		
		enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
			Object result = null;
			
			if( method.getName().equalsIgnoreCase( "get" ) || method.getName().equalsIgnoreCase( "put" ) ) {
				Store store = ( Store ) obj;
				String key = ( String ) args[0];
				Accessor< ? > accessor = store.map.get( key );
				
				if( accessor == null ) {
					accessor = new Accessor();
					
					synchronized( accessor ) {
						store.map.put(key, accessor );

						if( method.getName().equalsIgnoreCase( "get" ) ) {
							System.out.println( "[ GET ]" );
							result = proxy.invokeSuper(obj, args);
							accessor.set( result );
						}

						if( method.getName().equalsIgnoreCase( "put" ) ) {
							System.out.println( "[ PUT ]" );
							Object freshObject = args[1];
							accessor.set( freshObject );
						}
					}
					
				} else {
					
					synchronized( accessor ) {

						if( method.getName().equalsIgnoreCase( "get" ) ) {
							System.out.println( "[ GET - cache ]" );
							result = accessor.get();
						}

						if( method.getName().equalsIgnoreCase( "put" ) ) {
							Object freshObject = args[1];
							
							if( freshObject.hashCode() != accessor.hashCode() ) {
								System.out.println( "[ PUT - nytt objekt ]" );
								accessor.set( freshObject );
							} else {
								System.out.println( "[ PUT - NOOP ]" );
							}
							
						}
					}
					
				}
				
			}

			return result;
		});

		return ( T ) enhancer.create();

	}
	
	public Store save() {
		return this;
	}
	
	public Store empty() {
		return this;
	}
	
}
