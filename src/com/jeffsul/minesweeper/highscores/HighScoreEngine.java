package com.jeffsul.minesweeper.highscores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class HighScoreEngine 
{	
	public static enum SortDirection {ASC, DESC};
	public static enum Type {INTEGER, STRING, CHAR, DOUBLE};
	
	private File highScoreFile;
	private Type[] types;
	
	public HighScoreEngine(String file, Type[] types)
	{
		highScoreFile = new File(file);
		this.types = types;
	}
	
	public void setFile(String file)
	{
		highScoreFile = new File(file);
	}
	
	public void setTypes(Type[] types)
	{
		this.types = types;
	}
	
	public Object[][] load()
	{
		ArrayList<Object[]> resultSet = new ArrayList<Object[]>();
		BufferedReader reader;
		try 
		{
			reader = new BufferedReader(new FileReader(highScoreFile));
			String line;
			while ((line = reader.readLine()) != null)
				resultSet.add(new Object[] { line });
			reader.close();
		} catch (FileNotFoundException exception) { }
		catch (IOException exception) { }
		
		Object[][] result = new Object[resultSet.size()][];
		resultSet.toArray(result);
		return result;
	}
	
	public Object[][] load(String delim)
	{
		return load(delim, Integer.MAX_VALUE);
	}
	
	public Object[][] load(String delim, int max)
	{
		ArrayList<Object[]> resultSet = new ArrayList<Object[]>();
		BufferedReader reader;
		try 
		{
			reader = new BufferedReader(new FileReader(highScoreFile));
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null && i < max)
			{
				Object[] vals = parse(line.split(delim));
				if (vals != null)
					resultSet.add(vals);
				i++;
			}
			reader.close();
		} catch (FileNotFoundException exception) { }
		catch (IOException exception) { }
		
		Object[][] result = new Object[Math.min(max, resultSet.size())][];
		resultSet.toArray(result);
		return result;
	}
	
	public Object[][] load(String delim, int max, int sortField)
	{
		return load(delim, max, sortField, SortDirection.ASC);
	}
	
	public Object[][] load(String delim, int max, int sortField, SortDirection dir)
	{
		return load(delim, max, sortField, dir, new HighScoreFilter[] { });
	}
	
	public Object[][] load(String delim, int max, int sortField, SortDirection dir, HighScoreFilter[] filters)
	{
		ArrayList<Object[]> resultSet = new ArrayList<Object[]>();
		BufferedReader reader;
		try 
		{
			reader = new BufferedReader(new FileReader(highScoreFile));
			String line;
			while ((line = reader.readLine()) != null)
			{
				Object[] lineVal = parse(line.split(delim));
				boolean skip = (lineVal == null);
				for (int i = 0; i < filters.length && !skip; i++)
					if (!filters[i].match(lineVal))
						skip = true;
				
				if (!skip)
				{
					Object value = lineVal[sortField];
					boolean added = false;
					for (int i = 0; i < resultSet.size() && !added; i++)
					{
						if (compare(value, resultSet.get(i)[sortField]) && dir == SortDirection.DESC)
						{
							resultSet.add(i, lineVal);
							added = true;
						}
					}
					if (!added)
						resultSet.add(lineVal);
				}
			}
			reader.close();
		} catch (FileNotFoundException exception) { }
		catch (IOException exception) { }
		
		int size = Math.min(max, resultSet.size());
		Object[][] result = new Object[size][];
		resultSet.subList(0, size).toArray(result);
		return result;
	}
	
	public boolean add(Object[] values, String delim)
	{
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(highScoreFile, true));
			if (highScoreFile.length() != 0L)
				writer.write('\n');
			for (int i = 0; i < values.length - 1; i++)
				writer.write(values[i].toString() + delim);
			writer.write(values[values.length - 1].toString());
			writer.close();
		} catch (FileNotFoundException exception) { return false; }
		catch (IOException exception) { return false; }
		
		return true;
	}
	
	private Object[] parse(String[] vals)
	{
		if (vals.length != types.length)
			return null;
		Object[] result = new Object[vals.length];
		for (int i = 0; i < vals.length; i++)
		{
			switch (types[i])
			{
				case INTEGER:
					result[i] = new Integer(vals[i]);
					break;
				case STRING:
					result[i] = vals[i];
					break;
				case CHAR:
					result[i] = new Character(vals[i].charAt(0));
					break;
			}
		}
		return result;
	}
	
	private boolean compare(Object val1, Object val2)
	{
		if (val1 instanceof String && val2 instanceof String)
			return ((String) val1).compareToIgnoreCase((String) val2) <= 0;
		else if (val1 instanceof Integer && val2 instanceof Integer)
			return ((Integer) val1) > ((Integer) val2);
		else if (val1 instanceof Character && val2 instanceof Character)
			return ((Character) val1) > ((Character) val2);
		else
			return false;
	}
}
