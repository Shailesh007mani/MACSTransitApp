package fnsb.macstransit.Threads;

import android.util.Log;

import fnsb.macstransit.Activities.MapsActivity;
import fnsb.macstransit.RouteMatch.Bus;
import fnsb.macstransit.RouteMatch.Route;

/**
 * Created by Spud on 2019-10-13 for the project: MACS Transit.
 * <p>
 * For the license, view the file titled LICENSE at the root of the project
 *
 * @version 2.3
 * @since Beta 3
 */
public class UpdateThread {

	/**
	 * Create a boolean that will be used to determine if the update thread should be running or not.
	 */
	public boolean run = false;

	/**
	 * How quickly the thread should loop after its completed.
	 * Keep in mind that the smaller this number is the quicker it loops,
	 * and thus the more frequently its pulls data from the routematch server,
	 * and thus the more data it will consume.
	 * <p>
	 * This number is stored as a long, as it is the time in <i>milliseconds</i>,
	 * with the default being 4000 (4 seconds).
	 */
	private long updateFrequency = 4000;

	/**
	 * The MapsActivity (Main activity).
	 */
	private MapsActivity activity;

	/**
	 * Constructor for the UpdateThread.
	 *
	 * @param activity The MapsActivity (this should be the main activity).
	 */
	@SuppressWarnings("WeakerAccess")
	public UpdateThread(MapsActivity activity) {
		this.activity = activity;
	}

	/**
	 * Constructor for the UpdateThread.
	 *
	 * @param activity        The MapsActivity (this should be the main activity).
	 * @param updateFrequency How frequency (in milliseconds) the thread should loop.
	 *                        If this is omitted, it will default to 4000 milliseconds (4 seconds).
	 */
	public UpdateThread(MapsActivity activity, long updateFrequency) {
		this(activity);
		this.updateFrequency = updateFrequency;
	}

	/**
	 * This is the thread that repeatedly queries the routematch server for data on the buses, routes, and stops.
	 * It loops with the frequency defined by the {@code updateFrequency} variable (default of 4000 milliseconds, or 4 seconds).
	 *
	 * @return The thread. Note that this dies not run the thread, that has to be called separately.
	 */
	public Thread thread() {
		return new Thread(() -> {

			// For debugging purposes, let the poor developer know when the thread has started.
			Log.w("Update thread", "Starting up...");

			// Loop continuously while the run variable is true, and  the thread hasn't been interrupted for whatever reason.
			while (this.run && !Thread.interrupted()) {

				// Make a copy of the selected routes array to run iterations on (to avoid the ConcurrentModificationException of death).
				Route[] routes = this.activity.selectedRoutes;

				// If there are no selected routes, loop quickly (every quarter second) rather than the set frequency.
				if (routes.length != 0) {
					// TODO Update comments
					// Because there is a lot of JSON parsing in the following section, be sure to catch any JSON parsing errors.
					try {

						// For each of the selected routes from the activity, retrieve one, and execute the following
						for (Route route : routes) {

							// Update the bus positions
							Bus[] oldBuses = route.buses;

							Bus[] newBuses = Bus.getBuses(route);

							if (oldBuses.length != newBuses.length) {
								route.buses = newBuses;
							} else {
								for (int index = 0; index < oldBuses.length; index++) {
									Bus oldBus = oldBuses[index];
									for (Bus newBus : newBuses) {
										if (newBus.busID.equals(oldBus.busID)) {
											oldBus.color = newBus.color;
											oldBus.heading = newBus.heading;
											oldBus.latitude = newBus.latitude;
											oldBus.longitude = newBus.longitude;
										}
									}
									oldBuses[index] = oldBus;
								}
							}
							this.activity.runOnUiThread(() -> this.activity.drawBuses());

						}
						// Sleep for the given update frequency
						try {
							Thread.sleep(this.updateFrequency);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Thread.yield();
					} catch (org.json.JSONException e) {
						// For now, just print a stack trace if there are any errors.
						e.printStackTrace();
					}
				} else {

					// Quick sleep since there are no routes to track
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Thread.yield();
				}

				// Notify the developer that the thread is now starting over.
				Log.d("Update thread", "Looping...");
			}

			// Notify the developer that the thread has exited the while loop and will now stop.
			Log.w("Update thread", "Shutting down...");
		});
	}
}
