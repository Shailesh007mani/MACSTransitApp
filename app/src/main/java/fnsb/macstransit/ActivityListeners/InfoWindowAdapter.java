package fnsb.macstransit.ActivityListeners;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import fnsb.macstransit.MapsActivity;

/**
 * Created by Spud on 2019-11-01 for the project: MACS Transit.
 * <p>
 * For the license, view the file titled LICENSE at the root of the project
 *
 * @version 1.0
 * @since Beta 7
 */
public class InfoWindowAdapter implements com.google.android.gms.maps.GoogleMap.InfoWindowAdapter {

	/**
	 * The maps activity that this adapter corresponds to.
	 */
	private MapsActivity activity;

	/**
	 * Constructor for the info window adapter.
	 *
	 * @param activity The maps activity that this info window adapter corresponds to.
	 */
	public InfoWindowAdapter(MapsActivity activity) {
		this.activity = activity;
	}

	/**
	 * Provides a custom info window for a marker. If this method returns a view,
	 * it is used for the entire info window. If you change this view after this method is called,
	 * those changes will not necessarily be reflected in the rendered info window.
	 * If this method returns null , the default info window frame will be used,
	 * with contents provided by getInfoContents(Marker).
	 *
	 * @param marker The marker for which an info window is being populated.
	 * @return A custom info window for marker, or null to use the default info window frame with custom contents.
	 * (In this case its always going to be null, sorry).
	 */
	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

	/**
	 * Provides custom contents for the default info window frame of a marker.
	 * This method is only called if getInfoWindow(Marker) first returns null.
	 * If this method returns a view, it will be placed inside the default info window frame.
	 * If you change this view after this method is called,
	 * those changes will not necessarily be reflected in the rendered info window.
	 * If this method returns null, the default rendering will be used instead.
	 * <p><br >
	 * <p>
	 * Credit where credits due:
	 * this custom implementation was modified off of a stackoverflow post by user Hiren Patel - https://stackoverflow.com/users/4233197/hiren-patel.
	 * <p>
	 * The original post can be found at this link:
	 * https://stackoverflow.com/questions/13904651/android-google-maps-v2-how-to-add-marker-with-multiline-snippet.
	 *
	 * @param marker The marker for which an info window is being populated.
	 * @return A custom view to display as contents in the info window for marker,
	 * or null to use the default content rendering instead.
	 */
	@Override
	public View getInfoContents(Marker marker) {
		LinearLayout info = new LinearLayout(this.activity);
		info.setOrientation(LinearLayout.VERTICAL);

		TextView title = new TextView(this.activity);
		title.setTextColor(Color.BLACK);
		title.setGravity(android.view.Gravity.CENTER);
		title.setTypeface(null, android.graphics.Typeface.BOLD);
		title.setText(marker.getTitle());

		TextView snippet = new TextView(this.activity);
		snippet.setTextColor(Color.GRAY);
		snippet.setText(marker.getSnippet());
		snippet.setMaxLines(15);

		info.addView(title);
		if (!snippet.getText().equals("") && snippet.getText() != null) {
			info.addView(snippet);
		}
		return info;
	}
}
