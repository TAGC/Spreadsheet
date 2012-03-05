package spreadsheet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import spreadsheet.api.CellLocation;
import spreadsheet.api.ExpressionUtils;
import spreadsheet.api.observer.Observer;
import spreadsheet.api.value.InvalidValue;
import spreadsheet.api.value.Value;

public class Cell implements Observer<Cell> {
	
	private CellLocation location;
	private Spreadsheet spreadsheet;
	private Value value;
	private String expression;
	
	/*
	 * Cells that this cell depends on
	 */
	private Set<Cell> cells_referenced 
	        = new HashSet<Cell>();
	
	/*
	 * Cells that depends on this cell
	 */
	private Set<Observer<Cell>> observer_cell_set
	        = new HashSet<Observer<Cell>>();
	
	public Cell(CellLocation location, Spreadsheet spreadsheet) {
		this.location = location;
		this.spreadsheet   = spreadsheet;
		
		value = null;
		expression = null;
	}
	
	private void removeObserver(Observer<Cell> observer) {
		observer_cell_set.remove(observer);
	}
	
	private void addObserver(Observer<Cell> observer) {
		observer_cell_set.add(observer);
	}
	
	public Value getValue() {
		return value;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public Set<Cell> getCellsReferenced() {
		return cells_referenced;
	}
	
	public CellLocation getLocation() {
		return location;
	}
	
	public void setExpression(String new_expression) {
		expression = new_expression;
		value      = new InvalidValue(expression);
		
		unobserveCells();
		getReferencedCells();
		informSpreadsheet();
		informObservers();
	}
	
	private void unobserveCells() {
		Iterator<Cell> iterator;
		Cell next_cell;
		
		iterator = cells_referenced.iterator();
		while (iterator.hasNext()) {
			next_cell = iterator.next();
			next_cell.removeObserver(this);
		}
		
		cells_referenced.clear();
	}
	
	public void setValue(Value new_value) {
		value = new_value;
	}
	
	@Override
	public void update(Cell changed) {
		value = new InvalidValue(expression);
		informObservers();
		informSpreadsheet();
	}
	
	private void informObservers() {
		Iterator<Observer<Cell>> iterator;
		Observer<Cell> next_cell_observer;
		
		iterator = observer_cell_set.iterator();
		while (iterator.hasNext()) {
			next_cell_observer = iterator.next();
			System.out.println("OBSERVER: " + next_cell_observer);
			next_cell_observer.update(this);
		}
		
		System.out.println("Observers: " + Arrays.deepToString(observer_cell_set.toArray()));
	}
	
	private boolean alreadySetForUpdate() {
		return spreadsheet.getAlteredCells().contains(this);
	}
	
	private void informSpreadsheet() {
		if (!alreadySetForUpdate()) {
			spreadsheet.getAlteredCells().add(this);
		}
	}
	
	private void getReferencedCells() {
		Set<CellLocation> cell_locations;
		
		cell_locations = ExpressionUtils.getReferencedLocations(expression);
		for(CellLocation cl : cell_locations) {
			cells_referenced.add(spreadsheet.getCellMap().get(cl));
			
			/*
			 * This code notifies the dependent cell that this
			 * cell is observing (dependent upon) it.
			 */
			spreadsheet.getCellMap().get(cl).addObserver(this);
		}
		
		System.out.println("Cells Referenced: " + Arrays.deepToString(cells_referenced.toArray()));
	}
	
	@Override
	public String toString() {
		return location.toString();
	}

}
