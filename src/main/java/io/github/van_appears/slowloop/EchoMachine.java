package io.github.van_appears.slowloop;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import javax.sound.sampled.*;

public class EchoMachine {
	
	public static final int BUFFER_LENGTH = 4410;
	public static final AudioFormat MONO_16BIT = new AudioFormat
		(AudioFormat.Encoding.PCM_SIGNED, EchoModel.SAMPLE_RATE, 16, 1, 2, EchoModel.SAMPLE_RATE, false);
	
	private TargetDataLine recordLine = null;
	private SourceDataLine playLine = null;
	private Queue<byte[]> toWrite = null;
	private boolean finished = false;
	private StreamWriter currentWriter = null;
	private CompletionListener completionListener;	
	private EchoModel echo1;
	private EchoModel echo2;

	public EchoMachine(EchoModel echo1, EchoModel echo2) {
		this.echo1 = echo1;
		this.echo2 = echo2;
	}
	
	public void start() {
		try {
			openInput();
			openOutput();
			readFromMicrophone();
			playThroughSpeakers();
		}
		catch(Exception e1) {
			e1.printStackTrace();
			exit();
		}
	}
	
	public void exit() { 			
		try {
			closeInputAndOutput();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		System.exit(1);
	}

	private void openInput() throws LineUnavailableException {
		DataLine.Info recordDataLine = new DataLine.Info(TargetDataLine.class, MONO_16BIT);
		recordLine = (TargetDataLine)AudioSystem.getLine(recordDataLine);
		recordLine.open(MONO_16BIT);
		recordLine.start();
	}
	
	private void openOutput() throws LineUnavailableException {
		DataLine.Info playDataLine = new DataLine.Info(SourceDataLine.class, MONO_16BIT);
		playLine = (SourceDataLine)AudioSystem.getLine(playDataLine);
 		playLine.open(MONO_16BIT);
 		playLine.start();
	}
	
	public void setCompletionListener(CompletionListener listener) {
		this.completionListener = listener;
	}
	
	private void readFromMicrophone() {
 		new Thread() {
 			public void run() {
		        while (!finished) {
	 		 		byte buffer[] = new byte[BUFFER_LENGTH];
			        int count = recordLine.read(buffer, 0, buffer.length) / 2;
			        for (int index=0; index<count; index++) {
			        	echo1.writeNext(buffer, index*2);
			        	echo2.writeNext(buffer, index*2);
			        }
		        }
			}
		}.start();		
	}
	
	private void playThroughSpeakers() {
 		new Thread() {
 			public void run() {
		        while (!finished) {
		        	byte[] buffer = new byte[BUFFER_LENGTH];
		        	for (int index=0; index<buffer.length/2; index++) {
		        		double val1 = echo1.readNext();
		        		double val2 = echo2.readNext();
		        		writeDataValue(buffer, index*2, val1 + val2);
		        	}
					playLine.write(buffer, 0, buffer.length);
					if (toWrite != null) {
			            toWrite.add(buffer);
					}
		        }
			}
		}.start();		
	}

	public void startRecording(double length, String filename) {
 		new Thread() {
 			public void run() {
 				try {
 					toWrite = new LinkedList<>(); 					
	 				File f = new File(filename + ".wav");
	 				System.out.println(f.getAbsolutePath());
	 				currentWriter = new StreamWriter(toWrite, length);
					AudioSystem.write(currentWriter, AudioFileFormat.Type.WAVE, f);
	 				while (!currentWriter.isFinished()) {
	 					Thread.sleep(1000);
	 				}
	 				toWrite = null;
	 				if (completionListener != null) {
	 					completionListener.completed();
	 				}
	 				currentWriter = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();		
	}
	
	public void stopRecording() {
		if (currentWriter != null) {
			currentWriter.terminate();
		}
	}
	
	public boolean isRecording() {
		return currentWriter != null;
	}
	
	private void closeInputAndOutput()
	throws LineUnavailableException {
		recordLine.stop();
		recordLine.close();
		
 		playLine.stop();
 		playLine.close();
	}
	
	public void writeDataValue(byte[] data, int dataReadPos, double value) {		
		int limited = (int)Math.min(Math.max(value, -32768), 32767);
		data[dataReadPos] = (byte)(limited & 0xff);
		data[dataReadPos + 1] = (byte)((limited >>> 8) & 0xff);
	}
	
	public interface CompletionListener {
		public void completed();
	}
}
