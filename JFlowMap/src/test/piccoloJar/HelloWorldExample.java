package test.piccoloJar;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.PFrame;

public class HelloWorldExample extends PFrame {
	
	public void initialize() {	
		PText text = new PText("Hello World");
		text.setX(20);
		text.setY(40);
		getCanvas().getLayer().addChild(text);
		getCanvas().removeInputEventListener(getCanvas().getZoomEventHandler()); 
		
	}

	public static void main(String[] args) {
		new HelloWorldExample();
	}
}
