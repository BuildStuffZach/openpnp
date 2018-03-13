package org.openpnp.spi;

import java.util.Set;

import org.openpnp.model.Identifiable;
import org.openpnp.model.Named;
import org.openpnp.model.Part;

/**
 * A NozzleTip is the physical interface between a Nozzle and a Part.
 */
public interface NozzleTip extends Identifiable, Named, WizardConfigurable, PropertySheetHolder {
    public boolean canHandle(Part part);
    public void setCompatiblePackage(org.openpnp.model.Package compatiblePackage);
}
