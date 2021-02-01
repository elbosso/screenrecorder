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

import de.elbosso.ui.components.DesktopViewportSelector;
import de.netsysit.util.ResourceLoader;
import de.netsysit.util.threads.CubbyHole;
import de.netsysit.util.threads.SimpleNonBlockingCubbyHole;

import javax.media.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

public class Manager extends de.elbosso.util.beans.EventHandlingSupport implements de.netsysit.util.pattern.command.FileProcessor
{
	private final static org.apache.log4j.Logger CLASS_LOGGER = org.apache.log4j.Logger.getLogger(Manager.class);
	private final static String[] EXPORTIMAGESUFFIXES =javax.imageio.ImageIO.getWriterFileSuffixes();
	private final static String[] EXPORTMOVIESUFFIXES =new String[]{"avi"};
	private de.netsysit.util.pattern.command.ChooseFileAction startRecordingAction;
	private javax.swing.Action preferencesAction;
	private javax.swing.Action stopRecordingAction;
	private javax.swing.Action okAction;
	private javax.swing.Action okScreenshotAction;
	private javax.swing.Action cancelAction;
	private javax.swing.Action snapshotPngAction;
	private javax.swing.JPanel toplevel;
	private ImageProviderImpl imageProvider;
	private DesktopViewportSelector desktopViewportSelector;
	private MediaLocator oml;
	private Rectangle lastDesktopViewportSelectorBounds;
	private boolean drawMousePointer=true;
	private File screenshotFile;

	public static void main(String[]args)
	{
		try
		{
			java.util.Properties iconFallbacks = new java.util.Properties();
			java.io.InputStream is=de.netsysit.util.ResourceLoader.getResource("de/elbosso/ressources/data/icon_trans_material.properties").openStream();
			iconFallbacks.load(is);
			is.close();
			de.netsysit.util.ResourceLoader.configure(iconFallbacks);
		}
		catch(java.io.IOException ioexp)
		{
			ioexp.printStackTrace();
		}

		de.netsysit.util.ResourceLoader.setSize(de.netsysit.util.ResourceLoader.IconSize.small);
		new Manager();
	}

	public Manager()
	{
		javax.swing.JFrame f=new javax.swing.JFrame();
		f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
		javax.swing.JToolBar tb=new javax.swing.JToolBar();
		tb.setFloatable(false);
		toplevel=new javax.swing.JPanel(new BorderLayout());
		toplevel.add(tb, BorderLayout.NORTH);
		createActions();
		tb.add(startRecordingAction);
		tb.add(stopRecordingAction);
		tb.addSeparator();
		tb.add(snapshotPngAction);
		tb.addSeparator();
		tb.add(preferencesAction);
		f.setContentPane(toplevel);
		f.pack();
		f.setVisible(true);
	}

	public void setDrawMousePointer(boolean drawMousePointer)
	{
		boolean old = isDrawMousePointer();
		this.drawMousePointer = drawMousePointer;
		send("drawMousePointer", old, isDrawMousePointer());
	}

	public boolean isDrawMousePointer()
	{
		return drawMousePointer;
	}

	public boolean process(File[] files)
	{
		boolean rv=false;
		try
		{
			desktopViewportSelector=new DesktopViewportSelector(8,8,okAction,cancelAction);
			if(lastDesktopViewportSelectorBounds!=null)
				desktopViewportSelector.setBounds(lastDesktopViewportSelectorBounds);
			if ((oml = createMediaLocator(files[0].toURI().toURL().toString())) == null)
			{
				if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.ERROR))
					CLASS_LOGGER.error("Cannot build media locator from: " + files[0]);
				System.exit(1);
			}
			startRecordingAction.setEnabled(false);
			snapshotPngAction.setEnabled(false);
			rv=true;
		}
		catch(Throwable t)
		{
			de.elbosso.util.Utilities.handleException(CLASS_LOGGER,t);
		}
		return rv;
	}

	private void createActions()
	{
		startRecordingAction=new de.netsysit.util.pattern.command.ChooseFileAction(this,null, ResourceLoader.getIcon("de/netsysit/ressources/gfx/ca/rec_48.png"));
		startRecordingAction.setParent(toplevel);
		startRecordingAction.setAllowedSuffixes(EXPORTMOVIESUFFIXES);
		startRecordingAction.setSaveDialog(true);
		startRecordingAction.setDefaultFileEnding(".avi");

		stopRecordingAction=new javax.swing.AbstractAction(null, ResourceLoader.getIcon("de/netsysit/ressources/gfx/ca/stop_48.png"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				imageProvider.stop();
				stopRecordingAction.setEnabled(false);
				startRecordingAction.setEnabled(true);
				snapshotPngAction.setEnabled(true);
			}
		};
		stopRecordingAction.setEnabled(false);
		preferencesAction=new javax.swing.AbstractAction(null, ResourceLoader.getIcon("toolbarButtonGraphics/general/Preferences24.gif"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				//framerate
				//thickness of selector Border
				//Mauszeiger in video einblenden oder nicht
				//Graphik für Mauszeiger wählen
			}
		};
		preferencesAction.setEnabled(false);
		okAction=new javax.swing.AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					AviCreator imageToMovie = new AviCreator();
					CubbyHole<RenderedImage> cubbyHole = new SimpleNonBlockingCubbyHole();
					imageProvider = new ImageProviderImpl(cubbyHole, desktopViewportSelector.getSelectedBounds());
					lastDesktopViewportSelectorBounds = desktopViewportSelector.getBounds();
					imageToMovie.doIt(imageProvider, 10, oml, false);
					Thread t = new Thread(imageProvider);
					t.start();
					stopRecordingAction.setEnabled(true);
				}
				catch(Throwable t)
				{
					de.elbosso.util.Utilities.handleException(CLASS_LOGGER,toplevel,t);
				}
			}
		};
		cancelAction=new javax.swing.AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				lastDesktopViewportSelectorBounds=desktopViewportSelector.getBounds();
				stopRecordingAction.setEnabled(false);
				startRecordingAction.setEnabled(true);
				snapshotPngAction.setEnabled(true);
			}
		};
		de.netsysit.util.pattern.command.FileProcessor exportImgClient =
				new de.netsysit.util.pattern.command.FileProcessor()
				{
					public boolean process(File[] files)
					{
						return exportImg(files[0]);
					}
				};
		de.netsysit.util.pattern.command.ChooseFileAction img = null;
		if (ResourceLoader.getResource("de/netsysit/ressources/gfx/ca/screenshot_48.png") != null)
		{
			img = new de.netsysit.util.pattern.command.ChooseFileAction(exportImgClient, /*i18n.getString("ImageViewer.*/"snapshotPngAction"/*.text")*/, ResourceLoader.getIcon("de/netsysit/ressources/gfx/ca/screenshot_48.png"));
		}
		else
		{
			img = new de.netsysit.util.pattern.command.ChooseFileAction(exportImgClient, /*i18n.getString("ImageViewer.*/"snapshotPngAction"/*.text")*/, null);
		}
//					de.netsysit.db.ui.Utilities.configureOpenFileChooser(img.getFilechooser());
		img.setAllowedSuffixes(EXPORTIMAGESUFFIXES);
		img.setSaveDialog(true);
		img.setDefaultFileEnding(".png");
//		img.putValue(javax.swing.Action.SHORT_DESCRIPTION, i18n.getString("ImageViewer.snapshotPngAction.tooltip"));
		snapshotPngAction = img;
		okScreenshotAction=new javax.swing.AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Rectangle rect=desktopViewportSelector.getSelectedBounds();
					desktopViewportSelector.setVisible(false);
					desktopViewportSelector.dispose();
					Thread.currentThread().sleep(100l);
					BufferedImage bimg=new Robot().createScreenCapture(rect);
					javax.imageio.ImageIO.write(bimg, screenshotFile.getName().substring(screenshotFile.getName().lastIndexOf(".")+1), screenshotFile);
					de.netsysit.util.lowlevel.BareBonesFileOpen.openFile(screenshotFile);
				}
				catch(Throwable t)
				{
					de.elbosso.util.Utilities.handleException(CLASS_LOGGER,toplevel,t);
				}
				stopRecordingAction.setEnabled(true);
				snapshotPngAction.setEnabled(true);
			}
		};
	}
	private boolean exportImg(File file)
	{
		boolean rv = false;
		try
		{
			desktopViewportSelector = new DesktopViewportSelector(8, 8, okScreenshotAction, cancelAction);
			if (lastDesktopViewportSelectorBounds != null)
				desktopViewportSelector.setBounds(lastDesktopViewportSelectorBounds);
			screenshotFile=file;
			startRecordingAction.setEnabled(false);
			snapshotPngAction.setEnabled(false);
			rv = true;
		} catch (Throwable t)
		{
			de.elbosso.util.Utilities.handleException(CLASS_LOGGER, t);
		}
		return rv;
	}

	class ImageProviderImpl extends de.elbosso.util.threads.StoppableImpl implements AviCreator.ImageProvider
	{
		private final CubbyHole<RenderedImage> cubbyHole;
		private final Rectangle rect;
		private final BufferedImage blackSquare;

		public ImageProviderImpl(CubbyHole<RenderedImage> cubbyHole, Rectangle rect) throws IOException
		{
			this.cubbyHole = cubbyHole;
			this.rect=rect;
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int width = gd.getDisplayMode().getWidth();
			int height = gd.getDisplayMode().getHeight();

			blackSquare = javax.imageio.ImageIO.read(ResourceLoader.getImgResource("de/elbosso/ressources/gfx/mousecursor.png"));
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
			while(isStopped()==false)
			{
				try
				{
					long now=System.currentTimeMillis();
					BufferedImage bimg=new Robot().createScreenCapture(rect);
					if(isDrawMousePointer())
					{
						PointerInfo pointer = MouseInfo.getPointerInfo();
						int x = (int) pointer.getLocation().getX();
						int y = (int) pointer.getLocation().getY();
						Graphics g = bimg.getGraphics();
						g.drawImage(blackSquare, x - rect.x, y - rect.y, null);
						g.dispose();
					}
					cubbyHole.put(bimg);
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
	private MediaLocator createMediaLocator(String url)
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
