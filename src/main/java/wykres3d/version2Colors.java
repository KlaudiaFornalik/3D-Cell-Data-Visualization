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

public class version2Colors {
	static ArrayList<Coord3dColors> coordinates = new ArrayList<>();
	static Coord3dColors[] pointsArray;
	static Quality q = Quality.Advanced();
	static SwingChartFactory factory = new SwingChartFactory();
	static Chart chart = factory.newChart(q);

	static final int NO_COLOR = -1;

	public static void main(String[] args) {

		chart.getView().setBackgroundColor(Color.WHITE);
		chart.addController(new UniformChartMouseController(chart));
		
		initColorGradient();
		
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
		frame.setVisible(true);
	}

	// load data
	static void wczytywanieDanych(File wybranyPlik) {

		coordinates.clear(); // Clear previous data
		try (Scanner scanner = new Scanner(wybranyPlik)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] values = line.split("\\s+"); // whitespaces

				if (values.length == 4) { //valid data
					int x = Integer.parseInt(values[0]);
					int y = Integer.parseInt(values[1]);
					int z = Integer.parseInt(values[2]);
					int color = Integer.parseInt(values[3]);
					coordinates.add(new Coord3dColors((float) x, (float) y, (float) z, color));
				}
				else if (values.length == 3) { //valid data
					int x = Integer.parseInt(values[0]);
					int y = Integer.parseInt(values[1]);
					int z = Integer.parseInt(values[2]);
					coordinates.add(new Coord3dColors((float) x, (float) y, (float) z));
				}
			}
			JOptionPane.showMessageDialog(null, "Wczytano " + coordinates.size() + " punktów!");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Błąd odczytu pliku!", "Błąd", JOptionPane.ERROR_MESSAGE);
		}

	}

	// draw graph
	static void generowanieWykresu() {
		pointsArray = coordinates.toArray(new Coord3dColors[0]); // Coord3dColors must be an array

		chart.getScene().getGraph().getAll().clear();
		int minColor = Integer.MAX_VALUE;
		int maxColor = Integer.MIN_VALUE;
		
		boolean hasColorColumn = false;

		for (Coord3dColors point : coordinates) {
			if (point.color == NO_COLOR) {
				break;
			}
			else {
				hasColorColumn = true;
				if (point.color < minColor) minColor = point.color;
				if (point.color > maxColor) maxColor = point.color;
			}
		}
			
		float cubeSize = 1.0f; // Adjust size based on data spacing, I have int
		
		Composite allCubes = new Shape();

		for (Coord3dColors point : pointsArray) {
			Color cubeColor = hasColorColumn
			    ? mapColorCached(point.color, minColor, maxColor)
			    : Color.GRAY;
			Composite cube = createCube(point, cubeSize, cubeColor);
			for (Drawable face : cube.getDrawables()) {
			    allCubes.add(face); 
			}
		}
		chart.getScene().add(allCubes);


		chart.getView().setBackgroundColor(Color.WHITE);

		// center content
		BoundingBox3d dataBounds = chart.getScene().getGraph().getBounds();
		chart.getView().lookToBox(dataBounds);
		chart.getView().shoot();

		chart.render();

	}
	
	static Color mapColor(int value, int min, int max) {
		float normalized = (float)(value - min) / (max - min);// 0-1 scale
		
		float reversed = 1.0f - normalized;
		// from orange (255, 165, 0) to dark blue (0, 0, 50)
		int r = (int)(255 * (1 - reversed));
		int g = (int)(165 * (1 - reversed));
		int b = (int)(50 * reversed);

		return new Color(r, g, b);
	}
	
	static final int COLOR_RESOLUTION = 256;
	static Color[] colorGradient = new Color[COLOR_RESOLUTION];

	static void initColorGradient() {
		for (int i = 0; i < COLOR_RESOLUTION; i++) {
			float normalized = i / (float)(COLOR_RESOLUTION - 1);
			float reversed = 1.0f - normalized;

			int r = (int)(255 * (1 - reversed));
			int g = (int)(165 * (1 - reversed));
			int b = (int)(50 * reversed);
			colorGradient[i] = new Color(r, g, b);
		}
	}

	static Color mapColorCached(int value, int min, int max) {
		if (max == min) return colorGradient[0]; // avoid div by zero
		float norm = (float)(value - min) / (max - min);
		int idx = (int)(norm * (COLOR_RESOLUTION - 1));
		return colorGradient[Math.min(Math.max(idx, 0), COLOR_RESOLUTION - 1)];
	}


	static Composite createCube(Coord3dColors center, float size, Color color) {
		Composite cube = new Shape();
		float halfSize = size / 2;

		Coord3dColors[] vertices = new Coord3dColors[] {
			new Coord3dColors(center.x - halfSize, center.y - halfSize, center.z - halfSize),
			new Coord3dColors(center.x + halfSize, center.y - halfSize, center.z - halfSize),
			new Coord3dColors(center.x + halfSize, center.y + halfSize, center.z - halfSize),
			new Coord3dColors(center.x - halfSize, center.y + halfSize, center.z - halfSize),
			new Coord3dColors(center.x - halfSize, center.y - halfSize, center.z + halfSize),
			new Coord3dColors(center.x + halfSize, center.y - halfSize, center.z + halfSize),
			new Coord3dColors(center.x + halfSize, center.y + halfSize, center.z + halfSize),
			new Coord3dColors(center.x - halfSize, center.y + halfSize, center.z + halfSize)
		};

		int[][] faces = {
			{ 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 0, 1, 5, 4 },
			{ 2, 3, 7, 6 }, { 1, 2, 6, 5 }, { 0, 3, 7, 4 }
		};

		for (int[] face : faces) {
			Polygon side = new Polygon();
			for (int index : face) {
				side.add(new Point(vertices[index], color));
			}
			cube.add(side);
			//side.setWireframeColor(Color.BLACK);
			side.setWireframeDisplayed(false);
			
			// border
	        for (int i = 0; i < face.length; i++) {
	            int next = (i + 1) % face.length;
	            Coord3dColors start = vertices[face[i]];
	            Coord3dColors end = vertices[face[next]];

	            LineStrip border = new LineStrip();
	            border.add(new Point(start, Color.BLACK));
	            border.add(new Point(end, Color.BLACK));
	            cube.add(border);
	        }

		}

		return cube;
	}

	
	static class Coord3dColors extends Coord3d{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int color; 
		
		Coord3dColors(float x, float y, float z, int color) {
	        super(x, y, z); 
	        this.color = color;
	    }
		Coord3dColors(float x, float y, float z) {
	        super(x, y, z); 
	        this.color = NO_COLOR;
	    }
	}
}



