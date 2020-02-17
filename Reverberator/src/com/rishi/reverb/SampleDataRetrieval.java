package com.rishi.reverb;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import static java.lang.Math.ceil;
import static java.lang.Math.pow;

/******************************************************************************************************************************************
 * 
 * This class in for 'Unpacking' and 'Packing' of the audio data in arrays. 
 * 	The purpose of the class is to facilitate easy data manipulations by converting the audio data into array of samples .
 *	
 *	'Unpack' method converts to the byte array obtained from audio stream into an array of sample data.
 *
 *	'Pack' method converts the sample data back to byte array. This byte array is then used to create an input stream for audio playback.
 * 
 ******************************************************************************************************************************************/

public class SampleDataRetrieval {
	
	/**
     *	Converts:from a byte array to an audio sample array.
     *	
     *	Input Arguments:
     * 	bytes - the byte array, filled from the AudioInputStream
     * 	samples - an array to fill up with audio samples.
     * 	blen - length of the byte array of the input audio file
     * 	audioFormat - Audio format of the source audio
     * 
     * 	Return value:	
     * 	The number of valid audio samples converted.
     * 
     * 	The float array supplied is filled with the converted sample data.
	 */
	
	
	public static int unpack(byte[]      bytes,
            float[]     samples,
            int         blen,
            AudioFormat audioFormat) {
			int   bitsPerSample = audioFormat.getSampleSizeInBits();
			int  bytesPerSample = bytesPerSample(bitsPerSample);
			Encoding   encoding = audioFormat.getEncoding();
			double    fullScale = fullScale(bitsPerSample);
	
    int i = 0;
    int s = 0;
    while (i < blen)
    	{
    	long temp = unpackBits(bytes, i, bytesPerSample);
        float sample = 0f;
        
        if (encoding == Encoding.PCM_SIGNED) {
            temp = extendSign(temp, bitsPerSample);
            sample = (float) (temp / fullScale);

        } else if (encoding == Encoding.PCM_UNSIGNED) {
            temp = signUnsigned(temp, bitsPerSample);
            sample = (float) (temp / fullScale);
        }
        samples[s] = sample;

        i += bytesPerSample;
        s++;
    	}
    return i;
	}
	
	/**
     * Converts: from an audio sample array to a byte array
     * 
     * Input Arguments:
     * 	samples - an array of audio samples to encode.
     *  bytes - an array to fill up with bytes.
     *  slen - the return value of 'unpack'
     *	audioFormat - the destination AudioFormat.
     * 
     * Return value:
     * 	The number of valid bytes converted.
     *	
     *	The byte array supplied is filled with the sample data converted back to bytes.
     */
	
	public static int pack(float[]     samples,
            byte[]      bytes,
            int         slen,
            AudioFormat audioFormat) {
			int   bitsPerSample = audioFormat.getSampleSizeInBits();
			int  bytesPerSample = bytesPerSample(bitsPerSample);
			Encoding   encoding = audioFormat.getEncoding();
			double    fullScale = fullScale(bitsPerSample);

			int i = 0;
			int s = 0;
			while (s < slen) {
				float sample = samples[s];
				long temp = 0L;

				if (encoding == Encoding.PCM_SIGNED) {
	                temp = (long) (sample * fullScale);

	            } else if (encoding == Encoding.PCM_UNSIGNED) {
	                temp = (long) (sample * fullScale);
	                temp = unsignSigned(temp, bitsPerSample);
	            }   
				
	            packBits(bytes, i, temp, bytesPerSample);

				i += bytesPerSample;
				s++;
			}
			return i;
	}
	
    //	This is done for the PCM-Signed encoding. 
	//	The calling method is converting the byte data into long. So the twos-complement sign must be extended.
	//	There are 64 bits per long. So the bits in the sample are first shifted to the left and then the right-shift will do the filling.
    public static long extendSign(long temp, int bitsPerSample) {
        int extensionBits = 64 - bitsPerSample;
        return (temp << extensionBits) >> extensionBits;
    }
    
    //	Computes the largest magnitude representable by the audio format,
    //	with pow(2.0, bitsPerSample - 1).
    //
    //This is used for scaling the float array to the -1.0f to 1.0f range
    public static double fullScale(int bitsPerSample) {
        return pow(2.0, bitsPerSample - 1);
    }
    
    //	The UnSigned values are converted to Signed values.
    //	UnSigned values are simply offset such that the 'fullScale' corresponds to zero value
    //	So subtract the fullScale from the value of the sample and later scale it.
    private static long signUnsigned(long temp, int bitsPerSample) {
        return temp - (long) fullScale(bitsPerSample);
    }

	//	Computes the block-aligned bytes per sample of the audio format,
    //	with {(int) ceil(bitsPerSample / 8.0)}.

    //	Round towards the ceiling because formats that allow bit depths
    //	in non-integral multiples of 8 typically pad up to the nearest
    //	integral multiple of 8.

    public static int bytesPerSample(int bitsPerSample) {
        return (int) ceil(bitsPerSample / 8.0);
    }
            
    private static long unpackBits(byte[]  bytes,
            int     i,
            int     bytesPerSample) {
    	switch (bytesPerSample) {
    		case  1: return unpack8Bit(bytes, i);
    		case  2: return unpack16Bit(bytes, i);
    		default: return 1;
    		}
    }
    
    /*
     * 	The byte array contains the sample frames split up and all in a line. 
     * 	The WAV files are encoded in little-endian, the least significant byte is earlier in the order.
     * 
     * 	Bitwise AND each byte with the mask 0xFF (which is 0b1111_1111) to avoid sign extension when the byte is automatically promoted
     */
    
    //	This method converts the byte data into a long
    //	When the data is stored in 8-bit encoding, the conversion is straightforward. Each element in byte array corresponds to each sample.
    private static long unpack8Bit(byte[] bytes, int i) {
    	return bytes[i] & 0xffL;
    }

    //	This method converts the byte data into a long
    //	When the data is stored in 16-bit encoding, the bytes need to be bit shifted into position, and Bitwise OR to put the bytes together.
    private static long unpack16Bit(byte[]  bytes,
             int     i) {
    		return ((bytes[i    ] & 0xffL)
    				| ((bytes[i + 1] & 0xffL) << 8L));
    			}
    
    private static void packBits(byte[]  bytes,
            int     i,
            long    temp,
            int     bytesPerSample) {
    	switch (bytesPerSample) {
    		case  1: pack8Bit(bytes, i, temp);
    			break;
    		case  2: pack16Bit(bytes, i, temp);
    			break;
    		default: ;
    			break;
    	}
    }
    
    /*
     * 	Following methods just reverse the processing done for the 'unpack' method to get back
     * 	a byte array from float array.
     */
    
    private static void pack8Bit(byte[] bytes, int i, long temp) {
		bytes[i] = (byte) (temp & 0xffL);
    }

    private static void pack16Bit(byte[]  bytes,
             int     i,
             long    temp)  {
            bytes[i    ] = (byte) ( temp         & 0xffL);
            bytes[i + 1] = (byte) ((temp >>> 8L) & 0xffL);
        }
    
    private static long unsignSigned(long temp, int bitsPerSample) {
        return temp + (long) fullScale(bitsPerSample);
    }
}
