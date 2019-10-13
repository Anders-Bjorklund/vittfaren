package se.hackney.vittfaren.internal;

import java.util.Date;

public class Accessor< T > {

	private long lastRead = 0;
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
	
	public Accessor set( Object freshObject ) {
		cachedObject = ( T ) freshObject;
		lastRead = new Date().getTime();
		lastHashCode = freshObject.hashCode();
		
		return this;
	}
	
	public String getKey() {
		return key;
	}
	
	public long getLastRead() {
		return lastRead;
	}

	public long getLastWritten() {
		return lastWritten;
	}
	
	public int hashCode() {
		return lastHashCode;
	}

}
