package io.rover.model;

import java.util.Date;

/**
 * Created by Rover Labs Inc on 2016-04-12.
 */
public class DeviceUpdateEvent extends Event {

    public DeviceUpdateEvent(Date date) {
        mDate = date;
    }
}
