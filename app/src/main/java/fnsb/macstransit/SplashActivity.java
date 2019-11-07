package fnsb.macstransit;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;

import fnsb.macstransit.RouteMatch.Route;
import fnsb.macstransit.RouteMatch.RouteMatch;

/**
 * Created by Spud on 2019-11-04 for the project: MACS Transit.
 * <p>
 * For the license, view the file titled LICENSE at the root of the project
 *
 * @version 1.0
 * @since Beta 7
 */
public class SplashActivity extends androidx.appcompat.app.AppCompatActivity {

	/**
	 * TODO Documentation
	 */
	public static boolean loaded = false;

	/**
	 * The TextView widget in the activity.
	 */
	private android.widget.TextView textView;

	/**
	 * The ProgressBar widget in the activity.
	 */
	private android.widget.ProgressBar progressBar;

	/**
	 * The Button widget in the activity.
	 */
	private android.widget.Button button;

	/**
	 * The RouteMatch object to be loaded in the splash screen.
	 */
	private RouteMatch routeMatch;
	
	/**
	 * The routes from the RouteMatch object to be loaded in the splash screen.
	 */
	private Route[] routes;

	/**
	 * Called when the activity is starting.
	 * This is where most initialization should go: calling setContentView(int) to inflate the activity's UI,
	 * using findViewById(int) to programmatically interact with widgets in the UI,
	 * calling managedQuery(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[],
	 * java.lang.String) to retrieve cursors for data being displayed, etc.
	 * <p>
	 * You can call finish() from within this function,
	 * in which case onDestroy() will be immediately called after onCreate(Bundle)
	 * without any of the rest of the activity lifecycle (onStart(), onResume(), onPause(), etc) executing.
	 *
	 * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
	 *                           Note: Otherwise it is null. This value may be null.
	 */
	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the view to that of the splash screen.
		this.setContentView(R.layout.splashscreen);

		// Find the widgets of use in the splash screen, and assign them to their own private variables.
		this.textView = this.findViewById(R.id.textView);
		this.progressBar = this.findViewById(R.id.progressBar);
		this.button = this.findViewById(R.id.button);

		// Set the button widget to have no current onClickListener, and set it to be invisible for now.
		this.button.setOnClickListener(null);
		this.button.setVisibility(View.INVISIBLE);

		// Setup the progress bar by defining its max, and if the SDK supports it, assign its min as well.
		this.progressBar.setMax(100);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			this.progressBar.setMin(0);
		}

		// Make sure the progress bar is visible to the user.
		this.progressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(),
	 * for your activity to start interacting with the user.
	 * This is an indicator that the activity became active and ready to receive input.
	 * It is on top of an activity stack and visible to user.
	 * <p>
	 * On platform versions prior to Build.VERSION_CODES.Q this is also a good place to try to open exclusive-access devices or to get access to singleton resources.
	 * Starting with Build.VERSION_CODES.Q there can be multiple resumed activities in the system simultaneously,
	 * so onTopResumedActivityChanged(boolean) should be used for that purpose instead.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		this.progressBar.setVisibility(View.VISIBLE);
		this.setProgress(0);

		// First, check if the user has internet
		if (this.hasInternet()) {

			// Then create the routematch object
			try {
				this.routeMatch = new RouteMatch("https://fnsb.routematch.com/feed/");
			} catch (java.net.MalformedURLException e) {
				// If the route match url is malformed (hence an error was thrown) simply log it, and then return early.
				// Don't start loading data.
				Log.e("onResume", "The RouteMatch URL is malformed!");
				return;
			}

			// If the activity has made it this far then proceed to load the data from the RouteMatch object.
			this.loadData().start();

		} else {
			// Since the user doesn't have internet, let them know, and add an option to open internet settings via clicking the button.
			// First, hide the progress bar.
			this.progressBar.setVisibility(View.INVISIBLE);

			// Then, set the message of the text view to notify the user that there is no internet connection.
			this.setMessage(R.string.nointernet);

			// Then setup the button to open the internet settings when clicked on, and make it visible.
			this.button.setText(R.string.open_internet_settins);
			this.button.setOnClickListener((click) -> {
				this.startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
				// Also, close this application when clicked
				this.finish();
			});
			this.button.setVisibility(View.VISIBLE);

			// Since technically everything (which is nothing) has been loaded, set the variable as so
			SplashActivity.loaded = true;
		}

	}

	/**
	 * TODO Documentation
	 */
	@Override
	protected void onPause() {
		super.onPause();

		// Simply close the application, since it hasn't finished loading.
		if (!SplashActivity.loaded) {
			System.exit(0);
		}
	}

	/**
	 * Checks if the device has a current internet connection.
	 *
	 * @return Whether or not the device has an internet connection.
	 */
	private boolean hasInternet() {
		android.net.NetworkInfo activeNetwork = ((android.net.ConnectivityManager) this.getApplicationContext()
				.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	/**
	 * Sets the percent progress on the progress bar from a scale of 0 to 1 (as a double).
	 * If the progress is larger than 1, it will be converted to 1. If the progress is less than 0,
	 * it will be converted to 0.
	 *
	 * @param progress The progress to be set on the progress bar as a percent (between 0 and 1).
	 */
	private void setProgress(double progress) {
		this.runOnUiThread(() -> {
			// Convert the progress to be an int out of 100.
			int p = (int) Math.round(progress * 100);

			// Validate that that the progress is between 0 and 100.
			p = (p > 100) ? 100 : ((p < 0) ? 0 : p);
			Log.d("setProgress", "Progress: " + p);

			// Apply the progress to the progress bar, and animate it if its supported in the SDK.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				this.progressBar.setProgress(p, true);
			} else {
				this.progressBar.setProgress(p);
			}
		});
	}

	/**
	 * Sets the text of this activity's TextView to the provided message.
	 * This is executed on the UI Thread.
	 *
	 * @param message The message to be set (as a string ID).
	 */
	private void setMessage(int message) {
		this.runOnUiThread(() -> this.textView.setText(message));
	}

	/**
	 * Creates the thread that will be used to parse the routematch object,
	 * load the routes from the routematch object,
	 * and load the stops from the routes.
	 *
	 * @return The thread created. Note that this has not been started at this point,
	 * so start() needs to be called in order for this to run.
	 */
	private Thread loadData() {
		// Create a thread that will be used to load the data.
		Thread t = new Thread(() -> {

			// Inform the user that the routes are being loaded
			Log.d("loadData", "Loading routes from master schedule");
			this.setMessage(R.string.load_routes);

			// Load the routes from the RouteMatch object
			this.routes = Route.generateRoutes(this.routeMatch);

			// If there are routes that were loaded, execute the following:
			if (this.routes.length != 0) {

				// Update the progress to halfway, and inform the user that we are now loading stops.
				Log.d("loadData", "Loading stops in the routes");
				this.setProgress(0.5d);
				this.setMessage(R.string.load_stops);

				// Load the stops in each route.
				for (int i = 0; i < this.routes.length; i++) {
					// Get the route at the current index (i) from all the routes that were loaded.
					Route route = this.routes[i];

					// Load the stops in the route.
					route.stops = route.loadStops(this.routeMatch);

					// Set the progress to the current index (plus 1) out of the all the routes to be parsed,
					// and then divide that value in half, as this is the other 50% to be processed.
					this.setProgress(0.5d + (((i + 1d) / this.routes.length) / 2));
				}

				// Once all the routes and stops have been loaded,
				// call the dataLoaded function to mark that we are done and ready to load the maps activity.
				this.dataLoaded();
			} else {
				// Since there were no routes to be loaded, inform the user.
				Log.w("loadData", "Unable to load routes!");
				this.setMessage(R.string.no_routes);

				// Also add a chance to restart the activity to retry...
				this.runOnUiThread(() -> {
					// First hide the progress bar since it is no longer of use.
					this.progressBar.setVisibility(View.INVISIBLE);

					// THen setup the button to relaunch the activity, and make it visible.
					this.button.setText(R.string.retry);
					this.button.setOnClickListener((click) -> this.onResume());
					this.button.setVisibility(View.VISIBLE);
				});
			}
		});

		// Set the name of the thread, and return it.
		t.setName("Splash-Network");
		return t;
	}

	/**
	 * Starts the MapsActivity, and closes this activity.
	 * This should be called last once everything has been loaded from this activity.
	 */
	private void dataLoaded() {
		// Set the routeMatch object in the MapsActivity to that of this routeMatch object.
		MapsActivity.routeMatch = this.routeMatch;

		// Set the routes in the MapsActivity to the routes that were loaded in tis activity.
		MapsActivity.allRoutes = this.routes;

		// Set the value of loaded to be true at this point.
		SplashActivity.loaded = true;

		// Start the MapsActivity, and close this splash activity.
		this.startActivity(new Intent(this, MapsActivity.class));
		this.finish();
	}
}
