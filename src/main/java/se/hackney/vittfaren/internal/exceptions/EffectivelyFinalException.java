package se.hackney.vittfaren.internal.exceptions;

public class EffectivelyFinalException extends RuntimeException {
	private static final long serialVersionUID = 4048570881340102980L;

	public EffectivelyFinalException( String description ) {
		super( description );
	}

}
