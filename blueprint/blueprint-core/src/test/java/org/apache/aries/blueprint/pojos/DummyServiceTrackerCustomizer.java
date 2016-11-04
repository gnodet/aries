package org.apache.aries.blueprint.pojos;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class DummyServiceTrackerCustomizer implements ServiceTrackerCustomizer<PojoA, PojoB> {
    @Override
    public PojoB addingService(ServiceReference<PojoA> reference) {
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<PojoA> reference, PojoB service) {

    }

    @Override
    public void removedService(ServiceReference<PojoA> reference, PojoB service) {

    }
}
