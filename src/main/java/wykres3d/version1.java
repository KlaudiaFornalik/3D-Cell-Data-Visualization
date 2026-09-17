package wykres3d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.*;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.SwingChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.primitives.*;

public class version1 {
	static ArrayList<Coord3d> coordinates = new ArrayList<>();
	static Coord3d[] pointsArray;
	static Quality q = Quality.Advanced();
	static SwingChartFactory factory = new SwingChartFactory();
	static Chart chart = factory.newChart(q);

	public static void main(String[] args) {
		// random data for referance
		/*
		 * float x, y, z; Random rand = new Random(); for (int i = 0; i < 1000; i++) { x
		 * = rand.nextFloat() * 10; y = rand.nextFloat() * 10; z = rand.nextFloat() *
		 * 10; coordinates.add(new Coord3d(x, y, z)); } for (int i = 0; i <
		 * coordinates.size(); i++) { System.out.println( "x = " + coordinates.get(i).x
		 * + " y = " + coordinates.get(i).y + " z = " + coordinates.get(i).z); }
		 */

		chart.getView().setBackgroundColor(Color.WHITE);
		// chart.addMouse();
		chart.addController(new UniformChartMouseController(chart));

		// swing frame
		JFrame frame = new JFrame("wizualizacja");
		JPanel panel = new JPanel();
		JButton wykresButton = new JButton("Narysuj wykres");
		JButton daneButton = new JButton("Wybierz plik .dat z danymi");
		JButton widocznoscOsiButton = new JButton("Pokaż/Ukryj osie wykresu");

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(daneButton);
		buttonPanel.add(wykresButton);
		buttonPanel.add(widocznoscOsiButton);

		JLabel label = new JLabel("Ścieżka do pliku:");
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setVerticalTextPosition(JLabel.CENTER);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(2, 1));
		controlPanel.add(buttonPanel);
		controlPanel.add(label);

		daneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				FileNameExtensionFilter filter = new FileNameExtensionFilter("tylko pliki .dat", "dat");
				fileChooser.setFileFilter(filter);
				int returnValue = fileChooser.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File wybranyPlik = fileChooser.getSelectedFile();
					// JOptionPane.showMessageDialog(frame, "Wybrano plik: " +
					// wybranyPlik.getAbsolutePath());
					label.setForeground(java.awt.Color.RED);
					label.setText(wybranyPlik.getAbsolutePath());

					if (!wybranyPlik.getName().toLowerCase().endsWith(".dat")) {
						JOptionPane.showMessageDialog(frame, "Spróbuj ponownie. Wybierz plik .dat", "Błąd",
								JOptionPane.ERROR_MESSAGE);
					} else {
						wczytywanieDanych(wybranyPlik);
					}
				}
			}
		});
		// draw graph
		wykresButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				label.setForeground(java.awt.Color.GREEN);
				generowanieWykresu();
				panel.repaint();
			}
		});

		widocznoscOsiButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chart.getView().setAxisDisplayed(!chart.getView().isAxisDisplayed());
				chart.render();
				panel.repaint();
			}
		});

		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(600, 600));
		panel.add((JComponent) chart.getCanvas(), BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(800, 800));
		frame.setLayout(new FlowLayout());
		frame.add(controlPanel);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		// frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		// frame.toFront();
		// frame.requestFocus();
		// frame.setAlwaysOnTop(false);
	}

	// load data
	static void wczytywanieDanych(File wybranyPlik) {

		coordinates.clear(); // Clear previous data
		try (Scanner scanner = new Scanner(wybranyPlik)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] values = line.split("\\s+"); // whitespaces

				if (values.length == 3) { //valid data
					int x = Integer.parseInt(values[0]);
					int y = Integer.parseInt(values[1]);
					int z = Integer.parseInt(values[2]);
					coordinates.add(new Coord3d((float) x, (float) y, (float) z));
				}
			}
			JOptionPane.showMessageDialog(null, "Wczytano " + coordinates.size() + " punktów!");
			/*
			 * for (int i = 0; i < coordinates.size(); i++) {
			 * System.out.print(coordinates.get(i).x + " " + coordinates.get(i).y + " " +
			 * coordinates.get(i).z + "\n"); }
			 */
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Błąd odczytu pliku!", "Błąd", JOptionPane.ERROR_MESSAGE);
		}

	}

	// draw graph
	static void generowanieWykresu() {
		pointsArray = coordinates.toArray(new Coord3d[0]); // Coord3d must be an array

		chart.getScene().getGraph().getAll().clear();
		/*
		 * while (!chart.getScene().getGraph().getAll().isEmpty()) {
		 * chart.getScene().getGraph().remove(chart.getScene().getGraph().getAll().get(0
		 * )); }
		 */

		float cubeSize = 1.0f; // Adjust size based on data spacing, I have int
		ArrayList<Composite> cubes = new ArrayList<>();

		for (Coord3d point : pointsArray) {
			cubes.add(createCube(point, cubeSize));
		}
		chart.getScene().add(cubes);

		chart.getView().setBackgroundColor(Color.WHITE);

		// center content
		BoundingBox3d dataBounds = chart.getScene().getGraph().getBounds();
		chart.getView().lookToBox(dataBounds);
		chart.getView().shoot();

		chart.render();

	}

	static Composite createCube(Coord3d center, float size) {
		Composite cube = new Shape();
		float halfSize = size / 2;

		Coord3d[] vertices = new Coord3d[] { new Coord3d(center.x - halfSize, center.y - halfSize, center.z - halfSize),
				new Coord3d(center.x + halfSize, center.y - halfSize, center.z - halfSize),
				new Coord3d(center.x + halfSize, center.y + halfSize, center.z - halfSize),
				new Coord3d(center.x - halfSize, center.y + halfSize, center.z - halfSize),
				new Coord3d(center.x - halfSize, center.y - halfSize, center.z + halfSize),
				new Coord3d(center.x + halfSize, center.y - halfSize, center.z + halfSize),
				new Coord3d(center.x + halfSize, center.y + halfSize, center.z + halfSize),
				new Coord3d(center.x - halfSize, center.y + halfSize, center.z + halfSize) };

		int[][] faces = { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 0, 1, 5, 4 }, { 2, 3, 7, 6 }, { 1, 2, 6, 5 },
				{ 0, 3, 7, 4 } };

		for (int[] face : faces) {
			Polygon side = new Polygon();
			for (int index : face) {
				side.add(new Point(vertices[index], Color.GRAY));
			}
			cube.add(side);
			side.setWireframeColor(Color.BLACK);
		}

		return cube;
	}
}

/*class UniformChartMouseController extends AWTCameraMouseController {
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
