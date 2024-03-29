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

package org.openpnp.model;

import javax.swing.table.TableModel;
import org.openpnp.ConfigurationListener;
import org.openpnp.spi.Head;
import org.openpnp.spi.Machine;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.NozzleTip;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Version;
import org.simpleframework.xml.core.Persist;


public class Package extends AbstractModelObject implements Identifiable {
	
	public enum NozzleTipSelect {
        TIP1,
        TIP2,
        TIP3,
        TIP4,
        TIP5,
        TIP6,
        None
    }
	
	 private NozzleTip nozzleTip;
	
    @Version(revision=1.1)
    private double version;    
    
    @Attribute
    private String id;

    @Attribute(required = false)
    private String description;

    @Element(required = false)
    private Footprint footprint;

    private Package() {
        this(null);
    }

    public Package(String id) {
        this.id = id;
        footprint = new Footprint();
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Warning: This should never be called once the Package is added to the configuration. It
     * should only be used when creating a new package.
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Footprint getFootprint() {
        return footprint;
    }

    public void setFootprint(Footprint footprint) {
        this.footprint = footprint;
    }

    public void setNozzleTip(NozzleTipSelect tip, Package packag) {
   	 Machine machine=Configuration.get().getMachine();
    	Head head;
		try {
			head = machine.getDefaultHead();
			 for (int i = 0; i < head.getNozzles().size(); i++) {
	             Nozzle nozzle = head.getNozzles().get(i);
	             for (int j=0;j < nozzle.getNozzleTips().size();j++)
	             {
	            	 NozzleTip nozzleTip= nozzle.getNozzleTips().get(j);
			
	            	if (nozzleTip.getName().equals(tip.name()))
	            	{
	            	 	
	            		nozzleTip.setCompatiblePackage(packag);
	            		
	            		
	            		//firePropertyChange("package", packag, packag);
	            		
	            	}
	            	else
	            	{
	            		//remove Compatable
	            		nozzleTip.removeCompatiblePackage(packag);
	            	}
	             }
			 }
       
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		
        
   }
   
   public NozzleTipSelect getNozzleTip(Part part)
   {
   	 
	   Machine machine=Configuration.get().getMachine();
   	  
   	Head head;
		try {
			
			head = machine.getDefaultHead();
		
   	 for (int i = 0; i < head.getNozzles().size(); i++) {
            Nozzle nozzle = head.getNozzles().get(i);
            for (int j=0; j < nozzle.getNozzleTips().size(); j++)
            {
           	 org.openpnp.spi.NozzleTip nozzleTip= nozzle.getNozzleTips().get(j);
            
           	 if (nozzleTip.canHandle(part))
           	 {
           		 
           		 //this is a hack for now... eventually it should read the list of configured tips.
           	 	switch (j)
            		{
            		case 0: 	return NozzleTipSelect.TIP1;
            		case 1: 	return NozzleTipSelect.TIP2;
            		case 2: 	return NozzleTipSelect.TIP3;
            		case 3: 	return NozzleTipSelect.TIP4;
            		case 4: 	return NozzleTipSelect.TIP5;
            		case 5: 	return NozzleTipSelect.TIP6;
            		}
            	}
            }
   	 }
   	 return  NozzleTipSelect.None;
		} catch (Exception e) {
			
			e.printStackTrace();
			return  NozzleTipSelect.None;
		}
   }
   
    
    
    
    @Override
    public String toString() {
        return String.format("id %s", id);
    }
}
