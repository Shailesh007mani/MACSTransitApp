package fnsb.macstransit;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.MalformedURLException;

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
public class SplashActivity extends AppCompatActivity {

	/**
	 * TODO Documentation
	 */
	private TextView console;

	/**
	 * TODO Documentation
	 */
	private ProgressBar progressBar;

	/**
	 * TODO Documentation
	 */
	private Button button;

	/**
	 * TODO Documentation
	 */
	private RouteMatch routeMatch;

	/**
	 * TODO Documentation
	 */
	private Route[] routes;


	/**
	 * TODO Documentation
	 *
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.init);

		this.console = this.findViewById(R.id.textView);
		this.progressBar = this.findViewById(R.id.progressBar);
		this.button = this.findViewById(R.id.button);
		this.button.setOnClickListener(null);
		this.button.setVisibility(View.INVISIBLE);

		this.progressBar.setMax(100);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			this.progressBar.setMin(0);
		}
		this.progressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * TODO Documentation
	 */
	@Override
	protected void onResume() {
		super.onResume();
		this.setProgress(0);

		// First, check if the user has internet
		if (this.hasInternet()) {

			// Then create the routematch object
			try {
				this.routeMatch = new RouteMatch("https://fnsb.routematch.com/feed/");
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return;
			}

			// Then, proceed to load the data
			this.loadData().start();

		} else {
			// Since the user doesn't have internet, let them know, and add an option to open internet settings via clicking the button
			this.progressBar.setVisibility(View.INVISIBLE);
			this.setMessage(R.string.nointernet);
			this.button.setText(R.string.open_internet_settins);
			this.button.setOnClickListener((click) -> {
				this.startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
				// Also, close this application
				this.finish();
			});
			this.button.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * TODO Documentation
	 *
	 * @return
	 */
	private boolean hasInternet() {
		NetworkInfo activeNetwork = ((ConnectivityManager) this.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	/**
	 * TODO Documentation
	 *
	 * @param progress
	 */
	private void setProgress(double progress) {
		this.runOnUiThread(() -> {
			int p = (int) Math.round(progress * 100);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				this.progressBar.setProgress(p, true);
			} else {
				this.progressBar.setProgress(p);
			}
		});
	}

	/**
	 * TODO Documentation
	 *
	 * @param message
	 */
	private void setMessage(int message) {
		this.runOnUiThread(() -> this.console.setText(message));
	}

	/**
	 * TODO Documentation
	 *
	 * @return
	 */
	private Thread loadData() {
		Thread t = new Thread(() -> {
			Log.d("loadData", "Loading routes from master schedule");
			this.setMessage(R.string.load_routes);

			this.routes = Route.generateRoutes(routeMatch);

			if (this.routes.length != 0) {
				this.setProgress(0.5d);
				this.setMessage(R.string.load_stops);

				// Load the stops in each route.
				for (int i = 0; i < this.routes.length; i++) {
					Route route = this.routes[i];
					route.stops = route.loadStops(this.routeMatch);

					this.setProgress(0.5d + (((i + 1d) / this.routes.length) / 2));
				}

				this.dataLoaded();
			} else {
				this.setMessage(R.string.no_routes);
				this.runOnUiThread(() -> {
					this.progressBar.setVisibility(View.INVISIBLE);
					this.button.setText(R.string.retry);
					this.button.setOnClickListener((click) -> {
						this.recreate();
					});
					this.button.setVisibility(View.VISIBLE);
				});
			}
		});
		t.setName("Splash-Network");
		return t;
	}

	/**
	 * TODO Documentation
	 */
	private void dataLoaded() {
		MapsActivity.routeMatch = this.routeMatch;
		MapsActivity.allRoutes = this.routes;
		this.startActivity(new Intent(this, MapsActivity.class));
		this.finish();
	}
}
