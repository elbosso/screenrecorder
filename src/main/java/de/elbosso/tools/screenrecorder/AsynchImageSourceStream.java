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

package de.elbosso.tools.screenrecorder;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import javax.media.util.ImageToBuffer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * The source stream to go along with ImageDataSource.
 */
public class AsynchImageSourceStream implements PullBufferStream
{
	private final static org.apache.log4j.Logger CLASS_LOGGER = org.apache.log4j.Logger.getLogger(AsynchImageSourceStream.class);

	private final AviCreator aviCreator;
	private VideoFormat format;
	private BufferedImage frame = null;
	// Bug fix from Forums - next two lines
	private float frameRate;
	private long seqNo = 0;
	private int nextImage = 0; // index of the next image to be read.
	private boolean ended = false;
	private AviCreator.ImageProvider imageProvider;

	public AsynchImageSourceStream(AviCreator aviCreator, AviCreator.ImageProvider imageProvider, int frameRate)
	{
		this.aviCreator = aviCreator;
		this.imageProvider = imageProvider;

// Bug fix from Forums (next line)
		this.frameRate = (float) frameRate;

		final int rMask = 0x00ff0000;
		final int gMask = 0x0000FF00;
		final int bMask = 0x000000ff;

		format = new RGBFormat(new Dimension(imageProvider.getRect().width,
				imageProvider.getRect().height), Format.NOT_SPECIFIED, Format.intArray, frameRate, 32, bMask,
				gMask, rMask);



	}

	/**
	 * We should never need to block assuming data are read from files.
	 */
	public boolean willReadBlock()
	{
		return true;
	}

	/**
	 * This is called from the Processor to read a frame worth of video
	 * data.
	 */
	public void read(Buffer buf) throws IOException
	{
		BufferedImage image = null;
//			synchronized(monitor)
		{
			try
			{
//					if(CLASS_LOGGER.isDebugEnabled())CLASS_LOGGER.debug("trying get");
				image = (BufferedImage) imageProvider.getCubbyHole().get();
				if(image==null)
				{
					// We are done. Set EndOfMedia.
					if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.ERROR))
						CLASS_LOGGER.error("Done reading all images.");
					buf.setEOM(true);
					buf.setOffset(0);
					buf.setLength(0);
					ended = true;
					return;
				}
//					if(CLASS_LOGGER.isDebugEnabled())CLASS_LOGGER.debug("got");
			} catch (Throwable exp)
			{
				exp.printStackTrace();
			}
		}
		frame = image;
		nextImage++;
		if (aviCreator.internalConstantFrameRate)
		{
			long time = (long) (seqNo * (1000 / frameRate) * 1000000);
			buf.setTimeStamp(time);
		}
		else
		{
			java.util.Date now = new java.util.Date();
			if (aviCreator.timestampOfFirstFrame < 0)
				aviCreator.timestampOfFirstFrame = now.getTime();
			long diff = now.getTime() - aviCreator.timestampOfFirstFrame;
			buf.setTimeStamp(diff * 1000000);
		}
		buf.setSequenceNumber(seqNo++);
		Buffer f = ImageToBuffer.createBuffer(frame, frameRate);
		buf.setData(f.getData());
		buf.setOffset(f.getOffset());
		buf.setLength(f.getLength());
		buf.setFormat(f.getFormat());
		buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME);
	}

	/**
	 * Return the format of each video frame. That will be JPEG.
	 */
	public Format getFormat()
	{
		return format;
	}

	public ContentDescriptor getContentDescriptor()
	{
		return new ContentDescriptor(ContentDescriptor.RAW);
	}

	public long getContentLength()
	{
		return 0;
	}

	public boolean endOfStream()
	{
		return ended;
	}

	public Object[] getControls()
	{
		return new Object[0];
	}

	public Object getControl(String type)
	{
		return null;
	}
}