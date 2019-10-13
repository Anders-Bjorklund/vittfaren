package se.hackney.vittfaren.internal.todos;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.cglib.proxy.MethodProxy;
import se.hackney.vittfaren.internal.Accessor;

public class Write extends Todo {
	private static final Logger logger = LoggerFactory.getLogger( Write.class );
	
	private MethodProxy proxy = null;
	private Object object = null;
	private Object[] args = null;
	private Accessor accessor = null;
	
	public Write( long deadline, MethodProxy proxy, Object object, Object[] args, Accessor accessor ) {
		this.deadline = deadline;
		this.proxy = proxy;
		this.object = object;
		this.args = args;
		this.accessor = accessor;
	}

	@Override
	public boolean action() {
		
		synchronized( accessor ) {
			logger.debug( "[ WRITE: {} ]", accessor.getKey() );
			try {
				proxy.invokeSuper( object, args );
				return true;
				
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
		}
		
	}

}
