package se.hackney.vittfaren.example;

import se.hackney.vittfaren.api.Store;

public class OrderItemStore extends Store {
	
	public OrderItem get( String key ) {
		System.out.println( "[ Läser från disk ]" );
		return new OrderItem();
	}
	
	public void put( String key, OrderItem item ) {
		System.out.println( "[ Sparar till disk ]" );
	}
	
}
