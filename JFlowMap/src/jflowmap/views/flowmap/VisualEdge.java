/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views.flowmap;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import jflowmap.geom.GeomUtils;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.ColorCodes;
import prefuse.data.Edge;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public abstract class VisualEdge extends PNode {

  private static final int MAX_EDGE_WIDTH = 100;
  private static final long serialVersionUID = 1L;

  private final VisualFlowMap visualFlowMap;
  private final VisualNode sourceNode;
  private final VisualNode targetNode;
  private final Edge edge;
  private final boolean isSelfLoop;

  private final double edgeLength;

  private PPath edgePPath;

  public VisualEdge(VisualFlowMap visualFlowMap, Edge edge, VisualNode sourceNode, VisualNode targetNode) {
    this.edge = edge;
    this.sourceNode = sourceNode;
    this.targetNode = targetNode;
    this.visualFlowMap = visualFlowMap;
    this.isSelfLoop = visualFlowMap.getFlowMapGraph().isSelfLoop(edge);
    if (isSelfLoop) {
      this.edgeLength = 0;
    } else {
      final double x1 = sourceNode.getValueX();
      final double y1 = sourceNode.getValueY();
      final double x2 = targetNode.getValueX();
      final double y2 = targetNode.getValueY();
      this.edgeLength = GeomUtils.distance(x1, y1, x2, y2);
    }
  }

  /** Must be called by subclasses */
  protected void init() {
    PPath ppath = createEdgePPath();
    setEdgePPath(ppath);
    addChild(ppath);
    updateEdgeWidth();
    addInputEventListener(visualEdgeListener);
  }

  protected abstract PPath createEdgePPath();

  public boolean isSelfLoop() {
    return isSelfLoop;
  }

  protected Shape createSelfLoopShape() {
    Shape shape;

    Rectangle2D b = getSelfLoopBounds();
    shape = new Ellipse2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight());

////    final double size = SELF_LOOP_CIRCLE_SIZE;
////    MinMax xstats = visualFlowMap.getGraphStats().getNodeXStats();
////    MinMax ystats = visualFlowMap.getGraphStats().getNodeYStats();
//    final double size = visualFlowMap.getStats().getEdgeLengthStats().getAvg() / 15;
////
////    final double xsize = (xstats.getMax() - xstats.getMin()) / 20;
////    final double ysize = (ystats.getMax() - ystats.getMin()) / 20;
//    shape = new BSplinePath(Arrays.asList(new Point[] {
//        new Point(x1, y1),
//        new Point(x1 - size/2, y1 + size/2),
//        new Point(x1, y1 + size),
//        new Point(x1 + size/2, y1 + size/2),
//        new Point(x1, y1)
//    }));
    return shape;
  }

  public double getSourceX() {
    return sourceNode.getValueX();
  }

  public double getSourceY() {
    return sourceNode.getValueY();
  }

  public double getTargetX() {
    return targetNode.getValueX();
  }

  public double getTargetY() {
    return targetNode.getValueY();
  }

  protected void setEdgePPath(PPath ppath) {
    this.edgePPath = ppath;
  }

  protected PPath getEdgePPath() {
    return edgePPath;
  }

  public void updateEdgeWidth() {
    updateEdgeWidthTo(getEdgeWeight());
  }

  public void updateEdgeWidthTo(double value) {
    PPath ppath = getEdgePPath();
    if (ppath != null) {
      if (isSelfLoop) {
        ppath.setBounds(getSelfLoopBounds());
        ppath.setStroke(null);
      } else {
        ppath.setStroke(createStroke(normalizeForWidthScale(value)));
      }
    }
    updateVisibilityFor(value);
  }

  public void updateEdgeColors() {
    updateEdgeColorsTo(getEdgeWeight());
  }

  public void updateEdgeColorsTo(double value) {
    PPath ppath = getEdgePPath();
    if (ppath != null) {
      double normValue = normalizeForColorScale(value);
      if (isSelfLoop) {
        ppath.setPaint(createPaint(normValue));
      } else {
        ppath.setStrokePaint(createPaint(normValue));
      }
    }
  }

  private Rectangle2D.Double getSelfLoopBounds() {
    double size =  getSelfLoopSize(Math.max(1, visualFlowMap.getModel().getMaxEdgeWidth()));
    return new Rectangle2D.Double(getSourceX() - size/2, getSourceY() - size/2, size, size);
  }

  private double getSelfLoopSize(double edgeWidth) {
    double value = normalizeForWidthScale(getEdgeWeight());
    if (Double.isNaN(value)) {
      return 0;
    }
    double avgLen = visualFlowMap.getStats().getEdgeLengthStats().getAvg();
    return
      avgLen / MAX_EDGE_WIDTH *
      edgeWidth * value;
  }

//  public abstract void updateEdgeMarkerColors();

  public void updateVisibility() {
    updateVisibilityFor(getEdgeWeight());
  }

  public void updateVisibilityFor(double weight) {
    final VisualFlowMapModel model = visualFlowMap.getModel();
    double weightFilterMin = model.getEdgeWeightFilterMin();
    double weightFilterMax = model.getEdgeWeightFilterMax();

    double edgeLengthFilterMin = model.getEdgeLengthFilterMin();
    double edgeLengthFilterMax = model.getEdgeLengthFilterMax();
    double length = getEdgeLength();

    boolean visible =
        !Double.isNaN(weight)  &&
        weightFilterMin <= weight && weight <= weightFilterMax  &&
        edgeLengthFilterMin <= length && length <= edgeLengthFilterMax
    ;

    if (visible) {
      visible = areNodeClustersVisibile();
    }

    setVisible(visible);
    setPickable(visible);
    setChildrenPickable(visible);
  }

  private boolean areNodeClustersVisibile() {
    boolean visible = true;
    if (visualFlowMap.hasClusters()) {
      VisualNodeCluster srcCluster = visualFlowMap.getNodeCluster(getSourceNode());
      VisualNodeCluster targetCluster = visualFlowMap.getNodeCluster(getTargetNode());

      // TODO: why do we need these null checks here? i.e. why some countries don't have a cluster
      visible = (srcCluster != null  &&  srcCluster.getTag().isVisible())  ||
            (targetCluster != null  &&  targetCluster.getTag().isVisible());
    }
    return visible;
  }

  public Edge getEdge() {
    return edge;
  }

  public VisualFlowMap getVisualFlowMap() {
    return visualFlowMap;
  }

  public String getLabel() {
    return sourceNode.getLabel() + "  -->  " +
         targetNode.getLabel();
  }

  public double getEdgeWeight() {
    return edge.getDouble(visualFlowMap.getFlowWeightAttr());
  }

  public double getEdgeLength() {
    return edgeLength;
  }

  public VisualNode getSourceNode() {
    return sourceNode;
  }

  public VisualNode getTargetNode() {
    return targetNode;
  }

  /**
   * Returns source node if source is true or target node otherwise.
   */
  public VisualNode getNode(boolean source) {
    if (source) {
      return getSourceNode();
    } else {
      return getTargetNode();
    }
  }

  public VisualNode getOppositeNode(VisualNode node) {
    if (targetNode == node) {
      return sourceNode;
    }
    if (sourceNode == node) {
      return targetNode;
    }
    throw new IllegalArgumentException(
        "Node '" + node.getLabel() + "' is neither the source nor the target node of the edge '" + getLabel() + "'");
  }

  @Override
  public String toString() {
    return "VisualEdge{" +
        "label='" + getLabel() + "', " +
        "value=" + getEdgeWeight() +
    '}';
  }

  private double normalizeForWidthScale(double value) {
    return visualFlowMap.getModel().normalizeEdgeWeightForWidthScale(value);
  }

  private double normalizeForColorScale(double value) {
    return visualFlowMap.getModel().normalizeEdgeWeightForColorScale(value);
  }

  private Paint createPaint(double normValue) {
    return visualFlowMap.getVisualEdgePaintFactory().createPaint(
        normValue, getSourceX(), getSourceY(), getTargetX(), getTargetY(),
        edgeLength, isSelfLoop);
  }

  protected Stroke createStroke(double normValue) {
    return visualFlowMap.getVisualEdgeStrokeFactory().createStroke(normValue);
  }

  public void setHighlighted(boolean value, boolean showDirection, boolean asOutgoing) {
    PPath ppath = getEdgePPath();
    if (ppath != null) {
      Paint paint;
      if (value) {
        Color color;
        if (showDirection) {
          if (asOutgoing) {
            color = visualFlowMap.getColor(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT);
          } else {
            color = visualFlowMap.getColor(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT);
          }
        } else {
          color = visualFlowMap.getColor(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT);
        }
        paint = color;
      } else {
        paint = createPaint(normalizeForColorScale(getWidth()));
      }
      ppath.setStrokePaint(paint);
    }
  }

  public void update() {
//    updateVisibility();
    updateEdgeColors();
//    updateEdgeMarkerColors();
    updateEdgeWidth();
  }

  private static final PInputEventListener visualEdgeListener = new PBasicInputEventHandler() {
    @Override
    public void mouseEntered(PInputEvent event) {
      VisualEdge ve = PNodes.getAncestorOfType(event.getPickedNode(), VisualEdge.class);
      if (ve != null) {
        ve.setHighlighted(true, false, false);
        ve.getVisualFlowMap().showTooltip(ve, event.getPosition());
      }
//      node.moveToFront();
    }

    @Override
    public void mouseExited(PInputEvent event) {
      VisualEdge ve = PNodes.getAncestorOfType(event.getPickedNode(), VisualEdge.class);
      if (ve != null) {
        if (!ve.getVisible()) {
          return;
        }
        ve.setHighlighted(false, false, false);
        ve.getVisualFlowMap().hideTooltip();
      }
    }
  };

}
