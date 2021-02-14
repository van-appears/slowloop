package io.github.van_appears.slowloop;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SlowLoopApplication {
	private static final DecimalFormat FORMAT = new DecimalFormat("0.00");
	private static final String DEFAULT_PROPERTIES_PREFIX = "slowloop";
	
	public static void main(String[] args) {
		new SlowLoopApplication();
	}
		
	private SlowLoopApplication() {
		EchoModel echo1 = new EchoModel();
		EchoModel echo2 = new EchoModel();
		EchoMachine echoBox = new EchoMachine(echo1, echo2);
		
		JFrame frame = new JFrame("Slow loop");
		new AppUIBuilder(frame.getContentPane())
		    .build(echo1, echo2, echoBox);
				
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				saveValues(frame.getContentPane(), DEFAULT_PROPERTIES_PREFIX);
			}
		});
		loadValues(frame.getContentPane(), DEFAULT_PROPERTIES_PREFIX);
		frame.setVisible(true);
		SwingUtilities.invokeLater(() -> {
			echoBox.start();
		});
	}
	
	public void loadValues(Container container, String filePrefix) {
		loadValues(container, new File(filePrefix + ".properties"));
	}
	
	public void loadValues(Container container, File file) {
		Component[] children = container.getComponents();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			reader.lines()
			    .forEach(x -> {
				    try {
					    String[] parts = x.split("=");
					    Component c = children[Integer.parseInt(parts[0])];
                        if (c instanceof JSlider) {
							((JSlider)c).setValue(Integer.parseInt(parts[1]));
						} else if (c instanceof JSpinner) {
							((JSpinner)c).setValue(Double.parseDouble(parts[1]));
						} else if (c instanceof JCheckBox) {
							((JCheckBox)c).setSelected(Boolean.parseBoolean(parts[1]));
						} else if (c instanceof JTextField) {
							((JTextField)c).setText(parts[1]);
						}
				    } catch (Exception e) {
					   // ah well.
				    }
			    });
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveValues(Container container, String filePrefix) {
		Component[] children = container.getComponents();
		StringBuffer buffer = new StringBuffer();
		for (int index=0; index<children.length; index++) {
			Component c = children[index];
			if (c instanceof JSlider) {
				buffer.append(index + "=" + ((JSlider)c).getValue());
				buffer.append(System.lineSeparator());
			} else if (c instanceof JSpinner) {
				buffer.append(index + "=" + ((JSpinner)c).getValue());
				buffer.append(System.lineSeparator());
			} else if (c instanceof JCheckBox) {
				buffer.append(index + "=" + ((JCheckBox)c).isSelected());
				buffer.append(System.lineSeparator());
			} else if (c instanceof JTextField) {
				buffer.append(index + "=" + ((JTextField)c).getText());
				buffer.append(System.lineSeparator());
			}
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePrefix + ".properties"))) {
			writer.write(buffer.toString());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private class AppUIBuilder {
		private Container container;
		private GridBagConstraints c;

		public AppUIBuilder(Container container) {
			this.container = container;
			this.c = new GridBagConstraints();
			container.setLayout(new GridBagLayout());
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(4, 4, 4, 4);
		}
		
		public void build(EchoModel echo1, EchoModel echo2, EchoMachine echoBox) {
			addLengthControl(0, "Echo 1", i -> echo1.setFrameLength(i), v -> echo1.clear());
			addSliderControl(1, "Speed:", 55, 1.0, 0.1, d -> echo1.setSpeed(d));
			addReverseControl(1, b -> echo1.setReverse(b));
			addWetDryControl(2, 75, d -> echo1.setWetMix(d), d -> echo1.setDryMix(d));
			addSeparator(4);
			
			addLengthControl(5, "Echo 2", i -> echo2.setFrameLength(i), v -> echo2.clear());
			addSliderControl(6, "Speed:", 55, 1.0, 0.1, d -> echo2.setSpeed(d));
			addReverseControl(6, b -> echo2.setReverse(b));
			addWetDryControl(7, 75, d -> echo2.setWetMix(d), d -> echo2.setDryMix(d));
			addSeparator(9);
			
			addSliderControl(10, "Level 1:", 80, 0.0, 1.25, d -> echo1.setPlayLevel(d));
			addSliderControl(11, "Level 2:", 0, 0.0, 1.25, d -> echo2.setPlayLevel(d));
			addRecordControl(12, echoBox);
		}

		private void addLengthControl(
		    int row,
			String labelText,
			Consumer<Integer> setFrameLength, // change to set seconds length
			Consumer<Void> clearFrames
		) {
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel
					(4.0, 0.1, EchoModel.MAX_LENGTH_SECONDS, 0.1);
			JSpinner length = new JSpinner(spinnerModel);
			JLabel titleLabel = new JLabel(labelText + " length:");
			length.setPreferredSize(new Dimension(100, 24));
			JButton clear = new JButton("Clear");
			
			c.gridwidth = 1;
			c.gridy = row;
			c.gridx = 0;
			container.add(titleLabel, c);
			c.gridx = 1;
			container.add(length, c);
			c.gridx = 3;
			container.add(clear, c);
			
			length.addChangeListener(l -> {
				int frameLength = (int)(44100.0 * (double)length.getValue());
				setFrameLength.accept(frameLength);
			});
			clear.addActionListener(a -> {
				clearFrames.accept(null);
			});
		}
	
		private void addSliderControl(
		    int row,
		    String labelText,
		    int initialPosition,
		    double min,
		    double max,
		    Consumer<Double> processor
		) {
			JLabel label = new JLabel("Test");
			JSlider slider = new JSlider(0, 100);
			slider.setPreferredSize(new Dimension(300, 24));
			
			c.gridwidth = 1;
			c.gridy = row;
			c.gridx = 0;
			container.add(label, c);
			c.gridx = 1;
			c.gridwidth = 2;
			container.add(slider, c);
			slider.addChangeListener(l -> {
				double val = 0.01 * (double)slider.getValue();
				double converted = min + ((max - min) * val);
				processor.accept(converted);
				label.setText(labelText + " " + FORMAT.format(converted));
			});
			slider.setValue(0);
			slider.setValue(initialPosition);
		}
		
		private void addWetDryControl(
			int row,
			int initialWetPosition,
			Consumer<Double> wetProcessor,
			Consumer<Double> dryProcessor
	    ) {
			String wetPrefix = "Wet mix: ";
			String dryPrefix = "Dry mix: ";
			JLabel wetLabel = new JLabel();
			JLabel dryLabel = new JLabel();
			JSlider wetSlider = new JSlider(0, 100);
			JSlider drySlider = new JSlider(0, 100);
			AtomicBoolean linked = new AtomicBoolean(true);
			JCheckBox connected = new JCheckBox("Wet/dry link");
			connected.setSelected(true);
			JButton button = new JButton("Invert wet/dry");
	
			c.gridwidth = 1;
			c.gridy = row;
			c.gridx = 0;
			container.add(wetLabel, c);
			c.gridx = 1;
			c.gridwidth = 2;
			container.add(wetSlider, c);
			c.gridx = 3;
			c.gridwidth = 1;
			container.add(connected, c);
			c.gridy = row + 1;
			c.gridx = 0;
			container.add(dryLabel, c);
			c.gridx = 1;
			c.gridwidth = 2;
			container.add(drySlider, c);
			c.gridx = 3;
			container.add(button, c);
	
			wetSlider.addChangeListener(e -> {
				double val = 0.01 * (double)wetSlider.getValue();
				wetLabel.setText(wetPrefix + FORMAT.format(val));
				wetProcessor.accept(val);
				if (linked.get()) {
					drySlider.setValue(100 - wetSlider.getValue());
				}
			});
			drySlider.addChangeListener(e -> {
				double val = 0.01 * (double)drySlider.getValue();
				dryLabel.setText(dryPrefix + FORMAT.format(val));
				dryProcessor.accept(val);
				if (linked.get()) {
					wetSlider.setValue(100 - drySlider.getValue());
				}
			});
			wetSlider.setValue(initialWetPosition);
			connected.addActionListener(a -> {
				linked.set(connected.isSelected());
			});
			button.addActionListener(a -> {
				int dryVal = drySlider.getValue();
				int wetVal = wetSlider.getValue();
				wetSlider.setValue(dryVal);
				drySlider.setValue(wetVal);
			});
		}
	
		private void addReverseControl(int row, Consumer<Boolean> reverseProcessor) {
			JCheckBox reversed = new JCheckBox("Reverse");
			reversed.addActionListener(l -> {
				boolean selected = reversed.isSelected();
				reverseProcessor.accept(selected);
			});
			
			c.gridy = row;
			c.gridx = 3;
			c.gridwidth = 1;
			container.add(reversed, c);
		}
		
		private void addSeparator(int row) {
			c.gridy = row;
			c.gridx = 0;
			c.gridwidth = 4;
			container.add(new JSeparator(), c);
		}
			
		private void addRecordControl(
			int row,
			EchoMachine echoMachine
		) {
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(180.0, 0.0, 600, 1.0);
			JSpinner length = new JSpinner(spinnerModel);
			JButton startCancel = new JButton("Record");
			JButton loadProperties = new JButton("Load properties");
			JTextField fileNameField = new JTextField();
			
			c.gridwidth = 1;
			c.gridy = row;
			c.gridx = 0;
			container.add(new JLabel("Recording length:"), c);
			c.gridx = 1;
			container.add(length, c);
			c.gridx = 3;
			container.add(startCancel, c);
			
			c.gridy = row + 1;
			c.gridx = 0;
			container.add(new JLabel("File prefix:"), c);
			c.gridx = 1;
			c.gridwidth = 2;
			container.add(fileNameField, c);
			c.gridx = 3;
			c.gridwidth = 1;
			container.add(loadProperties, c);
			
			startCancel.addActionListener(a -> {
				if (echoMachine.isRecording()) {
					startCancel.setText("Start");
					echoMachine.stopRecording();
				} else {
					startCancel.setText("Cancel");
					length.setEnabled(false);
					fileNameField.setEnabled(false);
					loadProperties.setEnabled(false);
					
					String outFile = fileNameField.getText().trim();
					if (!outFile.isEmpty()) { outFile += "-"; }
					outFile += System.currentTimeMillis();
					
					echoMachine.startRecording((double)length.getValue(), outFile);
					saveValues(container, outFile);
				}
			});
			loadProperties.addActionListener(a -> {
				JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Properties files", "properties");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(container);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	loadValues(container, chooser.getSelectedFile());
			    }
			});
			echoMachine.setCompletionListener(() -> {
				System.out.println("Completed");
				startCancel.setText("Start");
				length.setEnabled(true);
				fileNameField.setEnabled(true);
				loadProperties.setEnabled(true);
			});
		}
	}
}
