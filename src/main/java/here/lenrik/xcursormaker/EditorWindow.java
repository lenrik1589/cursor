package here.lenrik.xcursormaker;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;
import com.jogamp.newt.opengl.GLWindow;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;

import static here.lenrik.xcursormaker.Template.indent;

public class EditorWindow extends PApplet {
	public static int selectedX = 0;
	public static int selectedY = 0;
	public static int selectedTemplate;
	public static int selectedSize;
	public static int delay;
	public static int frames;
	public static int frame;
	public static boolean pressedInsideToolbar = false;
	public static boolean draggingHotspot = false;
	public static PreviewWindow previewWindow;
	public static PrintWriter shellScriptWriter;
	public static PrintWriter options;
	public static MenuWindow menuWindow;
	public static ArrayList<String> backgrounds = new ArrayList<>();
	public static ArrayList<Template> templates = new ArrayList<>();
	public static ArrayList<IntBinaryOperator> generators;
	public static Boolean outputAbsolute;
	public static File outputLocation;
	public static PGraphics cursorImage;
	public static boolean save = false;
	public static boolean queued;
	public static boolean done;

	public void setup () {
		colorMode(HSB);
		generators = new ArrayList<>(
						Lists.newArrayList(
										(x, y) -> color(0, 0, save ? 0 : 1, save ? 0 : 1),
										(x, y) -> color(63, 50, 255, 255),
										(x, y) -> color((0f + frame) / frames * 255, 255, 255, save ? 140 : 100),
										(x, y) -> color(0, 0, 0, 60),
										(x, y) -> color(0, 0, 0, 20),
										(x, y) -> color(((0f + frame) / frames * 255 + PVector.angleBetween(new PVector(0, 1), new PVector(x - 14, y - 14).normalize()) * 255 / TWO_PI * ((x - 14) < 0 ? -1 : 1) + 255) % 255, 255, 255, 100),
										(x, y) -> color(((0f + frame) / frames * 255 + PVector.angleBetween(new PVector(0, 1), new PVector(x - 11.5f, y - 11.5f).normalize()) * 255 / TWO_PI * ((x - 11.5f) < 0 ? -1 : 1) + 255) % 255, 255, 255, 100),
										(x, y) -> color(43, 15, 30, 220),
										(x, y) -> color((((0f + frame) / frames + 0.5f) % 1) * 255, 255, 255, save ? 140 : 100)
						)
		);
		load("data/configs.json");
		selectedSize = (Integer) templates.get(selectedTemplate).sizes.keySet().toArray()[0];
		cursorImage = createGraphics(selectedSize, selectedSize/*, FX2D);*/, P2D);
		frameRate(1000f / delay);
		background(0);
		rectMode(CORNER);
		previewWindow = new PreviewWindow();
		menuWindow = new MenuWindow();
	}

	public static int getColor (int n, int x, int y) {
		return (0 <= n && n < generators.size()) ? generators.get(n).applyAsInt(x, y) : 0;
	}

	public static void saveConfig (String location) {
		println("saving config");
		ArrayList<String> strings = new ArrayList<>();
		strings.add(indent(0) + "{");
		strings.add(indent(1) + "\"templates\":[");
		for (Template template : templates) {
			strings.addAll(template.toStrings(1));
		}
		strings.set(strings.size() - 1, strings.get(strings.size() - 1).substring(0, strings.get(strings.size() - 1).length() - 1));
		strings.add(indent(1) + "],");
		strings.add(indent(1) + "\"frames\" : " + frames + ",");
		strings.add(indent(1) + "\"save_location\" : \"" + outputLocation + "\",");
		strings.add(indent(1) + "\"delay\" : " + delay + ",");
		strings.add(indent(1) + "\"backgrounds\" : [");
		backgrounds.forEach(s -> strings.add(indent(2) + "\"" + s + "\","));
		strings.set(strings.size() - 1, strings.get(strings.size() - 1).substring(0, strings.get(strings.size() - 1).length() - 1));
		strings.add(indent(1) + "]");
		strings.add(indent(0) + "}");
		saveStrings(new File(PApplet.calcSketchPath(), location), strings.toArray(new String[]{}));
		println("saved config");
	}

	static public void main (String[] args) {
		System.setProperty("newt.debug.EDT", "true");
		String[] appletArgs = new String[]{"here.lenrik.xcursormaker.EditorWindow"};
		if (args != null) {
			PApplet.main(concat(appletArgs, args));
		} else {
			PApplet.main(appletArgs);
		}
	}

	public void settings () {
		size(240, 260/*);//*//*,FX2D);*/, P2D);
	}

	public void draw () {
		int scaledX = mouseX / 10, scaledY = mouseY / 10;
		background(255);
		cursorImage.beginDraw();
		Template template = templates.get(selectedTemplate);
		for (int y = 0; y < selectedSize; y++) {
			for (int x = 0; x < selectedSize; x++) {
				noStroke();
				int color = getColor(template.sizes.get(selectedSize)[y][x], x, y);
				fill(color);
				cursorImage.set(x, y, color);
				rect(x * 10, y * 10, 10, 10);
			}
		}
		cursorImage.endDraw();
		if (!previewWindow.requestedClose) {
			previewWindow.redraw();
		} else {
			previewWindow.close();
		}
		if (menuWindow.requestedClose) {
			menuWindow.close();
		}
		stroke(90, 240, 255);
		noFill();
		rect(selectedX * 10, selectedY * 10, 10, 10);
		if (draggingHotspot) {
			stroke(175, 50, 30, 170);
		} else {
			noStroke();
		}
		fill(0, 255, 200, 70);
		this.circle((draggingHotspot ? scaledX : template.hotspot.x) * 10 + 5, (draggingHotspot ? scaledY : template.hotspot.y) * 10 + 5, 4);
		fill(0);
		rect(0, 240, 240, 20);
		fill(170);
		rect(0, 240, 30, 20);
		fill(7, 220, 200);
		rect(30, 240, 60, 20);
		fill(180, 200, 200);
		rect(150, 240, 30, 20);
		fill(90, 200, 200);
		rect(180, 240, 30, 20);
		fill(3, 200, 200);
		rect(210, 240, 30, 20);
		fill(255);
		text(template.name, 40, 255);
		text("save", 0, 255);
		text("rel", 150, 255);
		text("new", 180, 255);
		text("del", 210, 255);
//		cursor(cursorImage, (int) template.hotspot.x, (int) template.hotspot.y);
		{
			if (save) {
				cursorImage.save(new File(outputLocation, template.name + "_" + frame + ".png").toString());// outputLocation + "/" + template.name + "_" + frame + ".png");
				options.println("24\t" + (int) template.hotspot.x + "\t" +
								(int) template.hotspot.y + "\t" +
								template.name + "_" + frame + ".png\t" + delay);
			}
			frame = (frame + 1) % frames;
			if (done && frame == 0) {
				done = false;
				frameRate(1000f / delay);
			}
			if (save) {
				if (frame == frames - 1 && selectedTemplate == templates.size() - 1) {
					options.flush();
					print("saved config file\n");
				}
				if (frame == 0) {
					selectedTemplate = (selectedTemplate + 1) % templates.size();
					options.flush();
					options.close();
					template = templates.get(selectedTemplate);
					if (selectedTemplate == 0) {
						save = false;
						done = true;
						shellScriptWriter.println("echo \"done\";");
						shellScriptWriter.flush();
					} else {
						options = createWriter(outputLocation + "/" + template.name + ".conf");
						shellScriptWriter.println("echo \"stitching " + template.name + " " + (selectedTemplate + 1) + "/" + templates.size() + "\";");
						shellScriptWriter.println("xcursorgen " + template.name + ".conf " + template.name + ";");
					}
				}
				if (queued) {
					queued = false;
				}
			}
			if (queued) {
				frame = 0;
				selectedTemplate = 0;
				options = createWriter(new File(outputLocation, templates.get(0).name + ".conf"));
				shellScriptWriter = createWriter(new File(outputLocation, "makeCursors.sh"));
				shellScriptWriter.println("cd " + outputLocation + ";");
				shellScriptWriter.println("echo \"stitching " + template.name + " " + (selectedTemplate + 1) + "/" + templates.size() + "S\";");
				shellScriptWriter.println("xcursorgen " + template.name + ".conf " + template.name + ";");
				save = true;
				frameRate(1000);
			}
		}
	}

	protected void circle (float x, float y, int radius) {
		ellipse(x, y, radius, radius);
	}

	public void load (String location) {
		JSONObject loadedJson = loadJSONObject(location);
		JSONArray loadedJsonTemplates = loadedJson.getJSONArray("templates");
		templates.clear();
		IntStream.range(0, loadedJsonTemplates.size()).forEach(i -> templates.add(Template.fromJson((JSONObject) loadedJsonTemplates.get(i))));
		frames = loadedJson.getInt("frames", 35);
		delay = loadedJson.getInt("delay", 50);
		outputLocation = new File(System.getProperty("user.home"), loadedJson.getString("save_location",  "/Desktop/cursors"));
		backgrounds.clear();
		Arrays.stream(loadedJson.getJSONArray("backgrounds").getStringArray()).iterator().forEachRemaining(backgrounds::add);
	}

	public void keyPressed (KeyEvent event) {
		if (save) {
			return;
		}
		if ((int) event.getKey() != 65535) {
			println((int) event.getKey(), '\t', event.getKeyCode(), event.isAltDown(), event.isControlDown(), event.isShiftDown(), event.isMetaDown(), event.isAutoRepeat());
		}
		if (event.isAltDown()) {
			switch (event.getKeyCode()) {
				case 77 -> {
					if (menuWindow.getSurface() == null || menuWindow.getSurface().getNative() == null || ((GLWindow) menuWindow.getSurface().getNative()).getGL() == null) {
						runSketch(new String[]{"Menu"}, menuWindow);
					}
				}
				case 80 -> {
					if (previewWindow.getSurface() == null || previewWindow.getSurface().getNative() == null || ((GLWindow) previewWindow.getSurface().getNative()).getGL() == null) {
						runSketch(new String[]{"Preview"}, previewWindow);
					}
				}
			}
		} else {
			switch (key) {
				case 19 -> saveConfig("data/configs.json");
				case 8 -> {
					templates.get(selectedTemplate).hotspot.x = selectedX;
					templates.get(selectedTemplate).hotspot.y = selectedY;
				}
				default -> {
					if (event.isControlDown()) {
						switch (keyCode) {
							case ESC -> key = 0;
							case LEFT -> {
								selectedTemplate = (templates.size() + selectedTemplate - 1) % templates.size();
								if (menuWindow.getSurface() != null) {
									menuWindow.getSurface().setTitle(templates.get(selectedTemplate).name);
								}
							}
							case RIGHT -> {
								selectedTemplate = (selectedTemplate + 1) % templates.size();
								if (menuWindow.getSurface() != null) {
									menuWindow.getSurface().setTitle(templates.get(selectedTemplate).name);
								}
							}
							case UP -> templates.get(selectedTemplate).sizes.get(selectedSize)[selectedY][selectedX] = (templates.get(selectedTemplate).sizes.get(selectedSize)[selectedY][selectedX] + 1) % generators.size();
							case DOWN -> templates.get(selectedTemplate).sizes.get(selectedSize)[selectedY][selectedX] = (generators.size() + templates.get(selectedTemplate).sizes.get(selectedSize)[selectedY][selectedX] - 1) % generators.size();
						}
					} else {
						switch (keyCode) {
							case LEFT -> selectedX = max(selectedX - 1, 0);
							case RIGHT -> selectedX = min(selectedX + 1, selectedSize - 1);
							case UP -> selectedY = max(selectedY - 1, 0);
							case DOWN -> selectedY = min(selectedY + 1, selectedSize - 1);
						}
					}
				}
			}
		}
		if (key == 27) {
			key = 0;
		}
	}

	public void mousePressed () {
		if (save)
			return;
		int scaledX = mouseX / 10, scaledY = mouseY / 10;
		draggingHotspot = scaledX == (int) templates.get(selectedTemplate).hotspot.x && scaledY == (int) templates.get(selectedTemplate).hotspot.y;
		pressedInsideToolbar = scaledY >= 24;
//		println(scaledX, scaledY, pressedInsideToolbar, draggingHotspot, mouseButton);
	}

	public void mouseReleased () {
		if (save)
			return;
		int scaledX = mouseX / 10, scaledY = mouseY / 10;
		if (!pressedInsideToolbar && (!draggingHotspot || (scaledX == (int) templates.get(selectedTemplate).hotspot.x && scaledY == (int) templates.get(selectedTemplate).hotspot.y)) && mouseButton == 37) {
			selectedX = scaledX;
			selectedY = scaledY;
		}
		if (draggingHotspot) {
			templates.get(selectedTemplate).hotspot.y = max(min(scaledY, templates.get(selectedTemplate).sizes.get(selectedSize).length - 1), 0);
			templates.get(selectedTemplate).hotspot.x = max(min(scaledX, templates.get(selectedTemplate).sizes.get(selectedSize)[(int) templates.get(selectedTemplate).hotspot.y].length - 1), 0);
			draggingHotspot = false;
		}
		//println(mouseX, mouseY)
		if (pressedInsideToolbar) {
			if (0 < mouseX && mouseX < 30 && 240 < mouseY && mouseY < 260) {
				saveConfig("data/configs.json");
			}
			if (30 < mouseX && mouseX < 90 && 240 < mouseY && mouseY < 260) {
				println("generating cursor sources");
				queued = true;
			}
			if (150 < mouseX && mouseX < 180 && 240 < mouseY && mouseY < 260) {
				println("reloading configuration file");
				load("data/configs.json");
			}
			if (180 < mouseX && mouseX < 210 && 240 < mouseY && mouseY < 260) {
				println("adding new cursor to the end of the list");
				Template newCursor = new Template("cursor№" + templates.size(), selectedSize);
				templates.add(newCursor);
			}
			if (210 < mouseX && mouseX < 240 && 240 < mouseY && mouseY < 260) {
				println("removing current cursor");
				templates.remove(selectedTemplate);
				selectedTemplate--;
			}
		}
	}

}
