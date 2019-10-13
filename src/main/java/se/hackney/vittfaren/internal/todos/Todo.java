package se.hackney.vittfaren.internal.todos;

public abstract class Todo implements Comparable< Object > {
	long deadline = Long.MAX_VALUE;
	
	public abstract void action();

	@Override
	public int compareTo( Object arg ) {

		if( arg == null ) {
			throw new NullPointerException();
		}
		
		if( arg instanceof Todo ) {
			Todo stranger = ( Todo ) arg;
			
			if( this.deadline < stranger.deadline ) {
				return -1;
			}
			
			if( this.deadline > stranger.deadline ) {
				return +1;
			}
			
			return 0;
		}
		
		throw new ClassCastException();
	}
	
	public long getDeadline() {
		return deadline;
	}

}
