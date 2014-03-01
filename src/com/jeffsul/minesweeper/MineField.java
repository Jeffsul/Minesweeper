package com.jeffsul.minesweeper;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class MineField
{		
	private int height;
	private int width;
	private MineButton[][] btns;
	
	public MineField() { }
	
	public void init(MouseListener listener, JPanel pnl, int w, int h)
	{
		height = h;
		width = w;
		btns = new MineButton[width][height];
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				btns[x][y] = new MineButton(x, y);
				btns[x][y].addMouseListener(listener);
				pnl.add(btns[x][y]);
			}
		}
	}
	
	public void generate(int numMines, JButton button)
	{	
		MineButton startBtn = (MineButton) button;
		ArrayList<MineButton> mineFieldList = new ArrayList<MineButton>();
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if (x != startBtn.x && y != startBtn.y)
					mineFieldList.add(btns[x][y]);
		
		for (int i = 0; i < numMines; i++)
			mineFieldList.remove((int) (Math.random() * mineFieldList.size())).setMine();
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (!btns[x][y].isMine())
				{
					int count = 0;
					for (int dx = -1; dx <= 1 && x + dx < width; dx++)
						for (int dy = -1; dy <= 1 && y + dy < height; dy++)
							if (y + dy >= 0 && x + dx >= 0 && btns[x + dx][y + dy].isMine())
								count++;
					btns[x][y].setValue(count);
				}
			}
		}
	}
	
	public boolean hitBlock(JButton button)
	{
		MineButton clickBtn = (MineButton) button;
		if (btns[clickBtn.x][clickBtn.y].isMine())
			return false;
		
		LinkedList<MineButton> blocks = new LinkedList<MineButton>();
		blocks.add(btns[clickBtn.x][clickBtn.y]);
		
		MineButton btn;
		while (blocks.size() > 0)
		{	
			btn = blocks.pop();
			btn.setChecked();
			btn.display();
			int x = btn.x;
			int y = btn.y;
			
			if (btns[x][y].value == 0)
			{
				if (y + 1 < height)
				{
					MineButton sBtn = btns[x][y + 1];
					MineButton swBtn = btns[x - 1][y + 1];
					MineButton seBtn = btns[x + 1][y + 1];
					if (!sBtn.isMine() && !sBtn.isChecked())
					{
						blocks.add(sBtn);
						sBtn.setChecked();
					}
					if (x - 1 >= 0)
					{
						if (!swBtn.isMine() && !swBtn.isChecked())
						{
							blocks.add(swBtn);
							swBtn.setChecked();
						}
					}
					if (x + 1 < width)
					{
						if (!seBtn.isMine() && !seBtn.isChecked())
						{
							blocks.add(seBtn);
							seBtn.setChecked();
						}
					}
				}
				if (y - 1 >= 0)
				{
					MineButton nBtn = btns[x][y - 1];
					MineButton nwBtn = btns[x - 1][y - 1];
					MineButton neBtn = btns[x + 1][y - 1];
					if (!nBtn.isMine() && !nBtn.isChecked())
					{
						blocks.add(nBtn);
						nBtn.setChecked();
					}
					if (x - 1 >= 0)
					{
						if (!nwBtn.isMine() && !nwBtn.isChecked())
						{
							blocks.add(nwBtn);
							nwBtn.setChecked();
						}
					}
					if (x + 1 < width)
					{
						if (!neBtn.isMine() && !neBtn.isChecked())
						{
							blocks.add(neBtn);
							neBtn.setChecked();
						}
					}
				}
				if (x + 1 < width)
				{
					MineButton eBtn = btns[x + 1][y];
					if (!eBtn.isMine() && !eBtn.isChecked())
					{
						blocks.add(eBtn);
						eBtn.setChecked();
					}
				}
				if (x - 1 >= 0)
				{
					MineButton wBtn = btns[x - 1][y];
					if (!wBtn.isMine() && !wBtn.isChecked())
					{
						blocks.add(wBtn);
						wBtn.setChecked();
					}
				}
			}
		}
		return true;
	}
	
	public void flagButton(JButton btn)
	{
		if (btn instanceof MineButton)
		{
			MineButton mineBtn = (MineButton) btn;
			if (!mineBtn.isChecked())
				mineBtn.toggleFlag();
		}
	}
	
	public boolean checkWin()
	{
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if (!btns[x][y].isMine() && !btns[x][y].isChecked())
					return false;
		return true;
	}	
	
	@SuppressWarnings("serial")
	private static class MineButton extends JButton
	{	
		private static final String MINE_TEXT = "M";
		
		private static final Color[] COLOURS = {Color.BLACK, Color.BLUE, Color.getHSBColor(0.333f, 0.777f, 0.439f), Color.RED, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.BLACK};
		
		private static final Font BUTTON_FONT = new Font("Monospaced", Font.BOLD, 16);
		private static final Font BUTTON_FLAG_FONT = new Font("Monospaced", Font.BOLD, 14);
		
		private static final LineBorder BUTTON_CHECKED_BORDER = new LineBorder(Color.LIGHT_GRAY, 1, true);
		private static final Color BUTTON_CHECKED_BG = Color.WHITE;
		
		private boolean checked;
		public boolean flagged;
		private int value;
		private boolean isMine;
		public int x;
		public int y;
		
		public MineButton(int x, int y)
		{
			super("");
			setFocusable(false);
			setFocusPainted(false);
			this.x = x;
			this.y = y;
		}
		
		public boolean isMine()
		{
			return isMine;
		}
		
		public void setChecked()
		{
			checked = true;
		}
		
		public void display()
		{
			setFont(BUTTON_FONT);
			setBackground(BUTTON_CHECKED_BG);
			setForeground(COLOURS[value]);
			setBorder(BUTTON_CHECKED_BORDER);
			setRolloverEnabled(false);
			removeMouseListener(getMouseListeners()[0]);
			
			if (value != 0)
				setText(Integer.toString(value));
		}
		
		public void toggleFlag()
		{
			flagged = !flagged;
			if (flagged)
			{
				setText(MINE_TEXT);
				setFont(BUTTON_FLAG_FONT);
			}
			else
			{
				setText("");
				setFont(BUTTON_FONT);
			}
		}
		
		public void setValue(int value)
		{
			this.value = value;
		}
		
		public void setMine()
		{
			isMine = true;
		}
		
		public boolean isChecked()
		{
			return checked;
		}
	}
}
