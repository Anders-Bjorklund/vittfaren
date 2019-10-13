package se.hackney.vittfaren.internal.todos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.hackney.vittfaren.api.Store;
import se.hackney.vittfaren.internal.Accessor;

public class CleaningTodo extends Todo {
	private static final Logger logger = LoggerFactory.getLogger( CleaningTodo.class );
	
	private Store store = null;
	private Accessor accessor = null;
	
	public CleaningTodo( long deadline, Store store, Accessor accessor ) {
		this.deadline = deadline;
		this.store = store;
		this.accessor = accessor;
	}

	@Override
	public void action() {
		
		synchronized( accessor ) {
			logger.debug( "[ CLEANING: {} ]", accessor.getKey() );
			store.remove( accessor.getKey() );
		}
		
	}

}
