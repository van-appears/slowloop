package io.github.van_appears.util;

import java.io.File;

public class VolumeAdjust {
	private static final String DIRECTORY = "output/adjust";
	private static final double SCALE_LOW = 1.5;
	private static final double SCALE_TOP = 0.9;

	public static void main(String[] args) {
		new VolumeAdjust().run();
	}

	private void run() {
		File directory = new File(DIRECTORY);
		if (!directory.isDirectory()) {
			System.out.println("Folder argument required");
			System.exit(1);
		}
		File[] toProcess = directory
			.listFiles((d, n) -> n.endsWith(".wav"));
		for (File wavFile : toProcess) {
			try {
				adjust(wavFile);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
	
	private void adjust(File file) throws Exception {
		try (SoundDataStream loaded = new SoundDataStream(file)) {
			int channelCount = loaded.getChannelCount();
			int frameCount = loaded.getFrameCount();

			for (int channel=0; channel<channelCount; channel++) {
				boolean left = channel == 0;
				for (int index=0; index<frameCount; index++) {
					double value = (double)loaded.getDataValue(index, left);
					double rescale = Math.abs((double)value / 32768.0);
					rescale = SCALE_LOW + (rescale * (SCALE_TOP - SCALE_LOW));
					value = Math.round(value * rescale);
					loaded.setDataValue((int)value, index, left);
				}
			}
		
			String[] split = file.getName().split("\\.");
			split[split.length - 2] = split[split.length - 2] + "_adjusted"; 
			File newFile = new File(file.getParent(), String.join(".", split));
			loaded.write(newFile);
		}
	}
}
