package se.hackney.vittfaren.example;

import java.util.HashMap;
import java.util.Map;

import se.hackney.vittfaren.api.Store;

public class Cache {
	
	public static final Map< String, String > general = new HashMap< String, String >();
	public static final OrderItemStore orderItems = Store.getInstance( OrderItemStore.class );
	
	public static void main( String[] args ) {
		
		Cache.general.put( "key", "value" );
		Cache.general.get( "key" );
		
		OrderItem orderItem = new OrderItem();
		Cache.orderItems.get( "key" );
		Cache.orderItems.put( "key", orderItem );
		Cache.orderItems.get( "key" );
		Cache.orderItems.put( "key", orderItem );
		Cache.orderItems.put( "key", new OrderItem() );
		
//		Cache.orderItems.save().empty();
		
	}

}
