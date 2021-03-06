package net.clgd.ccemux.rendering.awt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.util.Colour;
import lombok.extern.slf4j.Slf4j;
import net.clgd.ccemux.api.Utils;
import net.clgd.ccemux.api.rendering.PaletteAdapter;
import net.clgd.ccemux.api.rendering.PaletteAdapter.ColorAdapter;

@Slf4j
class TerminalComponent extends Canvas {
	private static final long serialVersionUID = -5043543826280613143L;

	public static final ColorAdapter<Color> AWT_COLOR_ADAPTER = (r, g, b) -> new Color((float) r, (float) g, (float) b);
	private static final char[] SHUTDOWN_MESSAGE = "Computer is shutdown. Press Ctrl+r to reboot".toCharArray();

	private final PaletteAdapter<Color> paletteCacher;

	public final Terminal terminal;
	public final int pixelWidth;
	public final int pixelHeight;
	public final int margin;

	public char cursorChar = '_';

	public boolean blinkLocked = false;

	private final Cache<Pair<Character, Color>, BufferedImage> charImgCache = CacheBuilder.newBuilder()
		.expireAfterAccess(10, TimeUnit.SECONDS).build();

	public TerminalComponent(Terminal terminal, double termScale) {
		this.pixelWidth = (int) (6 * termScale);
		this.pixelHeight = (int) (9 * termScale);
		this.margin = (int) (2 * termScale);
		this.terminal = terminal;
		this.paletteCacher = new PaletteAdapter<>(terminal.getPalette(), AWT_COLOR_ADAPTER);

		resizeTerminal(terminal.getWidth(), terminal.getHeight());
	}

	public void resizeTerminal(int width, int height) {
		Dimension termDimensions = new Dimension(width * pixelWidth + margin * 2, height * pixelHeight + margin * 2);

		setSize(termDimensions);
		setPreferredSize(termDimensions);
	}

	private void drawChar(AWTTerminalFont font, Graphics g, char c, int x, int y, int color) {
		if (c == '\0' || Character.isSpaceChar(c)) {
			return; // nothing to do here
		}

		Rectangle r = font.getCharCoords(c);
		Color colour = paletteCacher.getColor(color);

		BufferedImage charImg = null;

		float[] zero = new float[4];

		try {
			charImg = charImgCache.get(Pair.of(c, colour), () -> {
				float[] rgb = new float[4];
				colour.getRGBComponents(rgb);

				RescaleOp rop = new RescaleOp(rgb, zero, null);

				GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();

				BufferedImage img = font.getBitmap().getSubimage(r.x, r.y, r.width, r.height);
				BufferedImage pixel = gc.createCompatibleImage(r.width, r.height, Transparency.TRANSLUCENT);

				Graphics ig = pixel.getGraphics();
				ig.drawImage(img, 0, 0, null);
				ig.dispose();

				rop.filter(pixel, pixel);
				return pixel;
			});
		} catch (ExecutionException e) {
			log.error("Could not retrieve char image from cache!", e);
		}

		g.drawImage(charImg, x, y, pixelWidth, pixelHeight, null);
	}

	private void renderTerminal(AWTTerminalFont font, double dt, boolean shutdownOverlay) {
		synchronized (terminal) {
			Graphics g = getBufferStrategy().getDrawGraphics();

			int dx = 0;
			int dy = 0;

			for (int y = 0; y < terminal.getHeight(); y++) {
				TextBuffer textLine = terminal.getLine(y);
				TextBuffer bgLine = terminal.getBackgroundColourLine(y);
				TextBuffer fgLine = terminal.getTextColourLine(y);

				int height = (y == 0 || y == terminal.getHeight() - 1) ? pixelHeight + margin : pixelHeight;

				for (int x = 0; x < terminal.getWidth(); x++) {
					int width = (x == 0 || x == terminal.getWidth() - 1) ? pixelWidth + margin : pixelWidth;

					g.setColor(paletteCacher.getColor((bgLine == null) ? 'f' : bgLine.charAt(x)));
					g.fillRect(dx, dy, width, height);

					char character = (textLine == null) ? ' ' : textLine.charAt(x);
					char fgChar = (fgLine == null) ? ' ' : fgLine.charAt(x);

					drawChar(font, g, character, x * pixelWidth + margin, y * pixelHeight + margin,
						Utils.base16ToInt(fgChar));

					dx += width;
				}

				dx = 0;
				dy += height;
			}

			boolean blink = terminal.getCursorBlink() && (blinkLocked || Utils.getGlobalCursorBlink());

			if (blink) {
				drawChar(font, g, cursorChar, terminal.getCursorX() * pixelWidth + margin,
					terminal.getCursorY() * pixelHeight + margin, terminal.getTextColour());
			}

			if (shutdownOverlay) {
				int remaining = terminal.getWidth() - SHUTDOWN_MESSAGE.length;
				if (remaining >= 0) {
					int startX = margin + remaining * pixelWidth / 2;
					int startY = margin + pixelHeight * (terminal.getHeight() - 1);

					g.setColor(new Color(0x4c4c4c));
					g.fillRect(startX, startY - margin * 2, SHUTDOWN_MESSAGE.length * pixelWidth + margin * 2, pixelHeight + margin * 2);

					g.setColor(new Color(0x888888));
					g.fillRect(startX - margin, startY - margin, SHUTDOWN_MESSAGE.length * pixelWidth + margin * 2, pixelHeight + margin * 2);

					for (int i = 0; i < SHUTDOWN_MESSAGE.length; i++) {
						drawChar(font, g, SHUTDOWN_MESSAGE[i], i * pixelWidth + startX, startY, 15 - Colour.White.ordinal());
					}
				} else {
					// TODO: Word wrap or something
				}
			}

			g.dispose();
			// paletteCacher.setCurrentPalette(terminal.getPalette());
		}
	}

	public void render(AWTTerminalFont font, double dt, boolean shutdownOverlay) {
		if (getBufferStrategy() == null) {
			createBufferStrategy(2);
		}

		do {
			do {
				renderTerminal(font, dt, shutdownOverlay);
			} while (getBufferStrategy().contentsRestored());

			getBufferStrategy().show();
		} while (getBufferStrategy().contentsLost());
	}
}
