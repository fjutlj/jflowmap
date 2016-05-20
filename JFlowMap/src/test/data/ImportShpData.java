package test.data;

import java.awt.Color;
import java.io.IOException;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolox.PFrame;
import jflowmap.FlowMapColorSchemes;
import jflowmap.data.ShapefileReader;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.GeoMap;
import jflowmap.views.ColorCodes;
import jflowmap.views.flowmap.ColorSchemeAware;
import jflowmap.views.map.PGeoMap;

public class ImportShpData extends PFrame {
	 private final PGeoMap map;
	 
	 private final ColorSchemeAware mapColorScheme = new ColorSchemeAware() {
		    @Override
		    public Color getColor(ColorCodes code) {
		      return FlowMapColorSchemes.INVERTED.get(code);
		    }
		  };
	
	public ImportShpData() throws IOException{
		setSize(800, 600);
		 PCanvas canvas = getCanvas();
		 PCamera camera = canvas.getCamera();
//		String location = "D:\\ArcGisWork\\上海行政区边界图\\shangHai\\shangHai.shp";
//		GeoMap.asAreaMap(ShapefileReader.loadShapefile(location, null, null));
		
		
		 map = new PGeoMap(mapColorScheme,
//		        GeoMap.load("data/refugees/countries-areas-ll.xml.gz"),
//		        MapProjections.MERCATOR);
		    		GeoMap.load("D:\\_WORK\\JFlowMap\\JFlowMap\\demo\\data\\maps\\countries-areas-ll.xml.gz"),
		            MapProjections.MERCATOR);
		PLayer layer = canvas.getLayer();
	    layer.addChild(map);
	}

	public static void main(String[] args){
		 try {
			new ImportShpData().setVisible(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};

	}

}
