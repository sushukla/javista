package org.javista.model.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.javista.model.ITimeModel;

public class MstTimeModel implements ITimeModel {

	public String getTime() {
		final Date currentTime = new Date();
		final SimpleDateFormat sdf =
		        new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
		sdf.setTimeZone(TimeZone.getTimeZone("MST"));
		return sdf.format(currentTime);
	}

}
