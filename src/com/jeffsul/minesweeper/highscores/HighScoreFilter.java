package com.jeffsul.minesweeper.highscores;

public class HighScoreFilter 
{
	public static enum Filter {EQUAL, NOT_EQUAL};
	
	private int field;
	private Object value;
	private Filter criterion;
	
	public HighScoreFilter(int field, Object val, Filter crit)
	{
		this.field = field;
		value = val;
		criterion = crit;
	}
	
	public boolean match(Object[] vals)
	{
		switch (criterion)
		{
		case EQUAL:
			return isEqual(vals[field], value);
		case NOT_EQUAL:
			return !isEqual(vals[field], value);
		default:
			return false;
		}
	}
	
	private boolean isEqual(Object val1, Object val2)
	{
		if (val1 instanceof String && val2 instanceof String)
			return ((String) val1).equals((String) val2);
		else if (val1 instanceof Integer && val2 instanceof Integer)
			return ((Integer) val1).equals((Integer) val2);
		else
			return val1.equals(val2);
	}
}
