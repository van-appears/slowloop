package io.github.van_appears.util;

import java.io.File;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class DeClick {
	private static final String DIRECTORY = "output/test";
	
	private SplineInterpolator interpolator = new SplineInterpolator();

	public static void main(String[] args) {
		new DeClick().run();
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
				for (int index=2; index<frameCount-2; index++) {
					double valueM2 = (double)loaded.getDataValue(index-2, left);
					double valueM1 = (double)loaded.getDataValue(index-1, left);
					double valueP1 = (double)loaded.getDataValue(index, left);
					double valueP2 = (double)loaded.getDataValue(index+1, left);
					
					boolean down1 = valueM2 > valueM1;
					double expect1 = valueM1 + valueM1 - valueM2;
					boolean down2 = valueP1 > valueP2;
					double expect2 = valueP1 + valueP1 - valueP2;

					if (down1 && down2 && (valueP1 - valueM1 > 100))  {
						System.out.println("> " + index + " (" + ((double)index/44100.0) + ") " + valueM2 + " " + valueM1 + " " + down1 + " " + valueP1 + " " + valueP2 + " " + down2);
					} else if (!down1 && !down2 && (valueM1 - valueP1 > 100)) {
						System.out.println("< " + index + " (" + ((double)index/44100.0) + ") " + valueM2 + " " + valueM1 + " " + down1 + " " + valueP1 + " " + valueP2 + " " + down2);
					} else if (Math.abs(expect1 - valueP1) > 400 && Math.abs(expect2 - valueM1) > 400) {
						System.out.println("X " + index + " (" + ((double)index/44100.0) + ") " + valueM2 + " " + valueM1 + " " + down1 + " " + valueP1 + " " + valueP2 + " " + down2);
						index = fix(loaded, index, left);
					}
				}
			}

			String[] split = file.getName().split("\\.");
			split[split.length - 2] = split[split.length - 2] + "_adjusted"; 
			File newFile = new File(file.getParent(), String.join(".", split));
			loaded.write(newFile);
		}
	}
	
	private int fix(SoundDataStream loaded, int pos, boolean left) {
		double valueM19 = (double)loaded.getDataValue(pos-19, left);
		double valueM18 = (double)loaded.getDataValue(pos-18, left);
		double valueM17 = (double)loaded.getDataValue(pos-17, left);
		double valueP17 = (double)loaded.getDataValue(pos+17, left);
		double valueP18 = (double)loaded.getDataValue(pos+18, left);
		double valueP19 = (double)loaded.getDataValue(pos+19, left);
		
		PolynomialSplineFunction spline = interpolator.interpolate(
			new double[]{-19.0, -18.0, -17.0, 17.0, 18.0, 19.0},
			new double[]{valueM19, valueM18, valueM17, valueP17, valueP18, valueP19});
		for (int index=-19; index<=19; index++) {
			loaded.setDataValue((int)spline.value((double)index), pos + index, left);
		}
		return pos + 20;
	}
}

