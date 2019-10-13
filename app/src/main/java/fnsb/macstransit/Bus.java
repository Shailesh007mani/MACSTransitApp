package fnsb.macstransit;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Spud on 2019-10-12 for the project: MACS Transit.
 * <p>
 * For the license, view the file titled LICENSE at the root of the project
 */
public class Bus {

	/**
	 * The ID of the bus. While typically this is a number, on the rare occasion it can also be a name.
	 * As such, it should just be stored as a string. If this needs to be a number try parsing it from the string.
	 */
	public String busID;

	/**
	 * The latitude and longitude of the bus. Essentially making up its respective coordinates.
	 * This is stored as a double as latitude and longitude values are floating points numbers.
	 */
	public double latitude, longitude;

	/**
	 * The current heading of the bus (Think cardinal directions).
	 */
	public Heading heading;

	/**
	 * The current bus's color. This is more representative of the route its on (ie what is its route color),
	 * and thus is optional.
	 * <p>
	 * This is an int instead of a Color object because for whatever reason android stores its colors as ints.
	 */
	public int color;

	/**
	 * The bus's corresponding route.
	 */
	public Route route;

	/**
	 * The google maps marker corresponding to the bus.
	 * <p>
	 * <b>THIS SHOULD NOT BE CREATED OR MODIFIED OUTSIDE THE UI THREAD!</b>
	 * Doing so will cause an error and crash the app.
	 * The only reason why its part of the bus object is for ease of use within the UI thread.
	 * <p>
	 * Because of this specific behavior,
	 * the object is private and should only be set and retrieved with the corresponding functions {@code getMarker()} and {@code setMarker(Marker marker)}
	 */
	private Marker marker;

	/**
	 * Construction for the bus.
	 * Only the bus's ID and its corresponding route are required.
	 *
	 * @param busID The ID belonging to the bus.
	 * @param route The bus's route.
	 */
	public Bus(String busID, Route route) {
		this.busID = busID;
		this.route = route;
	}

	/**
	 * Retrieves the bus's current marker. This may be null if the bus has never been given a marker.
	 *
	 * @return The bus's Marker.
	 */
	public Marker getMarker() {
		return this.marker;
	}

	/**
	 * Sets the bus's marker.
	 * <p>
	 * Remember: When creating the marker be sure to create it within the UI thread.
	 *
	 * @param marker The marker.
	 */
	public void setMarker(Marker marker) {
		this.marker = marker;
	}

}
