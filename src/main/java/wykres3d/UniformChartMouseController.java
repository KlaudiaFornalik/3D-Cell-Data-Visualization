package wykres3d;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.AWTMouseUtilities;
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.rendering.view.View;

/*public class UniformChartMouseController extends AWTCameraMouseController {
	private Chart chart;

	public UniformChartMouseController(Chart chart) {
		super(chart);
		this.chart = chart;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (threadController != null)
			threadController.stop();

		float factor = (e.getWheelRotation() < 0) ? 0.9f : 1.1f;

		View view = chart.getView();
		BoundingBox3d currentBounds = view.getBounds();

		Coord3d center = currentBounds.getCenter();
		float xRange = (currentBounds.getXmax() - currentBounds.getXmin()) / 2;
		float yRange = (currentBounds.getYmax() - currentBounds.getYmin()) / 2;
		float zRange = (currentBounds.getZmax() - currentBounds.getZmin()) / 2;

		BoundingBox3d newBounds = new BoundingBox3d(center.x - xRange * factor, // Xmin
				center.x + xRange * factor, // Xmax
				center.y - yRange * factor, // Ymin
				center.y + yRange * factor, // Ymax
				center.z - zRange * factor, // Zmin
				center.z + zRange * factor // Zmax
		);

		view.setBoundManual(newBounds);
		chart.render();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		prevMouse = new Coord2d(e.getX(), e.getY());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Coord2d mouse = new Coord2d(e.getX(), e.getY()); // current coordinates
		Coord2d move = mouse.sub(prevMouse); // subtracts the coordinates stored in prevMouse (mousePressed) from the
												// current mouse coordinates (mouse)

		View view = chart.getView();
		BoundingBox3d currentBounds = view.getBounds(); // currently visible region

		float rangeX = currentBounds.getXmax() - currentBounds.getXmin();
		float rangeY = currentBounds.getYmax() - currentBounds.getYmin();

		float baseFactor = 0.02f;
        float zoomFactor = (rangeX + rangeY) / 2.0f * baseFactor;

        float dx = -move.x * zoomFactor;
        float dy = move.y * zoomFactor;

		// Left-click: Rotate
		if (AWTMouseUtilities.isLeftDown(e)) {
			chart.getView().rotate(move.div(100));
		}
		// Right-click: Shift (pan)
		else if (AWTMouseUtilities.isRightDown(e)) {
			BoundingBox3d newBounds = new BoundingBox3d(
					currentBounds.getXmin() + dx, currentBounds.getXmax() + dx,
					currentBounds.getYmin() + dy, currentBounds.getYmax() + dy, currentBounds.getZmin(),
					currentBounds.getZmax()
					);

			view.setBoundManual(newBounds);
			
		}

		prevMouse = mouse;
		chart.render();
	}
	
}*/

public class UniformChartMouseController extends AWTCameraMouseController {
	private final Chart chart;
	private Coord2d prevMouse;
	private long lastRenderTime = 0;

	public UniformChartMouseController(Chart chart) {
		super(chart);
		this.chart = chart;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		float factor = (e.getWheelRotation() < 0) ? 0.9f : 1.1f;

		View view = chart.getView();
		BoundingBox3d currentBounds = view.getBounds();

		Coord3d center = currentBounds.getCenter();
		float xRange = (currentBounds.getXmax() - currentBounds.getXmin()) / 2;
		float yRange = (currentBounds.getYmax() - currentBounds.getYmin()) / 2;
		float zRange = (currentBounds.getZmax() - currentBounds.getZmin()) / 2;

		BoundingBox3d newBounds = new BoundingBox3d(
			center.x - xRange * factor, center.x + xRange * factor,
			center.y - yRange * factor, center.y + yRange * factor,
			center.z - zRange * factor, center.z + zRange * factor
		);

		view.setBoundManual(newBounds);
		chart.getView().shoot(); // rerender
	}

	@Override
	public void mousePressed(MouseEvent e) {
		prevMouse = new Coord2d(e.getX(), e.getY());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		long now = System.currentTimeMillis();
		//if (now - lastRenderTime < 16) return; // 60fps
		if (now - lastRenderTime < 30) return; // 30
		lastRenderTime = now;

		Coord2d mouse = new Coord2d(e.getX(), e.getY());
		Coord2d move = mouse.sub(prevMouse);

		View view = chart.getView();
		BoundingBox3d currentBounds = view.getBounds();

		float rangeX = currentBounds.getXmax() - currentBounds.getXmin();
		float rangeY = currentBounds.getYmax() - currentBounds.getYmin();
		float zoomFactor = (rangeX + rangeY) / 2.0f * 0.02f;

		float dx = -move.x * zoomFactor;
		float dy = move.y * zoomFactor;

		if (AWTMouseUtilities.isLeftDown(e)) {
			// Rotate
			view.rotate(move.div(100));
		} else if (AWTMouseUtilities.isRightDown(e)) {
			// Pan
			BoundingBox3d newBounds = new BoundingBox3d(
				currentBounds.getXmin() + dx, currentBounds.getXmax() + dx,
				currentBounds.getYmin() + dy, currentBounds.getYmax() + dy,
				currentBounds.getZmin(), currentBounds.getZmax()
			);
			view.setBoundManual(newBounds);
		}

		prevMouse = mouse;
		chart.getView().shoot();
	}
}

