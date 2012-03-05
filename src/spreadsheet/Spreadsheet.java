package spreadsheet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import spreadsheet.api.CellLocation;
import spreadsheet.api.ExpressionUtils;
import spreadsheet.api.SpreadsheetInterface;
import spreadsheet.api.value.InvalidValue;
import spreadsheet.api.value.LoopValue;
import spreadsheet.api.value.Value;
import spreadsheet.api.value.ValueVisitor;

public class Spreadsheet implements SpreadsheetInterface {
	
	private static final Value default_value       = null;
	private static final String default_expression = "";
	private final Map<CellLocation, Cell> cellmap
	        = new HashMap<CellLocation, Cell>();
	
	private final Map<CellLocation, Double> cell_values
	        = new HashMap<CellLocation, Double>();
	
	private final Set<Cell> altered_cells
	        = new HashSet<Cell>();
	
	private final Set<Cell> ignored_cells
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
		LinkedHashSet<Cell> cells_seen;
		ArrayDeque<Cell> dependent_cells = new ArrayDeque<Cell>();
		Cell current_cell;
		boolean cell_has_dependents;
		
		cells_seen = new LinkedHashSet<Cell>();
		
		cell_expression = c.getExpression();
		//cell_value      = ExpressionUtils.computeValue(cell_expression,
		//											   cell_values);
		
		c.setValue(new InvalidValue(c.getExpression()));
		checkLoops(c, cells_seen);
		
		if (c.getValue() == LoopValue.INSTANCE) { return; }
		
		System.out.println("CELL VALUE: " + c.getValue());
		dependent_cells.addFirst(c);
		
		while(!dependent_cells.isEmpty()) {
			current_cell = dependent_cells.removeFirst();
			
			System.out.println("Current cell: " + current_cell);
			cell_has_dependents = false;
			for(Cell dependent : current_cell.getCellsReferenced()) {
				if (altered_cells.contains(dependent)) {
					dependent_cells.addFirst(dependent);
					cell_has_dependents = true;
				}
			}
			
			if (!cell_has_dependents) {
				calculateCellValue(c);
				System.out.println("THIS FAR!");
				ignored_cells.add(current_cell);
			} else {
				dependent_cells.addLast(current_cell);
			}
			
			System.out.println(Arrays.deepToString(dependent_cells.toArray()));
		}
	}
	
	private void checkLoops(Cell c, LinkedHashSet<Cell> cells_seen) {
		LinkedHashSet<Cell> cells;
		Set<Cell> cells_referenced;
		
		cells_referenced = c.getCellsReferenced();
		
		if (cells_seen.contains(c)) {
			cells = new LinkedHashSet<Cell>(cells_referenced);
			System.out.println("SEEN CELL: " + c);
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
	
	private void buildCellValueMap(Cell cell) {
		Set<Cell> cells_referenced;
		Value dependent_value;
		
		cells_referenced = cell.getCellsReferenced();
		
		System.out.println("I guess this far too?");
		System.out.println(cells_referenced.toArray().getClass().getName());

		Iterator<Cell> iterator = cells_referenced.iterator();
		System.out.println("THIS FAR MAN!");
		while(iterator.hasNext()) {
			final Cell next_dependent = iterator.next();
			System.out.println("NEXT DEPENDENT: " + next_dependent);
			dependent_value    = next_dependent.getValue();
			
			dependent_value.visit(new ValueVisitor() {

				@Override
				public void visitDouble(double value) {
					cell_values.put(next_dependent.getLocation(),
							        value);
				}

				@Override
				public void visitLoop() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void visitString(String expression) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void visitInvalid(String expression) {
					// TODO Auto-generated method stub
					
				}
				
				
			});
		}
		
		
	}

	private void calculateCellValue(Cell cell) {
		Value cell_value;
		String cell_expression;
		
		System.out.println("THIS FAR MAN LOL!");
		cell_expression = cell.getExpression();
		System.out.println("NOW THIS FAR!");
		buildCellValueMap(cell);
		
		System.out.println("CELL VALUES: " + Arrays.deepToString(cell_values.entrySet().toArray()));
		System.out.println("CELL EXPRESSION: " + cell_expression);
		cell_value = ExpressionUtils.computeValue(cell_expression, cell_values);
		cell.setValue(cell_value);
	}
}