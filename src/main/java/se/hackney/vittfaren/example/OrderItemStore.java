package se.hackney.vittfaren.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.hackney.vittfaren.api.Store;

public class OrderItemStore extends Store {
	private static final Logger logger = LoggerFactory.getLogger( OrderItemStore.class );
	
	public OrderItem get( String key ) {
		logger.debug( "[ READING FROM DISC ]" );
		return new OrderItem();
	}
	
	public void put( String key, OrderItem item ) {
		logger.debug( "[ SAVING TO DISC - KEY = \"{}\". HASH = \"{}\" ]", key, item.hashCode() );
	}
	
}
