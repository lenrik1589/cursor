package here.lenrik.xcursormaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

import static java.lang.Math.max;

public class Template {
	public String name;
	public PVector hotspot;
	public HashMap<Integer, Integer[][]> sizes = new HashMap<>();

	public Template () {
	}

	public Template (int size) {
		this("", size);
	}

	public Template (String name, int size) {
		this(name, new PVector(size / 2 - 1, size / 2 - 1), size);
	}

	public Template (String name, PVector hotspot, int size) {
		this.name = name;
		this.hotspot = hotspot;
		sizes = new HashMap<>();
		Integer[][] blank = new Integer[size][size];
		for (int i = 0, blankLength = blank.length; i < blankLength; i++) {
			Integer[] ro = blank[i];
			for (int j = 0, roLength = ro.length; j < roLength; j++) {
				ro[j] = 0;
			}
		}
		sizes.put(size, blank);
	}

	public static String indent (int indent) {
		return "\t".repeat(max(0, indent));
	}

	public static Template fromJson (JSONObject json) {
		Template template = new Template();
		template.name = json.getString("name");
		int[] hot = json.getJSONArray("hotspot").getIntArray();
		template.hotspot = new PVector();
		template.hotspot.x = hot[0];
		template.hotspot.y = hot[1];
		try {
			JSONObject sizeTemplates = json.getJSONObject("sizes");
			((Set<String>) sizeTemplates.keys()).forEach(size -> {
				int t = Integer.parseInt(size);
				try {
					template.sizes.put(t, (Integer[][]) sizeTemplates.get(size));
				} catch (ClassCastException e) {
					Integer[][] n = new Integer[t][t];
					for (int i = 0; i < t; i++) {
						int[] d = ((JSONArray) sizeTemplates.get(size)).getJSONArray(i).getIntArray();
						for (int k = 0, l = d.length; k < l; k++) {
							n[i][k] = d[k];
						}
					}
					template.sizes.put(t, n);
				}
			});
		} catch (RuntimeException e) {
			//			try {
			//				JSONArray pixelModeArray = json.getJSONArray("template");
			//				template.pixelMode = new int[24][24];
			//				for (int i = 0; i < 24; i++) {
			//					template.pixelMode[i] = pixelModeArray.getJSONArray(i).getIntArray();
			//				}
			//			} catch (RuntimeException ignored) {
			//				template.pixelMode = (int[][]) json.get("template");
			//			}
			//			Integer[][] n = new Integer[template.pixelMode.length][template.pixelMode[0].length];
			//			for (int y = 0; y < n.length; y++) {
			//				for (int x = 0; x < n[y].length; x++) {
			//					n[y][x] = template.pixelMode[y][x];
			//				}
			//			}
			//			template.sizes.put(template.pixelMode.length, n);
		}
		return template;
	}

	public String toString () {
		String result = "";
		result += name + "(" + (int) hotspot.x + ", " + (int) hotspot.y + ")";
		return result;
	}

	public ArrayList<String> toStrings (int indent) {
		ArrayList<String> strings = new ArrayList<>();
		strings.add(indent(indent + 1) + "{");
		strings.add(indent(indent + 2) + "\"name\" : \"" + this.name + "\",");
		strings.add(indent(indent + 2) + "\"hotspot\" : [" + (int) this.hotspot.x + ", " + (int) this.hotspot.y + "],");
		strings.add(indent(indent + 2) + "\"sizes\" : {");
		for (int size = 0, count = sizes.size(); size < count; size++) {
			Map.Entry<Integer, Integer[][]> entry = (Map.Entry<Integer, Integer[][]>) sizes.entrySet().toArray()[size];
			strings.add(indent(indent + 3) + "\"" + entry.getKey() + "\" : [");
			for (int row = 0, height = entry.getValue().length; row < height; row++) {
				StringBuilder row_str = new StringBuilder(indent(indent + 4) + '[');
				for (int column = 0, width = entry.getValue()[row].length; column < width; column++) {
					row_str.append(entry.getValue()[row][column]).append(column < width - 1 ? ", " : "]");
				}
				if (row < height - 1) {
					row_str.append(",");
				}
				strings.add(row_str.toString());
			}
			strings.add(indent(indent + 3) + (size < count - 1 ? "]," : "]"));
		}
		strings.add(indent(indent + 2) + "}");
		strings.add(indent(indent + 1) + "},");
		return strings;
	}

}
