package fnsb.macstransit.ActivityListeners;

import android.util.Log;

import fnsb.macstransit.MapsActivity;
import fnsb.macstransit.RouteMatch.Stop;

/**
 * Created by Spud on 2019-10-28 for the project: MACS Transit.
 * <p>
 * For the license, view the file titled LICENSE at the root of the project
 *
 * @version 1.0
 * @since Beta 7
 */
public class AdjustZoom implements com.google.android.gms.maps.GoogleMap.OnCameraIdleListener {

	/**
	 * The MapsActivity that this listener will apply to.
	 * This is used to get access to all the public variables.
	 */
	private MapsActivity activity;

	/**
	 * Constructor for the listener.
	 *
	 * @param activity The MapsActivity that will be using this listener (just pass {@code this} as the argument in the activity).
	 */
	public AdjustZoom(MapsActivity activity) {
		this.activity = activity;
	}

	/**
	 * Called when camera movement has ended,
	 * there are no pending animations and the user has stopped interacting with the map.
	 * <p>
	 * This is called on the Android UI thread.
	 */
	@Override
	public void onCameraIdle() {
		// Get the camera's new zoom position
		float zoom = this.activity.map.getCameraPosition().zoom;
		Log.d("CameraChange", "Zoom level: " + zoom);

		// Get how much it has changed from the default zoom (11).
		float zoomChange = 11.0f / zoom;
		Log.d("CameraChange", "Zoom change: " + zoomChange);

		// Iterate through all the routes.
		for (fnsb.macstransit.RouteMatch.Route route : MapsActivity.allRoutes) {

			// If the route isn't null, execute the following:
			if (route != null) {
				// Iterate through all the stops in the route.
				for (Stop stop : route.stops) {

					// Get the stop's icon
					com.google.android.gms.maps.model.Circle icon = stop.getIcon();

					// If the icon isn't null, change its radius in proportion to the zoom change.
					if (icon != null) {
						icon.setRadius(Stop.RADIUS * (Math.pow(zoomChange, 5)));
					}
				}
			}
		}

		// TODO Iterate through all the shared stops
	}
}
