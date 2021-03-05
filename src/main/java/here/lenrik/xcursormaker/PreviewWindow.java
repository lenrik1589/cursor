package here.lenrik.xcursormaker;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import jogamp.newt.WindowImpl;
import processing.core.PGraphics;
import processing.core.PImage;

public class PreviewWindow extends CloseablePApplet {

	public int template = 0, backgroundInd = 0;
	public PGraphics image;
	public Image scaledBackground;
	public Method resizeMethod;
	public String mode = "Colors";

	public PreviewWindow () {
		super();
		try {
			resizeMethod = WindowImpl.class.getDeclaredMethod("setSize", int.class, int.class, boolean.class);
			resizeMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public void setup () {
		surface.setTitle("Preview");
		surface.setResizable(true);
		blendMode(1);
		colorMode(HSB);
		image = createGraphics(24, 24, P2D);
		scaledBackground = loadImage(EditorWindow.backgrounds.get(backgroundInd)).getImage();
		noLoop();
//		WindowListener updatedListener = new WindowListener() {
//			@Override
//			public void windowResized (WindowEvent event) {
//				if (event.getEventType() == 100) {
//					switch (platform) {
//						case WINDOWS -> frameResized(((jogamp.newt.driver.windows.WindowDriver) event.getSource()).getWidth(), ((jogamp.newt.driver.windows.WindowDriver) event.getSource()).getHeight());
//						case LINUX -> frameResized(((jogamp.newt.driver.x11.WindowDriver) event.getSource()).getWidth(), ((jogamp.newt.driver.x11.WindowDriver) event.getSource()).getHeight());
//						case MACOSX -> frameResized(((jogamp.newt.driver.macosx.WindowDriver) event.getSource()).getWidth(), ((jogamp.newt.driver.macosx.WindowDriver) event.getSource()).getHeight());
//					}
//				}
//			}
//
//			@Override
//			public void windowMoved (WindowEvent windowEvent) { }
//
//			@Override
//			public void windowDestroyNotify (WindowEvent windowEvent) { }
//
//			@Override
//			public void windowDestroyed (WindowEvent windowEvent) { }
//
//			@Override
//			public void windowGainedFocus (WindowEvent windowEvent) { }
//
//			@Override
//			public void windowLostFocus (WindowEvent windowEvent) { }
//
//			@Override
//			public void windowRepaint (WindowUpdateEvent windowUpdateEvent) { }
//		};
//		((GLWindow) surface.getNative()).addWindowListener(updatedListener);
	}

	public void frameResized (int w, int h) {
	}

	public void draw () {
		//		print("draw start ");
		if (mode.equals("Image")) {
			image(new PImage(scaledBackground.getScaledInstance(width, height, 0)), 0, 0);
		} else if (mode.equals("Colors")) {
			cBack();
		} else {
			background(128);
			text("invalid mode or window is smaller than loaded image", 0, 10);
		}
		image.beginDraw();
		for (int y = 0, size = EditorWindow.selectedSize; y < size; y++) {
			for (int x = 0; x < size; x++) {
				image.set(x, y, EditorWindow.getColor(EditorWindow.templates.get(EditorWindow.selectedTemplate).sizes.get(EditorWindow.selectedSize)[y][x], x, y));
			}
		}
		image.endDraw();
		cursor(image, (int) EditorWindow.templates.get(EditorWindow.selectedTemplate).hotspot.x, (int) EditorWindow.templates.get(EditorWindow.selectedTemplate).hotspot.y);
		if (keyPressed && keyCode == ENTER) {
			image(new PImage(scaledBackground.getScaledInstance(width, height, 0)), 0, 0);
		}
		//		println("draw end");
	}

	public void keyPressed () {
		switch (key) {
			case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> template = min(key - 48, EditorWindow.templates.size() - 1);
			case '/' -> EditorWindow.queued = true;
			case 'f' -> EditorWindow.frames = max(EditorWindow.frames - 1, 1);
			case 's' -> EditorWindow.frames++;
			case 'p' -> template = (EditorWindow.templates.size() + template - 1) % EditorWindow.templates.size();
			case 'n' -> template = (template + 1) % EditorWindow.templates.size();
			case 'm' -> EditorWindow.delay = max(EditorWindow.delay - 1, 1);
			case 'l' -> EditorWindow.delay++;
			case 27 -> requestedClose = true;
			default -> {
				switch (keyCode) {
					case RIGHT -> {
						if (mode.equals("Image")) {
							if (++backgroundInd == EditorWindow.backgrounds.size()) {
								mode = "Colors";
								forceResize(100, 200);
							} else {
								scaledBackground = loadImage(EditorWindow.backgrounds.get(backgroundInd)).getImage();
							}
						} else {
							mode = "Image";
							backgroundInd = 0;
							scaledBackground = loadImage(EditorWindow.backgrounds.get(backgroundInd)).getImage();
							forceResize(scaledBackground.getWidth(null), scaledBackground.getHeight(null));
						}
					}
					case LEFT -> {
						if (mode.equals("Image")) {
							if (--backgroundInd < 0) {
								mode = "Colors";
								forceResize(100, 200);
							} else {
								scaledBackground = loadImage(EditorWindow.backgrounds.get(backgroundInd)).getImage();
							}
						} else {
							mode = "Image";
							backgroundInd = EditorWindow.backgrounds.size() - 1;
							scaledBackground = loadImage(EditorWindow.backgrounds.get(backgroundInd)).getImage();
							forceResize(scaledBackground.getWidth(null), scaledBackground.getHeight(null));
						}
					}
					default -> {
					}
				}
			}
		}
	}

	private void forceResize (int w, int h) {
		try {
			resizeMethod.invoke(((GLWindow) surface.getNative()).getUpstreamWidget(), w, h, true);
		} catch (IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	public void cBack () {
		pushStyle();
		colorMode(RGB);
		noStroke();
		fill(0);
		int HALF_WIDTH = width / 2;
		int QUARTER_HEIGHT = height / 4;
		rect(0, 0, HALF_WIDTH, QUARTER_HEIGHT);
		fill(127);
		rect(HALF_WIDTH, 0, width, QUARTER_HEIGHT);
		fill(255);
		rect(0, QUARTER_HEIGHT, HALF_WIDTH, QUARTER_HEIGHT * 2);
		fill(0, 255, 127);
		rect(HALF_WIDTH, QUARTER_HEIGHT, width, QUARTER_HEIGHT * 2);
		fill(0, 255, 255);
		rect(0, QUARTER_HEIGHT * 2, HALF_WIDTH, QUARTER_HEIGHT * 3);
		fill(127, 255, 255);
		rect(HALF_WIDTH, QUARTER_HEIGHT * 2, width, QUARTER_HEIGHT * 3);
		fill((int) (0.3 * 255), 255, 255);
		rect(0, QUARTER_HEIGHT * 3, HALF_WIDTH, height);
		fill((int) (0.6 * 255), 255, 255);
		rect(HALF_WIDTH, QUARTER_HEIGHT * 3, width, height);
		popStyle();
	}

	public void settings () { size(100, 200, P2D); }

	//	public void exit () {
	//		((PSurfaceJOGL)this.surface).pgl.finish();
	//	}

}
