/*
 * Copyright (c) 2021.
 *
 * Juergen Key. Alle Rechte vorbehalten.
 *
 * Weiterverbreitung und Verwendung in nichtkompilierter oder kompilierter Form,
 * mit oder ohne Veraenderung, sind unter den folgenden Bedingungen zulaessig:
 *
 *    1. Weiterverbreitete nichtkompilierte Exemplare muessen das obige Copyright,
 * die Liste der Bedingungen und den folgenden Haftungsausschluss im Quelltext
 * enthalten.
 *    2. Weiterverbreitete kompilierte Exemplare muessen das obige Copyright,
 * die Liste der Bedingungen und den folgenden Haftungsausschluss in der
 * Dokumentation und/oder anderen Materialien, die mit dem Exemplar verbreitet
 * werden, enthalten.
 *    3. Weder der Name des Autors noch die Namen der Beitragsleistenden
 * duerfen zum Kennzeichnen oder Bewerben von Produkten, die von dieser Software
 * abgeleitet wurden, ohne spezielle vorherige schriftliche Genehmigung verwendet
 * werden.
 *
 * DIESE SOFTWARE WIRD VOM AUTOR UND DEN BEITRAGSLEISTENDEN OHNE
 * JEGLICHE SPEZIELLE ODER IMPLIZIERTE GARANTIEN ZUR VERFUEGUNG GESTELLT, DIE
 * UNTER ANDEREM EINSCHLIESSEN: DIE IMPLIZIERTE GARANTIE DER VERWENDBARKEIT DER
 * SOFTWARE FUER EINEN BESTIMMTEN ZWECK. AUF KEINEN FALL IST DER AUTOR
 * ODER DIE BEITRAGSLEISTENDEN FUER IRGENDWELCHE DIREKTEN, INDIREKTEN,
 * ZUFAELLIGEN, SPEZIELLEN, BEISPIELHAFTEN ODER FOLGENDEN SCHAEDEN (UNTER ANDEREM
 * VERSCHAFFEN VON ERSATZGUETERN ODER -DIENSTLEISTUNGEN; EINSCHRAENKUNG DER
 * NUTZUNGSFAEHIGKEIT; VERLUST VON NUTZUNGSFAEHIGKEIT; DATEN; PROFIT ODER
 * GESCHAEFTSUNTERBRECHUNG), WIE AUCH IMMER VERURSACHT UND UNTER WELCHER
 * VERPFLICHTUNG AUCH IMMER, OB IN VERTRAG, STRIKTER VERPFLICHTUNG ODER
 * UNERLAUBTE HANDLUNG (INKLUSIVE FAHRLAESSIGKEIT) VERANTWORTLICH, AUF WELCHEM
 * WEG SIE AUCH IMMER DURCH DIE BENUTZUNG DIESER SOFTWARE ENTSTANDEN SIND, SOGAR,
 * WENN SIE AUF DIE MOEGLICHKEIT EINES SOLCHEN SCHADENS HINGEWIESEN WORDEN SIND.
 *
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.elbosso.tools.screenrecorder;

import de.elbosso.ui.image.Utilities;
import de.elbosso.util.generator.generalpurpose.RandomColor;
import de.elbosso.util.generator.semantics.DungeonMazeImageSequence;
import de.netsysit.util.threads.CubbyHole;
import de.netsysit.util.threads.SimpleNonBlockingCubbyHole;

import javax.imageio.ImageIO;
import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

/**For AVI files, each frame must have a time stamp set.See the
following message from the jmf - interest archives for details :
http : //archives.java.sun.com/cgi-bin/wa?A2=ind0107&L=jmf-
interest&P=R34660
 */
public class AviCreator implements ControllerListener,
		DataSinkListener
{
	private final static org.slf4j.Logger CLASS_LOGGER = org.slf4j.LoggerFactory.getLogger(AviCreator.class);

	boolean internalConstantFrameRate;
	long timestampOfFirstFrame=-1l;

	private boolean doIt(Rectangle rect, int frameRate, MediaLocator outML, boolean constantFrameRate)
	{
		RobotImageDataSource ids = new RobotImageDataSource(this, rect, frameRate);
//		ImageDataSource ids = new ImageDataSource(this, width, height, frameRate);
		return doIt(ids,frameRate,outML,constantFrameRate);
	}
	private boolean doIt(int width, int height, int frameRate, MediaLocator outML,boolean constantFrameRate)
	{
		ImageDataSource ids = new ImageDataSource(this, width, height, frameRate);
		return doIt(ids,frameRate,outML,constantFrameRate);
	}
	public boolean doIt(ImageProvider imageProvider, int frameRate, MediaLocator outML, boolean constantFrameRate)
	{
		AsynchImageDataSource ids = new AsynchImageDataSource(this, imageProvider, frameRate);
		return doIt(ids,frameRate,outML,constantFrameRate);
	}
	private boolean doIt(PullBufferDataSource ids, int frameRate,MediaLocator outML,boolean constantFrameRate)
	{
		internalConstantFrameRate=constantFrameRate;
		Processor p;
		try
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("- create processor for the image datasource ...");
			p = javax.media.Manager.createProcessor(ids);
		}
		catch (Exception e)
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Yikes! Cannot create a processor from the datasource.");
			return false;
		}
		p.addControllerListener(this);
// Put the Processor into configured state so we can set someprocessing options on the processor.
		p.configure();
		if (!waitForState(p, Processor.Configured))
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Failed to configure the processor.");
			return false;
		}
// Set the output content descriptor to QuickTime.
		p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.MSVIDEO));
//		p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
//		p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.MPEG));
// Query for the processor for supported formats.
// Then set it on the processor.
		TrackControl tcs[] = p.getTrackControls();
		Format f[] = tcs[0].getSupportedFormats();
		if (f == null || f.length <= 0)
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("The mux does not support the input format: "
					+ tcs[0].getFormat());
			return false;
		}
		else
		{
			for (int i=0;i<f.length;++i)
			{
				if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("available track format ["+i+"]: " + f[i]+" "+f[i].getEncoding());
			}
		}
		//out: 4,5
		//0: 23MB
		//1: 35MB
		//2: 47MB
		//3: 17MB
		//6: 35MB
		//7: 17MB
		//8: 17MB
		//9: 23MB
		//10:35MB
		//11:47MB
		//12:17MB
		tcs[0].setFormat(f[1]);
		if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Setting the track format to: " + f[1]);
// We are done with programming the processor. Let's just realize it.
		p.realize();
		if (!waitForState(p, Processor.Realized))
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Failed to realize the processor.");
			return false;
		}
// Now, we'll need to create a DataSink.
		DataSink dsink;
		if ((dsink = createDataSink(p, outML)) == null)
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Failed to create a DataSink for the given outputMediaLocator: " + outML);
			return false;
		}
		dsink.addDataSinkListener(this);
		fileDone = false;
		if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("start processing...");
// OK, we can now start the actual transcoding.
		new Thread()
		{
			public void run()
			{
				try
				{
					p.start();
					dsink.start();
// Wait for EndOfStream event.
					waitForFileDone();
// Cleanup.
					try
					{
						dsink.close();
					}
					catch (Exception e)
					{
					}
					p.removeControllerListener(AviCreator.this);
					if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("...done processing.");
				}
				catch (IOException e)
				{
					if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("IO error during processing");
					e.printStackTrace();
				}
			}
		}.start();
		return true;
	}

	/** Create the DataSink.
	 */
	private DataSink createDataSink(Processor p, MediaLocator outML)
	{
		DataSource ds;
		if ((ds = p.getDataOutput()) == null)
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Something is really wrong: the processor does nothave an output DataSource");
			return null;
		}
		DataSink dsink;
		try
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("- create DataSink for: " + outML);
			dsink = javax.media.Manager.createDataSink(ds, outML);
			dsink.open();
		}
		catch (Exception e)
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Cannot create the DataSink: " + e);
			return null;
		}
		return dsink;
	}
	private Object waitSync = new Object();
	private boolean stateTransitionOK = true;

	/** Block until the processor has transitioned to the given state.
	 * Return false if the transition failed.
	 */
	private boolean waitForState(Processor p, int state)
	{
		synchronized (waitSync)
		{
			try
			{
				while (p.getState() < state && stateTransitionOK)
				{
					waitSync.wait();
				}
			}
			catch (Exception e)
			{
			}
		}
		return stateTransitionOK;
	}

	/** Controller Listener.
	 */
	public void controllerUpdate(ControllerEvent evt)
	{

		if (evt instanceof ConfigureCompleteEvent || evt instanceof RealizeCompleteEvent || evt instanceof PrefetchCompleteEvent)
		{
			synchronized (waitSync)
			{
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		}
		else if (evt instanceof ResourceUnavailableEvent)
		{
			synchronized (waitSync)
			{
				stateTransitionOK = false;
				waitSync.notifyAll();
			}
		}
		else if (evt instanceof EndOfMediaEvent)
		{
			evt.getSourceController().stop();
			evt.getSourceController().close();
//			if(CLASS_LOGGER.isDebugEnabled())CLASS_LOGGER.debug("Se procue CONTROLLER-END");
		}
	}
	private Object waitFileSync = new Object();
	private boolean fileDone = false;
	private boolean fileSuccess = true;

	/** Block until file writing is done.
	 */
	private boolean waitForFileDone()
	{
		synchronized (waitFileSync)
		{
			try
			{
				while (!fileDone)
				{
					waitFileSync.wait();
				}
			}
			catch (Exception e)
			{
			}
		}
		return fileSuccess;
	}

	/**
	 * Event handler for the file writer.
	 */
	public void dataSinkUpdate(DataSinkEvent evt)
	{
		if (evt instanceof EndOfStreamEvent)
		{
			synchronized (waitFileSync)
			{
				fileDone = true;
				waitFileSync.notifyAll();
//				if(CLASS_LOGGER.isDebugEnabled())CLASS_LOGGER.debug("Se procue DATASINKEVENT-END");
			}
		}
		else if (evt instanceof DataSinkErrorEvent)
		{
			synchronized (waitFileSync)
			{
				fileDone = true;
				fileSuccess = false;
				waitFileSync.notifyAll();
			}
		}
	}

	/** Self-test main.
	 * @param args Arguments from the command-line.
	 * @exception Exception case of errors.
	 */
	public static void main(String args[]) throws Exception
	{
//jpegCreator.main(null);
//if (args.length == 0)
// prUsage();
// Parse the arguments.
//		int i = 0;
		int width = -1, height = -1, frameRate = 10;
//Vector inputFiles = new Vector();
		String outputURL = null;
//		width = 658;
//		height = 573;
width = 320;//1280;//657;
height = 256;//800;//573;
		outputURL = "/tmp/test.avi";
//outputURL = "test.mov";
// Generate the output media locators.
		MediaLocator oml;
		if ((oml = createMediaLocator(outputURL)) == null)
		{
			if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Cannot build media locator from: " + outputURL);
			System.exit(1);
		}
		RandomColor randomColor=new RandomColor();
		DungeonMazeImageSequence dungeonMazeImageSequence=new DungeonMazeImageSequence();
		for(int i=0;i<10;++i)
		{
			BufferedImage bimg=new BufferedImage(320,256, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2=bimg.createGraphics();
			g2.setPaint(Color.red);//randomColor.next());
			g2.fillRect(0,0, bimg.getWidth(),bimg.getHeight());
			g2.dispose();
			bimg= dungeonMazeImageSequence.next();
			bimg= Utilities.toBufferedImage(bimg.getScaledInstance(320,256,0), BufferedImage.TYPE_INT_ARGB);
			ImageIO.write(bimg,"png",new File("/tmp/"+i+".png"));
		}
		AviCreator imageToMovie = new AviCreator();
//		imageToMovie.doIt(width, height, frameRate, oml,true);
//		imageToMovie.doIt(new java.awt.Rectangle(0,0,640,480), frameRate, oml,false);
//		imageToMovie.doIt(new javax.swing.JLabel(), frameRate, oml,true);
		CubbyHole<RenderedImage> cubbyHole=new SimpleNonBlockingCubbyHole();
		ImageProvider imageProvider=new ImageProviderImpl(cubbyHole,new Rectangle(0,0,640,480));
		imageToMovie.doIt(imageProvider, frameRate, oml,false);
		Thread t=new Thread(imageProvider);
		t.start();
		t.join();
		System.exit(0);
	}
	static interface ImageProvider extends Runnable
	{
		public CubbyHole<RenderedImage> getCubbyHole();
		public Rectangle getRect();

	}
	static class ImageProviderImpl implements ImageProvider
	{
		private final CubbyHole<RenderedImage> cubbyHole;
		private final Rectangle rect;

		public ImageProviderImpl(CubbyHole<RenderedImage> cubbyHole, Rectangle rect)
		{
			this.cubbyHole = cubbyHole;
			this.rect=rect;
		}

		public CubbyHole<RenderedImage> getCubbyHole()
		{
			return cubbyHole;
		}

		public Rectangle getRect()
		{
			return rect;
		}

		@Override
		public void run()
		{
			for(int i=0;i<300;++i)
			{
				try
				{
					long now=System.currentTimeMillis();
					cubbyHole.put(new Robot().createScreenCapture(rect));
					long then=System.currentTimeMillis();
					Thread.currentThread().sleep(1000/10-(then-now));
				} catch (AWTException|InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			cubbyHole.put(null);
		}
	}
	/*
	static void prUsage() {
	if(CLASS_LOGGER.isErrorEnabled())CLASS_LOGGER.error("Usage: java JpegImagesToMovie -w <width> -h
	<height> -f <frame rate> -o <output URL> <input JPEG file 1> <input
	JPEG file 2> ...");
	System.exit( -1);
	}
	 */
	/** Create a media locator from the given string.
	 */
// Allows JMF to locate output file.
	private static MediaLocator createMediaLocator(String url)
	{
		MediaLocator ml;
		if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null)
		{
			return ml;
		}
		if (url.startsWith(File.separator))
		{
			if ((ml = new MediaLocator("file:" + url)) != null)
			{
				return ml;
			}
		}
		else
		{
			String file = "file:" + System.getProperty("user.dir")
					+ File.separator + url;
			if ((ml = new MediaLocator(file)) != null)
			{
				return ml;
			}
		}
		return null;
	}

}
