package com.rishi.reverb;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Color;
import javax.swing.SwingConstants;

/*
 *	This is the class that handles the GUI components and the communication with the Reverberator class.
 *	All GUI component are instantiated here and their behaviour is coded here.
 *	The call to the 'reverb' method in Reverberator class is made here. Exceptions are handled here.
 */

public class Frame1 {

	private JFrame frmReverberator;
	private JTextField textField;
	static final int DELAY_MIN = 20;
	static final int DELAY_MAX = 1000;
	static final int DELAY_INIT = 99;
	static final int DECAY_MIN = 1;
	static final int DECAY_MAX = 100;
	static final int DECAY_INIT = 29;
	static final int MIX_MAX = 100;
	static final int MIX_MIN = 0;
	static final int MIX_INIT = 50;
	public float delay = 78.9f;
	public float decay = 0.45f;
	public int mixPercent = 50;
	static String message;
	String fileName;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame1 window = new Frame1();
					window.frmReverberator.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Frame1() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmReverberator = new JFrame();
		frmReverberator.setTitle("Reverberator");
		frmReverberator.setBounds(100, 100, 663, 327);
		frmReverberator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmReverberator.getContentPane().setLayout(null);
		
	
	//Instantiating and Initializing all the labels
		JLabel lblEnterFilePath = new JLabel("Enter file path");
		lblEnterFilePath.setHorizontalAlignment(SwingConstants.CENTER);
		lblEnterFilePath.setBounds(-29, 39, 237, 25);
		frmReverberator.getContentPane().add(lblEnterFilePath);
		
		JLabel lblDevelopedBy = new JLabel("Developed by - Rishikesh Daoo");
		lblDevelopedBy.setBounds(449, 251, 196, 16);
		frmReverberator.getContentPane().add(lblDevelopedBy);
		
		JLabel lblDelayinMilliseconds = new JLabel("Delay");
		lblDelayinMilliseconds.setHorizontalAlignment(SwingConstants.CENTER);
		lblDelayinMilliseconds.setBounds(93, 96, 131, 16);
		frmReverberator.getContentPane().add(lblDelayinMilliseconds);
		
		JLabel lblDecayFactor = new JLabel("Decay Factor");
		lblDecayFactor.setHorizontalAlignment(SwingConstants.CENTER);
		lblDecayFactor.setBounds(93, 135, 144, 16);
		frmReverberator.getContentPane().add(lblDecayFactor);
		
		JLabel lblDrywetMixPercentage = new JLabel("Dry/Wet Mix Percentage");
		lblDrywetMixPercentage.setBounds(93, 175, 154, 16);
		frmReverberator.getContentPane().add(lblDrywetMixPercentage);
		
		JLabel lblNewLabel_1 = new JLabel(Float.toString(delay) + " ms");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setForeground(Color.GRAY);
		lblNewLabel_1.setBounds(538, 93, 56, 16);
		frmReverberator.getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel(Float.toString(decay));
		lblNewLabel_2.setForeground(Color.GRAY);
		lblNewLabel_2.setBackground(Color.BLACK);
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(538, 131, 54, 16);
		frmReverberator.getContentPane().add(lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel(Integer.toString(mixPercent) + "%");
		lblNewLabel_3.setForeground(Color.GRAY);
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_3.setBounds(538, 171, 56, 16);
		frmReverberator.getContentPane().add(lblNewLabel_3);
		
		JLabel lblStatus = new JLabel();
		lblStatus.setBounds(12, 251, 225, 16);
		frmReverberator.getContentPane().add(lblStatus);
		
		textField = new JTextField();
		textField.setBounds(142, 39, 365, 22);
		frmReverberator.getContentPane().add(textField);
		textField.setColumns(10);
		
	//Instantiating and Initializing the sliders for controlling reverb parameters
		
		//This slider is for the Delay parameter
		JSlider slider = new JSlider(JSlider.HORIZONTAL, DELAY_MIN, DELAY_MAX, DELAY_INIT);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				
				delay = (slider.getValue()/1.0f);
				lblNewLabel_1.setText(Integer.toString(slider.getValue()) + " ms");
			}
		});
		slider.setBounds(326, 90, 200, 25);
		frmReverberator.getContentPane().add(slider);
		
		//This slider is for the Decay parameter
		JSlider slider_1 = new JSlider(JSlider.HORIZONTAL, DECAY_MIN, DECAY_MAX, DECAY_INIT);
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				
				decay = (slider_1.getValue()/100.0f);
				lblNewLabel_2.setText(Float.toString(decay));
			}
		});
		slider_1.setBounds(326, 128, 200, 26);
		frmReverberator.getContentPane().add(slider_1);
		
		//This slider is for the mix percentage parameter
		JSlider slider_2 = new JSlider(JSlider.HORIZONTAL, MIX_MIN, MIX_MAX, MIX_INIT);
		slider_2.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				
				mixPercent = slider_2.getValue();
				lblNewLabel_3.setText(Integer.toString(mixPercent) + "%");
			}
		});
		slider_2.setBounds(326, 167, 200, 26);
		frmReverberator.getContentPane().add(slider_2);
		
		
		//Create a file chooser
		JFileChooser fc = new JFileChooser();
		
		// This is for the 'Browse file' button. It will pop up the file chooser window and copy the path of selected file into the fileName variable
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if (arg0.getSource() == btnBrowse) {
			        int returnVal = fc.showOpenDialog(null);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            //This is where the file path gets copied.
			            	fileName = file.getPath();
			            	textField.setText(fileName);
			        } else {
			            	textField.setText("No file selected.");
			        }
				}
			}
		});
		btnBrowse.setBounds(522, 37, 97, 25);
		frmReverberator.getContentPane().add(btnBrowse);
		
	//The reverb method in Reverberation class is called when the button is pressed. Exceptions are handled here, and shown as a pop up message. 
		JButton btnAddReverb = new JButton("Play Audio");
		btnAddReverb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	
			try {
				Reverberation audio = new Reverberation();
				audio.reverb(fileName, delay, decay, mixPercent);
				lblStatus.setText("Reverb added. Playing audio.");
				}
			
					catch(LineUnavailableException lue)
					{
						message = "No mixer available. File not supported.";
						lue.printStackTrace();
						JOptionPane.showMessageDialog(frmReverberator, message);
					}
					catch(UnsupportedAudioFileException uafe)
					{
						message = "Only WAV files supported. More formats to be supported in future.";
						uafe.printStackTrace();
						JOptionPane.showMessageDialog(frmReverberator, message);
					}
					catch(FileNotFoundException fnfe)
					{
						message = "Please ensure filename and filepath is correct.";
						fnfe.printStackTrace();
					}
					catch(IOException ioe)
					{
						message = "Error occured will audio input/ output. Please try again.";
						ioe.printStackTrace();
						JOptionPane.showMessageDialog(frmReverberator, message);
					}
					catch(InterruptedException ie)
					{
						message = "Thread Interrupted!";
						ie.printStackTrace();
						JOptionPane.showMessageDialog(frmReverberator, message);
					}
					catch(OutOfMemoryError oome)
					{
						message = "File size too large.";
						oome.printStackTrace();
						JOptionPane.showMessageDialog(frmReverberator, message);
					}
					catch(IllegalArgumentException iae)
					{
						message = "Error loading audio on system.";
						JOptionPane.showMessageDialog(frmReverberator, message);
					}
					catch(Exception ex)
					{
						message = "Application stopped working. Please restart application.";
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frmReverberator, message);
					}

			}
		});
		
		//This button is to stop Audio playback. On button press, the method 'stopTheMusic' is called from the class Reverberator.
		btnAddReverb.setBounds(188, 223, 97, 25);
		frmReverberator.getContentPane().add(btnAddReverb);
		
		JButton btnStopAudio = new JButton("Stop Audio");
		btnStopAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				Reverberation audio = new Reverberation();
				audio.stopTheMusic();
				lblStatus.setText("Audio stopped.");
			}
		});
		btnStopAudio.setBounds(311, 223, 97, 25);
		frmReverberator.getContentPane().add(btnStopAudio);
	}
}
