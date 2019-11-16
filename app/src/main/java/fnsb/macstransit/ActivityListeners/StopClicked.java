package fnsb.macstransit.ActivityListeners;

import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

import fnsb.macstransit.MapsActivity;
import fnsb.macstransit.R;
import fnsb.macstransit.RouteMatch.RouteMatch;
import fnsb.macstransit.RouteMatch.SharedStop;
import fnsb.macstransit.RouteMatch.Stop;

/**
 * Created by Spud on 2019-10-30 for the project: MACS Transit.
 * <p>
 * For the license, view the file titled LICENSE at the root of the project
 *
 * @version 1.0
 * @since Beta 7
 */
public class StopClicked implements com.google.android.gms.maps.GoogleMap.OnCircleClickListener {

	/**
	 * The maps activity that this listener corresponds to.
	 */
	private MapsActivity activity;

	/**
	 * Constructor the the StopClicked listener.
	 *
	 * @param activity The activity that this listener corresponds to.
	 */
	public StopClicked(MapsActivity activity) {
		this.activity = activity;
	}

	/**
	 * Called when a circle is clicked.
	 * <p>
	 * This is called on the Android UI thread.
	 *
	 * @param circle The circle that is clicked.
	 */
	@Override
	public void onCircleClick(com.google.android.gms.maps.model.Circle circle) {
		if (circle.getTag() instanceof Stop) { // Check if the circle is a stop
			Log.d("onCircleClick", "Showing stop infowindow");
			this.showStopInfoWindow((Stop) circle.getTag());
		} else if (circle.getTag() instanceof SharedStop) { // Check if the circle is a shared stop
			Log.d("onCircleClick", "Showing sharedStop infowindow");
			this.showSharedStopInfoWindow((SharedStop) circle.getTag());
		} else {
			// If it was neither a stop or a shared stop, warn that there was an unaccounted for object.
			Log.w("onCircleClick", String.format("Circle object (%s) unaccounted for!",
					java.util.Objects.requireNonNull(circle.getTag()).toString()));
		}
	}

	/**
	 * Creates and shows the info window for the provided Stop.
	 *
	 * @param stop The Stop to create the info window for.
	 */
	private void showStopInfoWindow(Stop stop) {
		// Get the marker from the Stop.
		Marker marker = stop.getMarker();
		if (marker == null) {
			// If the stop doesn't have a marker, just use toast to display the stop ID.
			Toast.makeText(this.activity, stop.stopID, Toast.LENGTH_SHORT).show();
		} else {
			// If the stop does have a marker, set the marker to be visible, and show the info window corresponding to that marker.
			marker.setVisible(true);

			// Get the stops departures and arrivals
			StringBuilder snippetText = new StringBuilder();
			JSONArray stopData = RouteMatch.parseData(MapsActivity.routeMatch.getStop(stop));
			int count = stopData.length();

			// Iterate through the stops, but have a hard limit of 2 (at least while scrolling doesn't work).
			for (int index = 0; index < count && index < 2; index++) {
				Log.d("showStopInfoWindow", String.format("Parsing stop times for stop %d/%d", index, count));

				// Try to get the stop time from the current stop.
				JSONObject object;
				try {
					object = stopData.getJSONObject(index);
				} catch (JSONException e) {
					// If that fails, just print the stack trace, and break from the for loop.
					e.printStackTrace();
					break;
				}

				// Set the arrival and departure time to the arrival and departure time in the jsonObject.
				// At this point this is stored in 24-hour time.
				String arrivalTime = this.getTime(object, "predictedArrivalTime"),
						departureTime = this.getTime(object, "predictedDepartureTime");

				// If the user doesn't use 24-hour time, convert to 12-hour time.
				if (!DateFormat.is24HourFormat(this.activity)) {
					Log.d("showStopInfoWindow", "Converting time to 12 hour time");
					arrivalTime = this.formatTime(arrivalTime);
					departureTime = this.formatTime(departureTime);
				}

				// Append the arrival and departure times to the snippet text.
				snippetText.append(String.format("%s %s\n%s %s\n\n", this.activity.getString(R.string.expected_arrival), arrivalTime,
						this.activity.getString(R.string.expected_departure), departureTime));
			}

			// Build the snippet text, and show the info window.
			marker.setSnippet(snippetText.toString());
			marker.showInfoWindow();
		}
	}

	/**
	 * Creates and shows the info window for the provided Shared Stop.
	 *
	 * @param sharedStop The Shared Stop to create the info window for.
	 */
	private void showSharedStopInfoWindow(SharedStop sharedStop) {
		// Get the marker from the SharedStop.
		Marker marker = sharedStop.getMarker();
		if (marker == null) {
			// If the shared stop doesn't have a marker, just use toast to display the stop ID.
			Toast.makeText(this.activity, sharedStop.stopID, Toast.LENGTH_SHORT).show();
		} else {
			// If the stop does have a marker, set the marker to be visible, and show the info window corresponding to that marker.
			marker.setVisible(true);

			// Get the stops departures and arrivals for each route that corresponds to that stop.
			StringBuilder snippetText = new StringBuilder();
			for (fnsb.macstransit.RouteMatch.Route route : sharedStop.routes) {
				Stop stop = null;
				Log.d("showSharedStopInfo", "Parsing stops for route: " + route.routeName);

				// Iterate through all the stops in the route to find the stop that we care about.
				for (Stop stops : route.stops) {
					if (stops.stopID.equals(sharedStop.stopID)) {
						stop = stops;
						break;
					}
				}

				// Once the stop was found, execute the following:
				if (stop != null) {

					// Get the stop data for the stop.
					JSONArray stopData = RouteMatch.parseData(MapsActivity.routeMatch.getStop(stop));
					int count = stopData.length();

					// Iterate through the stops, but have a hard limit of 1 (at least while scrolling doesn't work).
					for (int index = 0; index < count && index < 1; index++) {
						Log.d("showSharedStopInfo", String.format("Parsing stop times for stop %d/%d", index, count));

						// Try to get the stop time from the current stop.
						JSONObject object;
						try {
							object = stopData.getJSONObject(index);
						} catch (JSONException e) {
							// If that fails, just print the stack trace, and break from the for loop.
							e.printStackTrace();
							break;
						}


						// Set the arrival and departure time to the arrival and departure time in the jsonObject.
						// At this point this is stored in 24-hour time.
						String arrivalTime = this.getTime(object, "predictedArrivalTime"),
								departureTime = this.getTime(object, "predictedDepartureTime");

						// If the user doesn't use 24-hour time, convert to 12-hour time.
						if (!DateFormat.is24HourFormat(this.activity)) {
							arrivalTime = this.formatTime(arrivalTime);
							departureTime = this.formatTime(departureTime);
						}

						// Append the arrival and departure times to the snippet text.
						snippetText.append(String.format("Route: %s\n%s %s\n%s %s\n\n", route.routeName,
								this.activity.getString(R.string.expected_arrival), arrivalTime,
								this.activity.getString(R.string.expected_departure), departureTime));
					}
				}
			}

			// Build the snippet text, and show the info window.
			marker.setSnippet(snippetText.toString());
			marker.showInfoWindow();
		}
	}

	/**
	 * Returns the time (in 24-hour form) that is found in the provided JSONObject via a regex.
	 *
	 * @param json The JSONObject to search.
	 * @param tag  The tag in the JSONObject to search.
	 * @return The time (in 24-hour form) as a String that was found in the JSONObject.
	 * This may be null if no such string was able to be found, or if there was a JSONException.
	 */
	private String getTime(JSONObject json, String tag) {
		try {
			// Get a matcher object from the time regex, and have it match the tag.
			java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d\\d:\\d\\d").matcher(json.getString(tag));

			// If the match was found, return it, if not return null.
			return matcher.find() ? matcher.group(0) : null;

		} catch (JSONException jsonException) {
			// If there was an error, print a stack trace, and return null.
			jsonException.printStackTrace();
			return null;
		}
	}

	/**
	 * Formats the time from 24-hour time to 12-hour time (and even includes AM and PM).
	 *
	 * @param time The time to format as a string.
	 * @return The formatted 12-hour time.
	 * This may return the original 12 hour time if there was an exception parsing the time.
	 */
	private String formatTime(String time) {
		try {
			// Try to format the time from 24 hours to 12 hours (including AM and PM).
			return new SimpleDateFormat("K:mm a", Locale.US).format(new SimpleDateFormat("H:mm", Locale.US).parse(time));
		} catch (java.text.ParseException parseException) {
			// If there was a parsing exception simply return the old time.
			parseException.printStackTrace();
			return time;
		}
	}
}
