package io.github.van_appears.slowloop;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.github.van_appears.slowloop.EchoMachine.RecordType;

public class SlowLoop {
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.00");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private static final String DEFAULT_PROPERTIES_PREFIX = "slowloop";
	
	private JSpinner echo1Length = new JSpinner(new SpinnerNumberModel
			(4.0, 0.1, EchoModel.MAX_LENGTH_SECONDS, 0.1));
	private JButton echo1Clear = new JButton("Clear");
	private JLabel echo1SpeedLabel = new JLabel();
	private JSlider echo1Speed = new JSlider(0, 100);
	private JCheckBox echo1Reverse = new JCheckBox("Reverse");
	private JLabel echo1WetLabel = new JLabel();
	private JSlider echo1Wet = new JSlider(0, 100);
	private JLabel echo1DryLabel = new JLabel();
	private JSlider echo1Dry = new JSlider(0, 100);
	private JCheckBox echo1WetDryLink = new JCheckBox("Wet/dry link");
	private JButton echo1WetDryInvert = new JButton("Invert wet/dry");
	private JLabel echo1LevelLabel = new JLabel();
	private JSlider echo1Level = new JSlider(0, 100);

	private JSpinner echo2Length = new JSpinner(new SpinnerNumberModel
			(4.0, 0.1, EchoModel.MAX_LENGTH_SECONDS, 0.1));
	private JButton echo2Clear = new JButton("Clear");
	private JLabel echo2SpeedLabel = new JLabel();
	private JSlider echo2Speed = new JSlider(0, 100);
	private JCheckBox echo2Reverse = new JCheckBox("Reverse");
	private JLabel echo2WetLabel = new JLabel();
	private JSlider echo2Wet = new JSlider(0, 100);
	private JLabel echo2DryLabel = new JLabel();
	private JSlider echo2Dry = new JSlider(0, 100);
	private JCheckBox echo2WetDryLink = new JCheckBox("Wet/dry link");
	private JButton echo2WetDryInvert = new JButton("Invert wet/dry");
	private JLabel echo2LevelLabel = new JLabel();
	private JSlider echo2Level = new JSlider(0, 100);

	private JComboBox<LineSettings.LineSetting> inputs = new JComboBox<>();
	private JComboBox<LineSettings.LineSetting> outputs = new JComboBox<>();
	private JButton refresh = new JButton("Refresh");
	private JButton restart = new JButton("Restart");
	
	private JRadioButton useOutput = new JRadioButton("Record output");
	private JRadioButton useInput = new JRadioButton("Record input");
	private JSpinner recordLength = new JSpinner(new SpinnerNumberModel
			(180.0, 1.0, StreamWriter.MAX_LENGTH_SECONDS, 1.0));
	private JLabel timerLabel = new JLabel("0");
	private JTextField recordPrefix = new JTextField();
	private JButton mute = new JButton("Mute both");
	private JCheckBox clearOnRecord = new JCheckBox("Clear on record");
	private JButton record = new JButton("Record");
	private JButton load = new JButton("Load properties");

	private EchoModel echo1;
	private EchoModel echo2;
	private EchoMachine echoMachine;
	private LineSettings lineSettings;
	private TimerThread timerThread;

	public static void main(String[] args) {
		new SlowLoop();
	}

	private SlowLoop() {
		echo1 = new EchoModel();
		echo2 = new EchoModel();
		echoMachine = new EchoMachine(echo1, echo2);
		lineSettings = new LineSettings();

		JFrame frame = new JFrame("Slow loop");
		buildUI(frame.getContentPane());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				saveDefaultValues();
			}
		});
		loadDefaultValues();
		frame.setVisible(true);
		SwingUtilities.invokeLater(() -> {
			setInputAndOutput();
			echoMachine.start();
		});
	}

	private void buildUI(Container container) {
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2, 4, 2, 4);

		setInputsAndOutputs();
		layoutLabels(container, c);
		layoutControls(container, c);
		layoutButtons(container, c);
		layoutSeparators(container, c);
		connectListeners(container);
	}
	
	private void setInputsAndOutputs() {
		inputs.removeAllItems();
		outputs.removeAllItems();
		for (LineSettings.LineSetting item : lineSettings.getInputs()) {
			inputs.addItem(item);
		}
		for (LineSettings.LineSetting item : lineSettings.getOutputs()) {
			outputs.addItem(item);
		}
		if (inputs.getItemCount() == 0 || outputs.getItemCount() == 0) {
			// a bit rough...
			System.exit(1);
		}
	}
	
	private void setInputAndOutput() {
		echoMachine.reconnect(
			(LineSettings.LineSetting)inputs.getSelectedItem(),
			(LineSettings.LineSetting)outputs.getSelectedItem()
		);
	}

	private void layoutLabels(Container container, GridBagConstraints c) {
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		container.add(new JLabel("Input:"), c);
		c.gridy = 1;
		container.add(new JLabel("Output:"), c);
		c.gridy = 3;
		container.add(new JLabel("Echo 1 length:"), c);
		c.gridy = 4;
		container.add(echo1SpeedLabel, c);
		c.gridy = 5;
		container.add(echo1WetLabel, c);
		c.gridy = 6;
		container.add(echo1DryLabel, c);
		c.gridy = 8;
		container.add(new JLabel("Echo 2 length:"), c);
		c.gridy = 9;
		container.add(echo2SpeedLabel, c);
		c.gridy = 10;
		container.add(echo2WetLabel, c);
		c.gridy = 11;
		container.add(echo2DryLabel, c);
		c.gridy = 13;
		container.add(echo1LevelLabel, c);
		c.gridy = 14;
		container.add(echo2LevelLabel, c);
		c.gridy = 17;
		container.add(new JLabel("Recording length:"), c);
		c.gridy = 18;
		container.add(new JLabel("File prefix:"), c);
		c.gridy = 17;
		c.gridx = 2;
		container.add(timerLabel, c);
	}

	private void layoutControls(Container container, GridBagConstraints c) {
		c.gridx = 1;
		c.gridwidth = 1;
		c.gridy = 0;
		container.add(inputs, c);
		c.gridy = 1;
		container.add(outputs, c);
		c.gridy = 3;
		container.add(echo1Length, c);
		c.gridy = 8;
		container.add(echo2Length, c);
		c.gridy = 17;
		container.add(recordLength, c);
		c.gridwidth = 2;
		c.ipadx = 100;
		c.gridy = 4;
		container.add(echo1Speed, c);
		c.gridy = 5;
		container.add(echo1Wet, c);
		c.gridy = 6;
		container.add(echo1Dry, c);
		c.gridy = 9;
		container.add(echo2Speed, c);
		c.gridy = 10;
		container.add(echo2Wet, c);
		c.gridy = 11;
		container.add(echo2Dry, c);
		c.gridy = 13;
		container.add(echo1Level, c);
		c.gridy = 14;
		container.add(echo2Level, c);
		c.gridy = 18;
		container.add(recordPrefix, c);
		c.ipadx = 0;
	}

	private void layoutButtons(Container container, GridBagConstraints c) {
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 0;
		container.add(refresh, c);
		c.gridy = 1;
		container.add(restart, c);
		c.gridy = 3;
		container.add(echo1Clear, c);
		c.gridy = 4;
		container.add(echo1Reverse, c);
		c.gridy = 5;
		container.add(echo1WetDryLink, c);
		c.gridy = 6;
		container.add(echo1WetDryInvert, c);
		c.gridy = 8;
		container.add(echo2Clear, c);
		c.gridy = 9;
		container.add(echo2Reverse, c);
		c.gridy = 10;
		container.add(echo2WetDryLink, c);
		c.gridy = 11;
		container.add(echo2WetDryInvert, c);
		c.gridy = 13;
		container.add(mute, c);
		c.gridy = 16;
		container.add(clearOnRecord, c);
		c.gridy = 17;
		container.add(record, c);
		c.gridy = 18;
		container.add(load, c);
		c.gridy = 16;
		c.gridx = 0;
		container.add(useOutput, c);
		c.gridx = 1;
		container.add(useInput, c);	
	}

	private void layoutSeparators(Container container, GridBagConstraints c) {
		c.gridwidth = 4;
		c.gridx = 0;
		c.gridy = 2;
		container.add(new JSeparator(), c);
		c.gridy = 7;
		container.add(new JSeparator(), c);
		c.gridy = 12;
		container.add(new JSeparator(), c);
		c.gridy = 15;
		container.add(new JSeparator(), c);
	}

	private void connectListeners(Container container) {
		refresh.addActionListener(a -> setInputsAndOutputs());
		restart.addActionListener(a -> setInputAndOutput());
		echo1Clear.addActionListener(a -> echo1.clear());
		echo2Clear.addActionListener(a -> echo2.clear());
		echo1Reverse.addActionListener(a -> echo1.setReverse(echo1Reverse.isSelected()));
		echo2Reverse.addActionListener(a -> echo2.setReverse(echo2Reverse.isSelected()));
		connectLengthControl(echo1Length, i -> echo1.setFrameLength(i));
		connectLengthControl(echo2Length, i -> echo2.setFrameLength(i));
		connectSliderToLabel(echo1Speed, echo1SpeedLabel, "Speed:", 55, 1.0, 0.1, d -> echo1.setSpeed(d));
		connectSliderToLabel(echo2Speed, echo2SpeedLabel, "Speed:", 55, 1.0, 0.1, d -> echo2.setSpeed(d));
		connectSliderToLabel(echo1Level, echo1LevelLabel, "Level 1:", 67, 0.0, 1.495, d -> echo1.setPlayLevel(d));
		connectSliderToLabel(echo2Level, echo2LevelLabel, "Level 2:", 0, 0.0, 1.495, d -> echo2.setPlayLevel(d));
		connectSliderToLabel(echo1Wet, echo1WetLabel, "Wet mix:", 75, 0.0, 1.0, d -> echo1.setWetMix(d));
		connectSliderToLabel(echo2Wet, echo2WetLabel, "Wet mix:", 75, 0.0, 1.0, d -> echo2.setWetMix(d));
		connectSliderToLabel(echo1Dry, echo1DryLabel, "Dry mix:", 25, 0.0, 1.0, d -> echo1.setDryMix(d));
		connectSliderToLabel(echo2Dry, echo2DryLabel, "Dry mix:", 25, 0.0, 1.0, d -> echo2.setDryMix(d));
		connectLinkedWetDry(echo1Wet, echo1Dry, echo1WetDryLink);
		connectLinkedWetDry(echo2Wet, echo2Dry, echo2WetDryLink);
		connectInvertWetDry(echo1Wet, echo1Dry, echo1WetDryInvert);
		connectInvertWetDry(echo2Wet, echo2Dry, echo2WetDryInvert);
		
		ButtonGroup group = new ButtonGroup();
		group.add(useOutput);
		group.add(useInput);
		useOutput.setSelected(true);
		useOutput.addActionListener(a -> echoMachine.setRecordType(RecordType.Output));
		useInput.addActionListener(a -> echoMachine.setRecordType(RecordType.Input));

		connectMuteControl();
		connectRecordControls();
		connectLoadControl(container);
	}

	private void connectSliderToLabel(
			JSlider slider,
			JLabel label,
			String labelText,
			int initialPosition,
			double min,
			double max,
			Consumer<Double> processor
	) {
		slider.addChangeListener(l -> {
			double val = 0.01 * (double)slider.getValue();
			double converted = min + ((max - min) * val);
			processor.accept(converted);
			label.setText(labelText + " " + NUMBER_FORMAT.format(converted));
		});
		slider.setValue(0);
		slider.setValue(initialPosition);
	}

	private void connectLengthControl(JSpinner length, Consumer<Integer> processor) {
		length.addChangeListener(c -> {
			processor.accept((int)(
				(double)EchoModel.SAMPLE_RATE * (double)length.getValue()
			));
		});
	}

	private void connectLinkedWetDry(JSlider slider1, JSlider slider2, JCheckBox link) {
		slider1.addChangeListener(c -> {
			if (link.isSelected()) {
				slider2.setValue(100 - slider1.getValue());
			}
		});
		slider2.addChangeListener(c -> {
			if (link.isSelected()) {
				slider1.setValue(100 - slider2.getValue());
			}
		});
	}

	private void connectInvertWetDry(JSlider slider1, JSlider slider2, JButton action) {
		action.addActionListener(a -> {
			int value1 = slider1.getValue();
			int value2 = slider2.getValue();
			slider1.setValue(value2);
			slider2.setValue(value1);
		});
	}
	
	private void connectMuteControl() {
		AtomicBoolean isMuted = new AtomicBoolean(false);
		AtomicInteger level1Value = new AtomicInteger(0);
		AtomicInteger level2Value = new AtomicInteger(0);			
		mute.addActionListener(l -> {
			boolean currentlyMuted = isMuted.get();
			if (currentlyMuted) {
				mute.setText("Mute both");
				echo1Level.setValue(level1Value.get());
				echo2Level.setValue(level2Value.get());
			} else {
				mute.setText("Unmute");
				level1Value.set(echo1Level.getValue());
				level2Value.set(echo2Level.getValue());
				echo1Level.setValue(0);
				echo2Level.setValue(0);
			}
			isMuted.set(!currentlyMuted);
			echo1Level.setEnabled(currentlyMuted);
			echo2Level.setEnabled(currentlyMuted);
		});
	}
	
	private void connectRecordControls() {
		record.addActionListener(a -> {
			if (echoMachine.isRecording()) {
				record.setText("Start");
				echoMachine.stopRecording();
			} else {
				record.setText("Cancel");
				recordLength.setEnabled(false);
				recordPrefix.setEnabled(false);
				inputs.setEnabled(false);
				outputs.setEnabled(false);
				refresh.setEnabled(false);
				restart.setEnabled(false);
				load.setEnabled(false);
				
				String outFile = recordPrefix.getText().trim();
				if (!outFile.isEmpty()) { outFile += "-"; }
				outFile += DATE_FORMAT.format(new Date());
				
				if (clearOnRecord.isSelected()) {
					echo1.clear();
					echo2.clear();
				}
				echoMachine.startRecording((double)recordLength.getValue(), outFile);
				timerThread = new TimerThread();
				saveValues(new File(outFile + ".properties"));
			}
		});
		echoMachine.setCompletionListener(() -> {
			System.out.println("Completed");
			timerThread.stopTimer();
			record.setText("Start");
			recordLength.setEnabled(true);
			recordPrefix.setEnabled(true);
			inputs.setEnabled(true);
			outputs.setEnabled(true);
			refresh.setEnabled(true);
			restart.setEnabled(true);
			load.setEnabled(true);
		});
	}
	
	private void connectLoadControl(Container container) {
		load.addActionListener(a -> {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Properties files", "properties");
			chooser.setFileFilter(filter);
			int returnVal = chooser.showOpenDialog(container);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				loadValues(chooser.getSelectedFile());
			}
		});
	}

	private void saveDefaultValues() {
		saveValues(new File(DEFAULT_PROPERTIES_PREFIX + ".properties"));
	}

	private void saveValues(File file) {
		try {
			saveProperties(file);
		} catch (Exception e) {
			System.out.println("Failed to save properties: " + e.getMessage());
		}
	}

	private void saveProperties(File file) throws Exception {
		Properties properties = new Properties();
		properties.setProperty("echo1Length", String.valueOf(echo1Length.getValue()));
		properties.setProperty("echo2Length", String.valueOf(echo2Length.getValue()));
		properties.setProperty("echo1Speed", String.valueOf(echo1Speed.getValue()));
		properties.setProperty("echo2Speed", String.valueOf(echo2Speed.getValue()));
		properties.setProperty("echo1Wet", String.valueOf(echo1Wet.getValue()));
		properties.setProperty("echo2Wet", String.valueOf(echo2Wet.getValue()));
		properties.setProperty("echo1Dry", String.valueOf(echo1Dry.getValue()));
		properties.setProperty("echo2Dry", String.valueOf(echo2Dry.getValue()));
		properties.setProperty("echo1Level", String.valueOf(echo1Level.getValue()));
		properties.setProperty("echo2Level", String.valueOf(echo2Level.getValue()));
		properties.setProperty("echo1WetDryLink", String.valueOf(echo1WetDryLink.isSelected()));
		properties.setProperty("echo2WetDryLink", String.valueOf(echo2WetDryLink.isSelected()));
		properties.setProperty("echo1Reverse", String.valueOf(echo1Reverse.isSelected()));
		properties.setProperty("echo2Reverse", String.valueOf(echo2Reverse.isSelected()));
		properties.setProperty("clearOnRecord", String.valueOf(clearOnRecord.isSelected()));
		properties.setProperty("recordLength", String.valueOf(recordLength.getValue()));
		properties.setProperty("recordPrefix", recordPrefix.getText());
		properties.setProperty("useInput", String.valueOf(useInput.isSelected()));
		properties.store(new FileOutputStream(file), "");
	}
	
	private void loadDefaultValues() {
		loadValues(new File(DEFAULT_PROPERTIES_PREFIX + ".properties"));
	}

	private void loadValues(File file) {
		try {
			loadProperties(file);
		} catch (Exception e) {
			System.out.println("Failed to load properties");
			e.printStackTrace();
		}
	}
	
	private void loadProperties(File file) throws Exception {
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		echo1Length.setValue(Double.parseDouble(properties.getProperty("echo1Length")));
		echo2Length.setValue(Double.parseDouble(properties.getProperty("echo2Length")));
		echo1Speed.setValue(Integer.parseInt(properties.getProperty("echo1Speed")));
		echo2Speed.setValue(Integer.parseInt(properties.getProperty("echo2Speed")));
		echo1Wet.setValue(Integer.parseInt(properties.getProperty("echo1Wet")));
		echo2Wet.setValue(Integer.parseInt(properties.getProperty("echo2Wet")));
		echo1Dry.setValue(Integer.parseInt(properties.getProperty("echo1Dry")));
		echo2Dry.setValue(Integer.parseInt(properties.getProperty("echo2Dry")));
		echo1Level.setValue(Integer.parseInt(properties.getProperty("echo1Level")));
		echo2Level.setValue(Integer.parseInt(properties.getProperty("echo2Level")));
		echo1WetDryLink.setSelected(Boolean.parseBoolean(properties.getProperty("echo1WetDryLink")));
		echo2WetDryLink.setSelected(Boolean.parseBoolean(properties.getProperty("echo2WetDryLink")));
		echo1Reverse.setSelected(Boolean.parseBoolean(properties.getProperty("echo1Reverse")));
		echo2Reverse.setSelected(Boolean.parseBoolean(properties.getProperty("echo2Reverse")));
		clearOnRecord.setSelected(Boolean.parseBoolean(properties.getProperty("clearOnRecord")));
		recordLength.setValue(Double.parseDouble(properties.getProperty("recordLength")));
		recordPrefix.setText(properties.getProperty("recordPrefix"));
		if (Boolean.parseBoolean("useInput")) {
			useInput.setSelected(true);
		} else {
			useOutput.setSelected(true);
		}
	}
	
	private class TimerThread extends Thread {
		boolean running = true;
		int timer = 0;

		public TimerThread() {
			timerLabel.setText("0");
			start();
		}
		public void stopTimer() {
			running = false;
		}
		public void run() {
			while(running) {
				try {
					sleep(1000);
					timer++;
					timerLabel.setText(String.valueOf(timer));
				} catch (Exception e) {}
			}
		}
	}
}
