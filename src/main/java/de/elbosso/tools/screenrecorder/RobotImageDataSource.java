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

import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

/**
 * A DataSource to read from a list of JPEG image files and turn that
 * into a stream of JMF buffers.
 * The DataSource is not seekable or positionable.
 */
class RobotImageDataSource extends PullBufferDataSource
{
	private final static org.slf4j.Logger CLASS_LOGGER = org.slf4j.LoggerFactory.getLogger(RobotImageDataSource.class);

	private final AviCreator aviCreator;
	private RobotImageSourceStream[] streams;

	RobotImageDataSource(AviCreator aviCreator, java.awt.Rectangle rect, int frameRate)
	{
		this.aviCreator = aviCreator;
		streams = new RobotImageSourceStream[1];
		streams[0] = new RobotImageSourceStream(aviCreator, rect, frameRate);
	}

	public void setLocator(MediaLocator source)
	{
	}

	public MediaLocator getLocator()
	{
		return null;
	}

	/**
	 * Content type is of RAW since we are sending buffers of video
	 * frames without a container format.
	 */
	public String getContentType()
	{
		return ContentDescriptor.RAW;
	}

	public void connect()
	{
	}

	public void disconnect()
	{
	}

	public void start()
	{
	}

	public void stop()
	{
	}

	/**
	 * Return the ImageSourceStreams.
	 */
	public PullBufferStream[] getStreams()
	{
		return streams;
	}

	/**
	 * We could have derived the duration from the number of frames and
	 * frame rate. But for the purpose of this program, it's not necessary.
	 */
	public Time getDuration()
	{
//			if(CLASS_LOGGER.isDebugEnabled())CLASS_LOGGER.debug("dur is " + streams[0].nextImage);
//return new Time(1000000000);
		return DURATION_UNKNOWN;
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
