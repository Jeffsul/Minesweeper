package com.jeffsul.minesweeper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeffsul.minesweeper.highscores.HighScoreEngine;
import com.jeffsul.minesweeper.highscores.HighScoreFilter;

@SuppressWarnings("serial")
public class JMineSweeper extends JFrame implements MouseListener {	
	private static final int WINDOW_WIDTH = 900;
	private static final int WINDOW_HEIGHT = 700;
	
	private static final int HIGH_SCORE_TAB_INDEX = 1;
	private static final int PLAY_GAME_TAB_INDEX = 2;
	
	private static final int[] HEIGHTS = {10, 12, 15, 20, 20};
	private static final int[] WIDTHS = {10, 12, 15, 20, 20};
	private static final int[] MINES = {15, 24, 40, 80, 200};
	
	private static final int EASY_LEVEL = 0;
	private static final int MEDIUM_LEVEL = 1;
	private static final int HARD_LEVEL = 2;
	private static final int EXTREME_LEVEL = 3;
	private static final int INSANE_LEVEL = 4;
	
	private static final String HIGH_SCORE_FILE = "./src/minesweeper/hiscores.txt";
	
	private static final Font RADIO_BUTTON_FONT = new Font("Monospaced", Font.PLAIN, 18);
	private static final Font HIGH_SCORE_TABLE_FONT = new Font("Monospaced", Font.PLAIN, 18);
			
	private int level;
	private int mines;
	private int width;
	private int height;
		
	private MineField mineField;
	
	private JPanel mineFieldPnl;
	private JTabbedPane tabs;
	
	private Timer timer;
	private int time;
	
	private boolean firstClick = true;
	private boolean gameOver;
	
	private HighScoreEngine highScoreEngine;
	
	public JMineSweeper() {	
		super("JMineSweeper by Jeff Sullivan");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		
		populateScreen();
		
		mineField = new MineField();
		
		highScoreEngine = new HighScoreEngine(HIGH_SCORE_FILE, new HighScoreEngine.Type[] {
		    HighScoreEngine.Type.INTEGER,
		    HighScoreEngine.Type.STRING,
		    HighScoreEngine.Type.INTEGER});
		
		initGame();
				
		pack();
		setVisible(true);
	}
	
	private void populateScreen() {
		JPanel titlePanel = new JPanel();
		JLabel titleLbl = new JLabel("JMineSweeper by Jeff Sullivan");
		titleLbl.setFont(new Font("Monospaced", Font.BOLD, 24));
		titlePanel.add(titleLbl);
		add(titlePanel, BorderLayout.PAGE_START);
		
		JButton newGameBtn = new JButton("New Game");
		newGameBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initGame();
			}
		});
		
		JPanel timePanel = new JPanel();
		final JLabel timeLbl = new JLabel("0");
		timePanel.add(newGameBtn);
		timePanel.add(new JLabel("Time: "));
		timePanel.add(timeLbl);
		timePanel.add(new JLabel(" seconds"));
		add(timePanel, BorderLayout.PAGE_END);
		
		timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				time++;
				timeLbl.setText(Integer.toString(time));
			}
		});
		
		JPanel optionPane = new JPanel();
		optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.Y_AXIS));
		ButtonGroup group = new ButtonGroup();
		final JRadioButton easyRadioBtn = createRadioButton("Easy", group, optionPane);
		easyRadioBtn.setSelected(true);
		final JRadioButton mediumRadioBtn = createRadioButton("Medium", group, optionPane);
		final JRadioButton hardRadioBtn = createRadioButton("Hard", group, optionPane);
		final JRadioButton extremeRadioBtn = createRadioButton("Extreme", group, optionPane);
		final JRadioButton insaneRadioBtn = createRadioButton("Insane", group, optionPane);
		
		JButton setOptionButton = new JButton("Set Options");
		setOptionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (easyRadioBtn.isSelected()) {
					level = EASY_LEVEL;
				} else if (mediumRadioBtn.isSelected()) {
					level = MEDIUM_LEVEL;
				} else if (hardRadioBtn.isSelected()) {
					level = HARD_LEVEL;
				} else if (extremeRadioBtn.isSelected()) {
					level = EXTREME_LEVEL;
				} else if (insaneRadioBtn.isSelected()) {
					level = INSANE_LEVEL;
				}
				initGame();
			}
		});
		optionPane.add(setOptionButton);
		
		final JPanel highScorePnl = new JPanel(new BorderLayout());
		JPanel highScorePnlOptions = new JPanel();
		highScorePnlOptions.add(new JLabel("Select a Level:"));
		final JComboBox<String> selectLevelCombo = new JComboBox<String>
		    (new String[] {"Easy", "Medium", "Hard", "Extreme", "Insane"});
		selectLevelCombo.addItemListener(new ItemListener() {
			private JTable tbl;
			
			public void itemStateChanged(ItemEvent event) {
				if (tbl != null) {
					highScorePnl.remove(tbl);
				}
				tbl = getHighScores(selectLevelCombo.getSelectedIndex());
				highScorePnl.add(tbl.getTableHeader(), BorderLayout.PAGE_START);
				highScorePnl.add(tbl, BorderLayout.CENTER);
				repaint();
			}
		});
		highScorePnlOptions.add(selectLevelCombo);
		highScorePnl.add(highScorePnlOptions, BorderLayout.PAGE_END);
		
		tabs = new JTabbedPane();
		tabs.addTab("Options", optionPane);
		tabs.addTab("High Scores", highScorePnl);
		mineFieldPnl = new JPanel();
		tabs.addTab("Play Game", mineFieldPnl);
		tabs.addChangeListener(new ChangeListener() {
			JTable tbl = null;
			public void stateChanged(ChangeEvent event) {
				if (tabs.getSelectedIndex() == HIGH_SCORE_TAB_INDEX) {
					selectLevelCombo.setSelectedIndex(level);
					if (tbl != null) {
						highScorePnl.remove(tbl);
					}
					tbl = getHighScores(selectLevelCombo.getSelectedIndex());
					highScorePnl.add(tbl.getTableHeader(), BorderLayout.PAGE_START);
					highScorePnl.add(tbl, BorderLayout.CENTER);
					repaint();
				}
			}
		});
		add(tabs, BorderLayout.CENTER);
	}
	
	private void initGame() {
		width = WIDTHS[level];
		height = HEIGHTS[level];
		mines = MINES[level];
		
		if (timer.isRunning()) {
			timer.stop();
		}
		time = 0;
		
		firstClick = true;
		gameOver = false;
		
		mineFieldPnl.removeAll();
		mineFieldPnl.setLayout(new GridLayout(width, height));
		mineField.init(this, mineFieldPnl, width, height);
		tabs.setSelectedIndex(PLAY_GAME_TAB_INDEX);
		
		repaint();
	}
	
	public void mouseReleased(MouseEvent event) {
		if (gameOver) {
			return;
		}
		
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();
			if ((event.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
				mineField.flagButton(button);
			} else {	
				if (button.getText().equals("M")) {
					return;
				}
					
				if (firstClick) {
					firstClick = false;
					mineField.generate(mines, button);
					timer.start();
				}
					
				if (!mineField.hitBlock(button)) {
					button.setText("!");
					gameLose();
				} else if (mineField.checkWin()) {
					gameWin();
				}
			}
		}
	}
	
	private void gameLose() {
		gameOver = true;
		timer.stop();
		JOptionPane.showMessageDialog(this, "Oh no. YOU LOST!", "POW!", JOptionPane.ERROR_MESSAGE);
		initGame();
	}
	
	private void gameWin() {
		timer.stop();
		String name = JOptionPane.showInputDialog(this, "Congratulations! Enter your name:", "You beat JMineSweeper!",
		    JOptionPane.INFORMATION_MESSAGE);
		if (name != null) {
			highScoreEngine.add(new Object[] {level, name, time}, "/");
		}
	}
	
	private JTable getHighScores(int lvl) {
		HighScoreFilter filter = new HighScoreFilter(0, lvl, HighScoreFilter.Filter.EQUAL);
		Object[][] scores = highScoreEngine.load("/", 25, 2, HighScoreEngine.SortDirection.ASC,
		    new HighScoreFilter[] {filter});
		Object[][] data = new Object[scores.length][3];
		for (int i = 0; i < scores.length; i++) {
			data[i] = new Object[] {'#' + (i + 1), scores[i][1], scores[i][2] + " seconds"};
		}
			
		JTable scoreTable = new JTable(data, new String[] {"Rank", "Name", "Time"});
		scoreTable.setEnabled(false);
		scoreTable.setFont(HIGH_SCORE_TABLE_FONT);
		return scoreTable;
	}
	
	public void mouseClicked(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	public void mousePressed(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	
	private JRadioButton createRadioButton(String label, ButtonGroup group, JPanel panel) {
		JRadioButton radioBtn = new JRadioButton(label + " Level");
		radioBtn.setFont(RADIO_BUTTON_FONT);
		group.add(radioBtn);
		panel.add(radioBtn);
		return radioBtn;
	}
	
	public static void main(String[] args) {
		new JMineSweeper();
	}
}
