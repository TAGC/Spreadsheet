package spreadsheet;

import spreadsheet.gui.SpreadsheetGUI;


public class Main {

    private static final int DEFAULT_NUM_ROWS = 5000;
    private static final int DEFAULT_NUM_COLUMNS = 5000;

    public static void main(String[] args) {
    	int rows, columns;
    	
    	if (args.length == 2) {
    		rows    = Integer.parseInt(args[0]);
    		columns = Integer.parseInt(args[1]);
    	} else {
    		rows = DEFAULT_NUM_ROWS;
    		columns = DEFAULT_NUM_COLUMNS;
    	}
    	
    	Spreadsheet spread = new Spreadsheet();
    	SpreadsheetGUI spreadGUI = new SpreadsheetGUI(spread, rows, columns);
    	spreadGUI.start();
    }

}
