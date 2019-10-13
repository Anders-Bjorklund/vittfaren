package se.hackney.vittfaren.example;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.hackney.vittfaren.api.Store;

public class Cache {
	private static final Logger logger = LoggerFactory.getLogger( Cache.class ); 
	
	public static final Map< String, String > general = new HashMap< String, String >();
	public static final OrderItemStore orderItems = Store.getInstance( OrderItemStore.class );
	
	public static void main( String[] args ) {
		
		Cache.general.put( "key", "value" );
		Cache.general.get( "key" );
		
		OrderItem orderItem = new OrderItem();
		Cache.orderItems.get( "key" );
		pause();
		Cache.orderItems.put( "key", orderItem );
		pause();
		Cache.orderItems.get( "key" );
		pause();
		Cache.orderItems.put( "key", orderItem );
		pause();
		Cache.orderItems.get( "key" );
		pause();
		Cache.orderItems.put( "key", new OrderItem() );
		pause();
		Cache.orderItems.get( "key" );
		pause();
		
	}
	
	private static void pause() {
		
		try {
			Thread.sleep( 50 );
		} catch (InterruptedException e) { }
		
	}

}
