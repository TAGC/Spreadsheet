package spreadsheet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import spreadsheet.api.CellLocation;
import spreadsheet.api.ExpressionUtils;
import spreadsheet.api.SpreadsheetInterface;
import spreadsheet.api.value.LoopValue;
import spreadsheet.api.value.Value;

public class Spreadsheet implements SpreadsheetInterface {
	
	private static final Value default_value       = null;
	private static final String default_expression = "";
	private Map<CellLocation, Cell> cellmap
	        = new HashMap<CellLocation, Cell>();
	
	private Map<CellLocation, Double> cell_values
	        = new HashMap<CellLocation, Double>();
	
	private Set<Cell> altered_cells
	        = new HashSet<Cell>();
	
	private Set<Cell> ignored_cells
	        = new HashSet<Cell>();

	@Override
	public void setExpression(CellLocation location, String expression) {
		Cell cell;
		if (cellmap.containsKey(location)) {
			cell = cellmap.get(location);
		} else {
			cell = new Cell(location, this);
			cellmap.put(location, cell);
		}
		
		System.out.println("Setting " + expression + " at " + cell.getLocation());
		cell.setExpression(expression);
		altered_cells.add(cell);
	}

	@Override
	public String getExpression(CellLocation location) {
		if (cellmap.containsKey(location)) {
			return cellmap.get(location).getExpression();
		}
		
		return default_expression;
	}
	

	@Override
	public Value getValue(CellLocation location) {
		if (cellmap.containsKey(location)) {
			return cellmap.get(location).getValue();
		}
		
		return default_value;
	}
	
	public Map<CellLocation, Cell> getCellMap() {
		return cellmap;
	}
	
	public Set<Cell> getAlteredCells() {
		return altered_cells;
	}

	@Override
	public void recompute() {
		Iterator<Cell> iterator;
		Cell next_cell;
		Set<Cell> resolved_cells;
		
		resolved_cells = new HashSet<Cell>();
		
		iterator = altered_cells.iterator();
		while(iterator.hasNext()) {
			next_cell = iterator.next();
			
			if (ignored_cells.contains(next_cell)) {
				resolved_cells.add(next_cell);
				continue;
			}
			
			recomputeCell(next_cell);
			resolved_cells.add(next_cell);
		}
		
		iterator = resolved_cells.iterator();
		while(iterator.hasNext()) {
			next_cell = iterator.next();
			altered_cells.remove(next_cell);
		}
		
		ignored_cells.clear();
	}
	
	private void recomputeCell(Cell c) {
		String cell_expression;
		Value cell_value;
		LinkedHashSet<Cell> cells_seen = new LinkedHashSet<Cell>();
		
		cell_expression = c.getExpression();
		cell_value      = ExpressionUtils.computeValue(cell_expression,
													   cell_values);
		
		c.setValue(cell_value);
		checkLoops(c, cells_seen);
	}
	
	private void checkLoops(Cell c, LinkedHashSet<Cell> cells_seen) {
		LinkedHashSet<Cell> cells;
		Set<Cell> cells_referenced;
		
		cells_referenced = c.getCellsReferenced();
		
		if (cells_seen.contains(c)) {
			cells = new LinkedHashSet<Cell>(cells_referenced);
			markAsLoop(c, cells);
		} else {
			cells_seen.add(c);
			for(Cell cell_referenced : cells_referenced) {
				checkLoops(cell_referenced, cells_seen);
			}
			cells_seen.remove(c);
		}
		
	}
	
	private void markAsLoop(Cell start_cell, LinkedHashSet<Cell> cells) {
		start_cell.setValue(LoopValue.INSTANCE);
		ignored_cells.add(start_cell);
		for (Cell c : cells) {
			c.setValue(LoopValue.INSTANCE);
			ignored_cells.add(c);
		}
	}
}
 