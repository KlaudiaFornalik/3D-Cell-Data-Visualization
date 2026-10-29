package wykres3d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
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

public class version3Culling {
	static HashMap<GridPoint, Coord3dColors> coordinates = new HashMap<>();
	static Coord3dColors[] pointsArray;
	static Quality q = Quality.Advanced();
	static SwingChartFactory factory = new SwingChartFactory();
	static Chart chart;

	static final int NO_COLOR = -1;

	public static void main(String[] args) {
		chart = factory.newChart(q);
		chart.setAnimated(false);

		chart.getView().setBackgroundColor(Color.WHITE);
		chart.addController(new UniformChartMouseController(chart));
		
		initColorGradient();
		
		JFrame frame = new JFrame("Visualisation");
		JPanel panel = new JPanel();
		JButton graphButton = new JButton("Draw the graph");
		JButton dataButton = new JButton("Choose a .dat file with data");
		JButton axisVisibilityButton = new JButton("Show/Hide the axis");

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(dataButton);
		buttonPanel.add(graphButton);
		buttonPanel.add(axisVisibilityButton);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(2, 1));
		controlPanel.add(buttonPanel);

		dataButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("only .dat file", "dat");
				fileChooser.setFileFilter(filter);
				int returnValue = fileChooser.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File wybranyPlik = fileChooser.getSelectedFile();

					if (!wybranyPlik.getName().toLowerCase().endsWith(".dat")) {
						JOptionPane.showMessageDialog(frame, "Try again. Choose a .dat file", "Error",
								JOptionPane.ERROR_MESSAGE);
					} else {
						wczytywanieDanych(wybranyPlik);
					}
				}
			}
		});

		graphButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generowanieWykresu();
				panel.repaint();
			}
		});

		axisVisibilityButton.addActionListener(new ActionListener() {
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

	static void wczytywanieDanych(File chosenFile) {
		coordinates.clear(); 
		try (Scanner scanner = new Scanner(chosenFile)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] values = line.split("\\s+"); // whitespaces

				if (values.length == 4) { // valid data with color
					int x = Integer.parseInt(values[0]);
					int y = Integer.parseInt(values[1]);
					int z = Integer.parseInt(values[2]);
					int color = Integer.parseInt(values[3]);
					GridPoint key = new GridPoint((int)x, (int)y, (int)z);
					//String key = x + "," + y + "," + z;
					coordinates.put(key, new Coord3dColors((float) x, (float) y, (float) z, color));
				}
				else if (values.length == 3) { // valid data without color
					int x = Integer.parseInt(values[0]);
					int y = Integer.parseInt(values[1]);
					int z = Integer.parseInt(values[2]);
					GridPoint key = new GridPoint((int)x, (int)y, (int)z);
					coordinates.put(key, new Coord3dColors((float) x, (float) y, (float) z));
				}
			}
			JOptionPane.showMessageDialog(null, "Loaded " + coordinates.size() + " points!");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "File read error!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	
	static void generowanieWykresu() {
		pointsArray = coordinates.values().toArray(new Coord3dColors[0]);

		chart.getScene().getGraph().getAll().clear();

		int minColor = Integer.MAX_VALUE;
		int maxColor = Integer.MIN_VALUE;
		boolean hasColorColumn = false;

		for (Coord3dColors point : coordinates.values()) {
			if (point.color == NO_COLOR) {
				break;
			} else {
				hasColorColumn = true;
				if (point.color < minColor) minColor = point.color;
				if (point.color > maxColor) maxColor = point.color;
			}
		}
			
		float cubeSize = 1.0f; 
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

		// Center content
		BoundingBox3d dataBounds = chart.getScene().getGraph().getBounds();
		chart.getView().lookToBox(dataBounds);
		chart.getView().shoot();
		chart.render();
	}
	
	static Color mapColor(int value, int min, int max) {
		float normalized = (float)(value - min) / (max - min);
		float reversed = 1.0f - normalized;
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
		if (max == min) return colorGradient[0];
		float norm = (float)(value - min) / (max - min);
		int idx = (int)(norm * (COLOR_RESOLUTION - 1));
		return colorGradient[Math.min(Math.max(idx, 0), COLOR_RESOLUTION - 1)];
	}

	static int numberOfCubes = 0;
	
	private static final int[][] CUBE_FACES = {
	    {0, 1, 2, 3}, // Back face
	    {4, 5, 6, 7}, // Front face
	    {0, 1, 5, 4}, // Bottom face
	    {2, 3, 7, 6}, // Top face
	    {1, 2, 6, 5}, // Right face
	    {0, 3, 7, 4}  // Left face
	};
	
	static Composite createCube(Coord3dColors center, float size, Color color) {
		numberOfCubes++;
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


		for (int[] face : CUBE_FACES) {
			if (!faceIsAdjacent(face, CUBE_FACES, center)) {
				Polygon side = new Polygon();
				for (int index : face) {
					side.add(new Point(vertices[index], color));
				}
				cube.add(side);
				side.setWireframeColor(Color.BLACK);
			}
		}
		return cube;
	}

	private static boolean faceIsAdjacent(int[] face, int[][] faces, Coord3dColors currentPoint) {
		if (Arrays.equals(face, faces[0])) { // Back face 
			return checkForNeighbor(currentPoint.x, currentPoint.y, currentPoint.z - 1);
		} else if (Arrays.equals(face, faces[1])) { // Front face 
			return checkForNeighbor(currentPoint.x, currentPoint.y, currentPoint.z + 1);
		} else if (Arrays.equals(face, faces[2])) { // Bottom face 
			return checkForNeighbor(currentPoint.x, currentPoint.y - 1, currentPoint.z);	
		} else if (Arrays.equals(face, faces[3])) { // Top face 
			return checkForNeighbor(currentPoint.x, currentPoint.y + 1, currentPoint.z);
		} else if (Arrays.equals(face, faces[4])) { // Right face 
			return checkForNeighbor(currentPoint.x + 1, currentPoint.y, currentPoint.z);
		} else if (Arrays.equals(face, faces[5])) { // Left face 
			return checkForNeighbor(currentPoint.x - 1, currentPoint.y, currentPoint.z);
		}
		return false;
	}

	private static Boolean checkForNeighbor(float x, float y, float z) {
		GridPoint key = new GridPoint((int)x, (int)y, (int)z);
		return coordinates.containsKey(key);
	}

	static class Coord3dColors extends Coord3d {
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

	static class GridPoint {
	    final int x, y, z;

	    GridPoint(int x, int y, int z) {
	        this.x = x;
	        this.y = y;
	        this.z = z;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof GridPoint)) return false;
	        GridPoint that = (GridPoint) o;
	        return x == that.x && y == that.y && z == that.z;
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(x, y, z);
	    }
	}

}




