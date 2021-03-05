package here.lenrik.xcursormaker;

class MenuWindow extends CloseablePApplet {

	public void settings () {
		size(100, 400, P2D);
	}

	public void setup () {
		surface.setResizable(true);
	}

	public void draw () {
		background(0);
		print("");
		text(
				EditorWindow.frames + "\n" +
				EditorWindow.frames * EditorWindow.delay + "\n" +
				EditorWindow.delay + (EditorWindow.save ?
				("\n" +
						(EditorWindow.selectedTemplate + 1) + "/" + EditorWindow.templates.size() + "\n" +
						EditorWindow.frame + "/" + EditorWindow.frames + "\n" +
						nf(
								(EditorWindow.frame + EditorWindow.frames * EditorWindow.selectedTemplate + 0.0f) /
										(EditorWindow.frames * EditorWindow.templates.size()) * 100,
								2, 3) +
						'%'
				) : EditorWindow.done ? "\ndone" : ""),
				0, 10
		);
	}

	public void keyPressed () {
		if (key == 27) {
			requestedClose = true;
		}
	}

}
