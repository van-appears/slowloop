package io.github.van_appears.util;

import java.io.*;
import javax.sound.sampled.*;

public class SoundDataStream extends AudioInputStream {

	private byte[] soundData;
	private int bufferPosition;
	private long remainingFrames;
	
	public SoundDataStream(File file) throws Exception {
		super(new ByteArrayInputStream(new byte[0]),
		      AudioSystem.getAudioFileFormat(file).getFormat(),
		      (long)AudioSystem.getAudioFileFormat(file).getFrameLength());

		remainingFrames = 0;
		bufferPosition = 0;
		loadSound(file);
	}

	private void loadSound(File aFile) throws Exception {
		int remainingBytes = 0;
		byte[] data1 = new byte[0];
		byte[] data2 = new byte[0];

		try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(aFile)) {
			remainingBytes = 1;
			do {
				int available = inputStream.available();
				data1 = new byte[data2.length + available];
				System.arraycopy(data2, 0, data1, 0, data2.length);
				remainingBytes = inputStream.read(data1, data2.length, available);
				data2 = data1;
			}
			while (remainingBytes > 0);
			this.soundData = data2;
		}
	}
	
	public int getChannelCount() {
		return this.getFormat().getChannels();
	}

	public int available() {
		return (int)(remainingFrames * getFormat().getFrameSize());
	}

	public int read(byte[] data, int offset, int length) throws IOException {
		int	remainingLength = 0;
		int framesRead = 0;
		int bytesRead = 0;

		int constrainedLength = Math.min(available(), length);
		remainingLength = constrainedLength;
		while (remainingLength > 0) {
			int numBytesToCopyNow = soundData.length - bufferPosition;
			numBytesToCopyNow = Math.min(numBytesToCopyNow, remainingLength);
			System.arraycopy(soundData, bufferPosition, data, offset, numBytesToCopyNow);
			remainingLength -= numBytesToCopyNow;
			offset += numBytesToCopyNow;
			bufferPosition = (bufferPosition + numBytesToCopyNow) % soundData.length;
		}

		framesRead = constrainedLength / getFormat().getFrameSize();
		if (remainingFrames != AudioSystem.NOT_SPECIFIED) {
			remainingFrames -= framesRead;
		}

		bytesRead = constrainedLength;
		if (framesRead == 0) {
			bytesRead = -1;
		}

		return bytesRead;
	}

	public synchronized void write(File file) throws IOException {
		remainingFrames = getFrameLength();
		AudioSystem.write(this, AudioFileFormat.Type.WAVE, file);
	}
	
	public long getFrameLength() {
		return (long)getFrameCount();
	}
	
	public int getFrameCount() {
		return soundData.length / getFormat().getFrameSize();
	}

	public int getDataValue(int aPosition, boolean left) {
		int frameSize = getFormat().getFrameSize();
		int channels = getFormat().getChannels();
		int extra = (left || channels == 1) ? 0 : (frameSize / channels);
		int pos = ((aPosition * frameSize) + extra) % soundData.length;
		int value = 0;

		if (!getFormat().isBigEndian()) {
			value = (int)soundData[pos + 1] * 256;
			value = value + ((256 + (int)soundData[pos]) % 256);
		}
		else {
			value = (int)soundData[pos] * 256;
			value = value + ((256 + (int)soundData[pos + 1]) % 256);
		}
		return value;
	}

	public void setDataValue(int value, int position, boolean left) {
		int frameSize = getFormat().getFrameSize();
		int channels = getFormat().getChannels();
		int extra = (left || channels == 1) ? 0 : (frameSize / channels);
		int pos = (position * frameSize) + extra;

		if (!getFormat().isBigEndian()) {
			soundData[pos] = (byte)(value & 0xff);
			soundData[pos + 1] = (byte)((value >>> 8) & 0xff);
		}
		else {
			soundData[pos] = (byte)((value >>> 8) & 0xff);
			soundData[pos + 1] = (byte)(value & 0xff);
		}
	}
}
