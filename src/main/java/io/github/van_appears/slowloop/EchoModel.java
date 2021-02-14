package io.github.van_appears.slowloop;

public class EchoModel {
	public static final int SAMPLE_RATE = 44100;
	public static final double MAX_LENGTH_SECONDS = 10.0;
	
	private double[] data = new double[(int)(SAMPLE_RATE * MAX_LENGTH_SECONDS)];
	private int frameLength = 88200;
	public double readPos = 0;
	public int writePos = 0;
	private double lastValue = 0;
	
	private double speed = 0.99;
	private double wetMix = 1.0;
	private double dryMix = 0.0;
	private double playLevel = 1.0;
	private boolean reverse = false;
	
	public void setFrameLength(int length) {
		if (readPos > length) {
			readPos = 0;
		}
		if (writePos > length) {
			writePos = 0;
		}
		this.frameLength = length;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}
	
	public void setWetMix(double wetMix) {
		this.wetMix = wetMix;
	}

	public void setDryMix(double dryMix) {
		this.dryMix = dryMix;
	}

	public void setPlayLevel(double playLevel) {
		this.playLevel = playLevel;
	}
	
	public void clear() {
		data = new double[(int)(SAMPLE_RATE * MAX_LENGTH_SECONDS)];
	}

	public void writeNext(byte[] buffer, int bufferPos) {
		int value = (int)buffer[bufferPos + 1] * 256;
		value = value + ((256 + (int)buffer[bufferPos]) % 256);
		double next = 0.0;
		next += dryMix * (double)data[writePos];
		next += wetMix * (double)value;
		data[writePos] = (int)Math.min(Math.max(next, -32768), 32767);
		writePos = (writePos + 1) % frameLength; 
	}
	
	public double readNext() {
		double portion = reverse
			? Math.ceil(readPos) - readPos
			: readPos - Math.floor(readPos);
		int afterPos = reverse
			? (int)readPos - 1
			: ((int)readPos + 1) % frameLength;
		if (afterPos < 0) { afterPos += frameLength; }
		double before = data[(int)readPos];
		double after = data[afterPos];
		double val = before + (portion * (after - before));
		
		if (reverse) {
			readPos -= speed;
		    if (readPos < 0) {
		        readPos += frameLength;
		    }
		} else {
			readPos = (readPos + speed) % frameLength;		    
		}
		
		double diff = writePos - readPos; 
		if (diff > 0 && diff < 10) {
			double scale = diff / 10;
			val = lastValue + ((val - lastValue) * scale);	
		}
		lastValue = val;
		return (int)(playLevel * val);
	}
}
