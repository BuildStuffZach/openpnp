/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.gui.tablemodel;

import javax.swing.table.AbstractTableModel;

import org.openpnp.gui.support.LengthCellValue;
import org.openpnp.gui.support.PartCellValue;
import org.openpnp.gui.support.RotationCellValue;
import org.openpnp.model.Board;
import org.openpnp.model.Board.Side;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.model.Package.NozzleTipSelect;
import org.openpnp.model.Placement;
import org.openpnp.model.Placement.Type;
import org.openpnp.spi.Feeder;
import org.openpnp.spi.Nozzle;

import org.openpnp.spi.Head;
import org.openpnp.spi.Machine;
import org.openpnp.spi.PnpJobProcessor.JobPlacement;


public class PlacementsTableModel extends AbstractTableModel {
    final Configuration configuration;

    private String[] columnNames =
            new String[] {"ID", "Part", "Side", "Nozzle Tip",  "Rot.", "Type", "Placed", "Status", "Check Fids","Height"};

    private Class[] columnTypes = new Class[] {PartCellValue.class, Part.class, Side.class,
    		NozzleTipSelect.class, 
            RotationCellValue.class, Type.class,
            Boolean.class, Status.class, Boolean.class, LengthCellValue.class};

    public enum Status {
        Ready,
        MissingPart,
        MissingFeeder,
        ZeroPartHeight,
        MissingNozzleTip
        
    }
    
   

    private Board board;
    private BoardLocation boardLocation;

    public PlacementsTableModel(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setBoardLocation(BoardLocation boardLocation) {
        this.boardLocation = boardLocation;
        if (boardLocation == null) {
            this.board = null;
        }
        else {
            this.board = boardLocation.getBoard();
        }
        fireTableDataChanged();
    }
    
    public Placement getPlacement(int index) {
        return board.getPlacements().get(index);
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return (board == null) ? 0 : board.getPlacements().size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return  columnIndex == 2 || columnIndex == 3 || columnIndex == 4
                || columnIndex == 5 || columnIndex == 6 || columnIndex == 7 || columnIndex == 9 || columnIndex == 10;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        try {
            Placement placement = board.getPlacements().get(rowIndex);
            if (columnIndex == 1) {
                placement.setPart((Part) aValue);
                fireTableCellUpdated(rowIndex, 8);
            }
            else if (columnIndex == 2) {
                placement.setSide((Side) aValue);
            }
            else if (columnIndex == 3) {
            	placement.getPart().getPackage().setNozzleTip((org.openpnp.model.Package.NozzleTipSelect) aValue, placement.getPart().getPackage());
                fireTableDataChanged();
                
            }
        /*    else if (columnIndex == 4) {
                LengthCellValue value = (LengthCellValue) aValue;
                value.setDisplayNativeUnits(true);
                Length length = value.getLength();
                Location location = placement.getLocation();
                location = Length.setLocationField(configuration, location, length, Length.Field.Y,
                        true);
                placement.setLocation(location);
            }
            */
            else if (columnIndex == 4) {
                placement.setLocation(placement.getLocation().derive(null, null, null,
                        Double.parseDouble(aValue.toString())));
            }
            else if (columnIndex == 5) {
                placement.setType((Type) aValue);
                fireTableCellUpdated(rowIndex, 8);
            }
            else if (columnIndex == 6) {
                //placement.setPlaced((Boolean) aValue);
            	boardLocation.setPlaced(placement.getId(), (Boolean) aValue);
            }
            else if (columnIndex == 8) {
                placement.setCheckFids((Boolean) aValue);
            }
            else if (columnIndex == 9) {
                LengthCellValue value = (LengthCellValue) aValue;
                value.setDisplayNativeUnits(true);
                Length length = value.getLength();
                
                if (length.getUnits() == null) {
                   
                   
                        length.setUnits(Configuration.get().getSystemUnits());
                   
                }
                
                placement.getPart().setHeight(length);
                fireTableDataChanged();
               
            }
        }
        catch (Exception e) {
            // TODO: dialog, bad input
        }
    }

   
    
    // TODO: Ideally this would all come from the JobPlanner, but this is a
    // good start for now.
    private Status getPlacementStatus(Placement placement) {
        if (placement.getPart() == null) {
            return Status.MissingPart;
        }
        if (placement.getType() == Placement.Type.Place) {
            boolean found = false;
            for (Feeder feeder : Configuration.get().getMachine().getFeeders()) {
                if (feeder.getPart() == placement.getPart() && feeder.isEnabled()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return Status.MissingFeeder;
            }

            if (placement.getPart().getHeight().getValue() == 0) {
                return Status.ZeroPartHeight;
            }
            
            if (placement.getPart().getPackage().getNozzleTip(placement.getPart())== org.openpnp.model.Package.NozzleTipSelect.None) {
                return Status.MissingNozzleTip;
            }
        }
        return Status.Ready;
    }

    public Object getValueAt(int row, int col) {
        Placement placement = board.getPlacements().get(row);
        Location loc = placement.getLocation();
        switch (col) {
            case 0:
                return new PartCellValue(placement.getId());
            case 1:
                return placement.getPart();
            case 2:
                return placement.getSide();
            case 3:
            	if (placement.getPart()==null) return null;
            	if (placement.getPart().getPackage()==null) return null;
            	
            	return placement.getPart().getPackage().getNozzleTip(placement.getPart());
            //case 4:
             //   return new LengthCellValue(loc.getLengthY(), true);
            case 4:
                // return String.format(Locale.US, configuration.getLengthDisplayFormat(),
                // loc.getRotation());
                return new RotationCellValue(loc.getRotation(), true);
            case 5:
                return placement.getType();
            case 6:
            	return boardLocation.getPlaced(placement.getId());
            case 7:
                return getPlacementStatus(placement);
            case 8:
                return placement.getCheckFids();
            case 9:
            	if (placement.getPart()==null) return null;
            	if (placement.getPart().getHeight()==null) return null;
            	return placement.getPart().getHeight().getValue();
            default:
                return null;
        }
    }
}
