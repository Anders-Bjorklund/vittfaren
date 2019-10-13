package se.hackney.vittfaren.internal.todos;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.hackney.vittfaren.api.Store;
import se.hackney.vittfaren.internal.Accessor;

public class Purge extends Todo {
	private static final Logger logger = LoggerFactory.getLogger( Purge.class );
	
	private Store store = null;
	private Accessor accessor = null;
	
	public Purge( long deadline, Store store, Accessor accessor ) {
		this.deadline = deadline;
		this.store = store;
		this.accessor = accessor;
	}
	
	public Purge( long deadline, Purge todo ) {
		this.deadline = deadline;
		this.store = todo.store;
		this.accessor = todo.accessor;
	}

	@Override
	public boolean action() {
		
		synchronized( accessor ) {
			logger.debug( "[ PURGE: {} ]", accessor.getKey() );
			
			long now = new Date().getTime();
			
			if( now > accessor.getScheduledWrite() ) {
				store.remove( accessor.getKey() );
				return true;
			} else {
				logger.debug( "[ ABANDON PURGE: {}, WAITING FOR WRITE ]", accessor.getKey() );
				return false;
			}
			
		}
		
	}

}
