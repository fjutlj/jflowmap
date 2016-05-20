package test.piccoloJar;

import javax.swing.SwingUtilities; 

import org.piccolo2d.PNode; 
import org.piccolo2d.event.PMouseWheelZoomEventHandler; 
import org.piccolo2d.extras.PFrame; 
import org.piccolo2d.nodes.PText; 
  
/**
 * This program demonstrates how to create zoomable text component 
 * using Piccolo2D framework. 
 * @author www.codejava.net 
 * 
 */ 
public class ZoomableTextDemo extends PFrame { 
  
    public ZoomableTextDemo() { 
        super("Zoomable Text Demo", false, null); 
        setSize(480, 480); 
        setLocationRelativeTo(null); 
        getCanvas().removeInputEventListener(getCanvas().getZoomEventHandler()); 
         
        // disable panning 
        //getCanvas().removeInputEventListener(getCanvas().getPanEventHandler()); 
        getCanvas().addInputEventListener(new PMouseWheelZoomEventHandler()); 
        //getCanvas().addInputEventListener(new ScrollZoom()); 
    } 
      
    public void initialize() { 
    	System.out.println("in public void initialize()，这是自动调用！");
        PNode textNode = new PText("Hello CodeJava!");      
//        textNode.setX(getCanvas().getWidth()/2-textNode.getWidth()/2); 
//        textNode.setY(getCanvas().getHeight()/2-textNode.getHeight()/2); 
        System.out.println("getCanvas().getWidth(): " + getCanvas().getWidth());
        System.out.println("getCanvas().getHeight(): " + getCanvas().getHeight());
        
        textNode.setX(0); 
        textNode.setY(10); 
//        textNode.addInputEventListener(new ScrollZoom()); 
        getCanvas().getLayer().addChild(textNode); 
    } 
  
    public static void main(String[] args) { 
        SwingUtilities.invokeLater(new Runnable() { 
  
            @Override 
            public void run() { 
                new ZoomableTextDemo().setVisible(true); 
            } 
        }); 
    } 
}
