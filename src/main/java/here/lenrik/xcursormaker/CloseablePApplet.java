package here.lenrik.xcursormaker;

import com.jogamp.newt.opengl.GLWindow;
import processing.core.PApplet;
import processing.opengl.PSurfaceJOGL;

public class CloseablePApplet extends PApplet {
	public boolean requestedClose = false;
	@Override
	public void exit () {
		((PSurfaceJOGL)this.surface).pgl.finish();
	}

	public void close(){
		requestedClose = false;
		((GLWindow) surface.getNative()).destroy();
	}

}
