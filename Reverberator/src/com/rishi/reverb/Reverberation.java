package com.rishi.reverb;

import java.io.*;
import java.util.Arrays;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

/**************************************************************************************************************************************************************
 *  
 *  This class has the main processing for the Reverb. The process uses the JavaSound API. It uses the metaphor of a mixing console for the components of the API.
 *  'Mixer is the notion of a sound device. A 'line' is one of the strips in a mixing console for the audio i/o.
 * 
 * The steps in the process are as follows:
 *  
 *  First Step: Declaring Class variables. This includes the Mixer, Line(Clip in this case) and the ByteArrayOutputStream.
 *  			
 *  			Inside the 'reverb' method, The mixer is first instantiated to be assigned to the primary sound driver of the system.
 *  			Then the line is set to 'Clip', which is a data line into which audio data can be loaded prior to playback.
 *  			The input audio file is read and stored into an AudioInputStream. Which is then converted to an array for data manipulation.
 *
 *	Second Step: The processing of obtained array is done. The process has 4 Comb Filters in parallel and 2 All Pass Filters in series.
 *
 *	Third Step: The processed array is converted to a ByteInputStream which is assigned to the Clip and the start method for the clip is issued.
 *
 *************************************************************************************************************************************************************/

public class Reverberation {
	/*	
	 *	First Step:
	 *	Declaring Class variables to be used in the program
	 */
	public static Mixer mixer;
	public static Clip clip;
	private ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
	public static float sampleRate;	
	
	public void reverb(String inputFile, float delayinMilliSeconds, float decayFactor, int mixPercent) throws FileNotFoundException, UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException{
	
		//Getting the info on the audio mixers installed on the system
		Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
		
		//Setting up the lineInfo object for info about 'Clip' which is the dataLine used in this program
		DataLine.Info lineInfo = new DataLine.Info(Clip.class, null);
		
		//Checking for mixer compatible with the dataLine(Clip) and allocating that mixer to the mixer object
		for (int i = 0; i < aInfos.length; i++)  
        {  
            if (AudioSystem.getMixer(aInfos[i]).isLineSupported(lineInfo))  {
            	mixer = AudioSystem.getMixer(aInfos[i]);
            	break;
            }
        }
		
		//Setting up clip
		clip = (Clip) mixer.getLine(lineInfo);
		
		//Input audio file is read
		File audioSource = new File(inputFile);
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioSource);
		
		//Setting up the audioFormat instance for the input audio file supplied
		AudioFormat audioFormat = audioInputStream.getFormat();
		sampleRate = audioFormat.getSampleRate();

		//Getting a byte array from the input stream
		AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, byteArrayOutputStream);
		audioInputStream = null;
		byte[] byteArray = byteArrayOutputStream.toByteArray();
		byteArrayOutputStream = null;
		int bufferSize = byteArray.length * audioFormat.getFrameSize();

		//This step converts the byte array into and array of samples. This is for simplicity in data manipulation required for reverb	
		float[] samples = new float[bufferSize];
		int slen = SampleDataRetrieval.unpack(byteArray, samples, byteArray.length, audioFormat);
	
		/*	
		 * Second Step:
		 *	Method calls for the 4 Comb Filters in parallel. Defined at the bottom
		 */
		float[] combFilterSamples1 = combFilter(samples, bufferSize, delayinMilliSeconds, decayFactor, sampleRate);
		float[] combFilterSamples2 = combFilter(samples, bufferSize, (delayinMilliSeconds - 11.73f), (decayFactor - 0.1313f), sampleRate);
		float[] combFilterSamples3 = combFilter(samples, bufferSize, (delayinMilliSeconds + 19.31f), (decayFactor - 0.2743f), sampleRate);
		float[] combFilterSamples4 = combFilter(samples, bufferSize, (delayinMilliSeconds - 7.97f), (decayFactor - 0.31f), sampleRate);
		
		//Adding the 4 Comb Filters
		float[] outputComb = new float[bufferSize];
		for( int i = 0; i < bufferSize; i++) 
		{
			outputComb[i] = ((combFilterSamples1[i] + combFilterSamples2[i] + combFilterSamples3[i] + combFilterSamples4[i])) ;
		}	   	
	
		//Deallocating individual Comb Filter array outputs
		combFilterSamples1 = null;
		combFilterSamples2 = null;
		combFilterSamples3 = null;
		combFilterSamples4 = null;
	
		//Algorithm for Dry/Wet Mix in the output audio
		float [] mixAudio = new float[bufferSize];
		for(int i=0; i<bufferSize; i++)
			mixAudio[i] = ((100 - mixPercent) * samples[i]) + (mixPercent * outputComb[i]); 

		
		//Method calls for 2 All Pass Filters. Defined at the bottom
		float[] allPassFilterSamples1 = allPassFilter(mixAudio, bufferSize, sampleRate);
		float[] allPassFilterSamples2 = allPassFilter(allPassFilterSamples1, bufferSize, sampleRate);
		
		/*
		 * Third Step:
		 * Sample array converted back to byte array
		 */
		byte[] finalAudioSamples = new byte[bufferSize];
		SampleDataRetrieval.pack(allPassFilterSamples2, finalAudioSamples, slen, audioFormat);
	
		//Byte array converted to Input Stream
		ByteArrayInputStream bais = new ByteArrayInputStream(finalAudioSamples);
	    AudioInputStream outputAis = new AudioInputStream(bais, audioFormat,outputComb.length/ audioFormat.getFrameSize());
	  
	    //Clip open and start method issued
	    clip.open(outputAis);
		clip.start();
	}
	

	//Method for Comb Filter
	public float[] combFilter(float[] samples, int samplesLength, float delayinMilliSeconds, float decayFactor, float sampleRate)
	{
		//Calculating delay in samples from the delay in Milliseconds. Calculated from number of samples per millisecond
		int delaySamples = (int) ((float)delayinMilliSeconds * (sampleRate/1000));
		
		float[] combFilterSamples = Arrays.copyOf(samples, samplesLength);
	
		//Applying algorithm for Comb Filter
		for (int i=0; i<samplesLength - delaySamples; i++)
		{
			combFilterSamples[i+delaySamples] += ((float)combFilterSamples[i] * decayFactor);
		}
	return combFilterSamples;
	}
	
	//Method for All Pass Filter
	public float[] allPassFilter(float[] samples, int samplesLength, float sampleRate)
	{
		int delaySamples = (int) ((float)89.27f * (sampleRate/1000)); // Number of delay samples. Calculated from number of samples per millisecond
		float[] allPassFilterSamples = new float[samplesLength];
		float decayFactor = 0.131f;

		//Applying algorithm for All Pass Filter
		for(int i=0; i<samplesLength; i++)
			{
			allPassFilterSamples[i] = samples[i];
		
			if(i - delaySamples >= 0)
				allPassFilterSamples[i] += -decayFactor * allPassFilterSamples[i-delaySamples];
		
			if(i - delaySamples >= 1)
				allPassFilterSamples[i] += decayFactor * allPassFilterSamples[i+20-delaySamples];
			}
		
	
		//This is for smoothing out the samples and normalizing the audio. Without implementing this, the samples overflow causing clipping of audio
		float value = allPassFilterSamples[0];
		float max = 0.0f;
		
		for(int i=0; i < samplesLength; i++)
		{
			if(Math.abs(allPassFilterSamples[i]) > max)
				max = Math.abs(allPassFilterSamples[i]);
		}
		
		for(int i=0; i<allPassFilterSamples.length; i++)
		{
			float currentValue = allPassFilterSamples[i];
			value = ((value + (currentValue - value))/max);

			allPassFilterSamples[i] = value;
		}		
	return allPassFilterSamples;
	}
	
	//This method is to stop audio playback
	public void stopTheMusic()
	{
		clip.stop();
	}
}