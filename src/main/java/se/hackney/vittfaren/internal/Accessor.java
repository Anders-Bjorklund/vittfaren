package se.hackney.vittfaren.internal;

import java.util.Date;

public class Accessor< T > {

	private long lastTouched = 0;
	private long lastWritten = 0;
	private int lastHashCode = 0;
	
	private String key = null;
	private T cachedObject = null;
	
	public Accessor( String key ) {
		this.key = key;
	}

	public T get() {
		return cachedObject;
	}
	
	@SuppressWarnings("unchecked")
	public Accessor< T > set( Object freshObject ) {
		cachedObject = ( T ) freshObject;
		lastTouched = new Date().getTime();
		lastHashCode = freshObject.hashCode();
		
		return this;
	}
	
	public String getKey() {
		return key;
	}
	
	public long getLastTouched() {
		return lastTouched;
	}

	public long getScheduledWrite() {
		return lastWritten;
	}
	
	public void setLastWritten( long time ) {
		this.lastWritten = time;
	}
	
	public int hashCode() {
		return lastHashCode;
	}

}
