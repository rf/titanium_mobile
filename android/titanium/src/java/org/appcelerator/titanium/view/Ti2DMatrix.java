/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package org.appcelerator.titanium.view;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import android.graphics.Matrix;

@Kroll.proxy
public class Ti2DMatrix extends KrollProxy
{
	private static final String TAG = "Ti2DMatrix";

	public static final float DEFAULT_ANCHOR_VALUE = -1f;
	public static final float VALUE_UNSPECIFIED = Float.MIN_VALUE;

	protected Ti2DMatrix next, prev;

	protected static class Operation
	{
		protected static final int TYPE_SCALE = 0;
		protected static final int TYPE_TRANSLATE = 1;
		protected static final int TYPE_ROTATE = 2;
		protected static final int TYPE_MULTIPLY = 3;
		protected static final int TYPE_INVERT = 4;

		protected float scaleFromX, scaleFromY, scaleToX, scaleToY;
		protected float translateX, translateY;
		protected float rotateFrom, rotateTo;
		protected float anchorX = 0.5f, anchorY = 0.5f;
		protected Ti2DMatrix multiplyWith;
		protected int type;

		public Operation(int type)
		{
			this.type = type;
		}

		public void apply(float interpolatedTime, Matrix matrix,
			int childWidth, int childHeight, float anchorX, float anchorY)
		{
			anchorX = anchorX == DEFAULT_ANCHOR_VALUE ? this.anchorX : anchorX;
			anchorY = anchorY == DEFAULT_ANCHOR_VALUE ? this.anchorY : anchorY;
			switch (type) {
				case TYPE_SCALE:
					matrix.preScale((interpolatedTime * (scaleToX - scaleFromX)) + scaleFromX,
						(interpolatedTime * (scaleToY - scaleFromY)) + scaleFromY,
						anchorX * childWidth,
						anchorY * childHeight);
					break;
				case TYPE_TRANSLATE:
					matrix.preTranslate(interpolatedTime * translateX, interpolatedTime * translateY); break;
				case TYPE_ROTATE:
					matrix.preRotate((interpolatedTime * (rotateTo - rotateFrom)) + rotateFrom, anchorX * childWidth, anchorY * childHeight); break;
				case TYPE_MULTIPLY:
					matrix.preConcat(multiplyWith.interpolate(interpolatedTime, childWidth, childHeight, anchorX, anchorY)); break;
				case TYPE_INVERT:
					matrix.invert(matrix); break;
			}
		}
	}

	protected Operation op;

	public Ti2DMatrix() {}
	protected Ti2DMatrix(Ti2DMatrix prev, int opType)
	{
		if (prev != null) {
			// this.prev represents the previous matrix. This value does not change.
			this.prev = prev;
			// prev.next is not constant. Subsequent calls to Ti2DMatrix() will alter the value of prev.next.
			prev.next = this;
		}
		this.op = new Operation(opType);
	}

	@Override
	public void handleCreationDict(KrollDict dict)
	{
		super.handleCreationDict(dict);
		if (dict.containsKey(TiC.PROPERTY_ROTATE)) {
			op = new Operation(Operation.TYPE_ROTATE);
			op.rotateFrom = 0;
			op.rotateTo = TiConvert.toFloat(dict, TiC.PROPERTY_ROTATE);
			handleAnchorPoint(dict);
		}
		if (dict.containsKey(TiC.PROPERTY_SCALE)) {
			op = new Operation(Operation.TYPE_SCALE);
			op.scaleFromX = op.scaleFromY = 1.0f;
			op.scaleToX = op.scaleToY = TiConvert.toFloat(dict, TiC.PROPERTY_SCALE);
			handleAnchorPoint(dict);
		}
	}

	protected void handleAnchorPoint(KrollDict dict)
	{
		if (dict.containsKey(TiC.PROPERTY_ANCHOR_POINT)) {
			KrollDict anchorPoint = dict.getKrollDict(TiC.PROPERTY_ANCHOR_POINT);
			if (anchorPoint != null) {
				op.anchorX = TiConvert.toFloat(anchorPoint, TiC.PROPERTY_X);
				op.anchorY = TiConvert.toFloat(anchorPoint, TiC.PROPERTY_Y);
			}
		}
	}

	@Kroll.method
	public Ti2DMatrix translate(double x, double y)
	{
		Ti2DMatrix newMatrix = new Ti2DMatrix(this, Operation.TYPE_TRANSLATE);
		newMatrix.op.translateX = (float) x;
		newMatrix.op.translateY = (float) y;
		return newMatrix;
	}

	@Kroll.method
	public Ti2DMatrix scale(Object args[])
	{
		Ti2DMatrix newMatrix = new Ti2DMatrix(this, Operation.TYPE_SCALE);
		newMatrix.op.scaleFromX = newMatrix.op.scaleFromY = VALUE_UNSPECIFIED;
		newMatrix.op.scaleToX = newMatrix.op.scaleToY = 1.0f;
		// varargs for API backwards compatibility
		if (args.length == 4) {
			// scale(fromX, fromY, toX, toY)
			newMatrix.op.scaleFromX = TiConvert.toFloat(args[0]);
			newMatrix.op.scaleFromY = TiConvert.toFloat(args[1]);
			newMatrix.op.scaleToX = TiConvert.toFloat(args[2]);
			newMatrix.op.scaleToY = TiConvert.toFloat(args[3]);
		}
		if (args.length == 2) {
			// scale(toX, toY)
			newMatrix.op.scaleToX = TiConvert.toFloat(args[0]);
			newMatrix.op.scaleToY = TiConvert.toFloat(args[1]);
		} else if (args.length == 1) {
			// scale(scaleFactor)
			newMatrix.op.scaleToX = newMatrix.op.scaleToY = TiConvert.toFloat(args[0]);
		}
		// TODO newMatrix.handleAnchorPoint(newMatrix.getProperties());
		return newMatrix;
	}

	@Kroll.method
	public Ti2DMatrix rotate(Object[] args)
	{
		Ti2DMatrix newMatrix = new Ti2DMatrix(this, Operation.TYPE_ROTATE);

		if (args.length == 1) {
			newMatrix.op.rotateFrom = VALUE_UNSPECIFIED;
			newMatrix.op.rotateTo = TiConvert.toFloat(args[0]);
		} else if (args.length == 2) {
			newMatrix.op.rotateFrom = TiConvert.toFloat(args[0]);
			newMatrix.op.rotateTo = TiConvert.toFloat(args[1]);
		}
		// TODO newMatrix.handleAnchorPoint(newMatrix.getProperties());
		return newMatrix;
	}

	@Kroll.method
	public Ti2DMatrix invert()
	{
		return new Ti2DMatrix(this, Operation.TYPE_INVERT);
	}

	@Kroll.method
	public Ti2DMatrix multiply(Ti2DMatrix other)
	{
		Ti2DMatrix newMatrix = new Ti2DMatrix(other, Operation.TYPE_MULTIPLY);
		newMatrix.op.multiplyWith = this;
		return newMatrix;
	}
	
	@Kroll.method
	public float[] finalValuesAfterInterpolation (int width, int height)
	{
		Matrix m = interpolate(1f, width, height, 0.5f, 0.5f);
		float[] result = new float[9];
		m.getValues(result);
		return result;
	}

	public Matrix interpolate(float interpolatedTime, int childWidth, int childHeight, float anchorX, float anchorY)
	{
		Ti2DMatrix first = this;
		ArrayList<Ti2DMatrix> preMatrixList = new ArrayList<Ti2DMatrix>();
		
		while (first.prev != null)
		{
			first = first.prev;
			// It is safe to use prev matrix to trace back the transformation matrix list,
			// since prev matrix is constant.
			preMatrixList.add(0, first);
		}

		Matrix matrix = new Matrix();
		for (Ti2DMatrix current : preMatrixList) {
			if (current.op != null) {
				current.op.apply(interpolatedTime, matrix, childWidth, childHeight, anchorX, anchorY);
			}
		}
		if (op != null) {
			op.apply(interpolatedTime, matrix, childWidth, childHeight, anchorX, anchorY);
		}
		return matrix;
	}

	public boolean isScaleOperation()
	{
		if (this.op == null) {
			return false;
		}
		return (this.op.type == Operation.TYPE_SCALE);
	}

	public boolean isRotateOperation()
	{
		if (this.op == null) {
			return false;
		}
		return (this.op.type == Operation.TYPE_ROTATE);
	}

	public float[] getScaleOperationParameters()
	{
		if (!isScaleOperation()) {
			Log.w(TAG, "getScaleOperationParameters called though matrix is not for a scale operation.");
			return new float[6];
		}

		return new float[] {
			this.op.scaleFromX,
			this.op.scaleToX,
			this.op.scaleFromY,
			this.op.scaleToY,
			this.op.anchorX,
			this.op.anchorY
		};
	}

	public float[] getRotateOperationParameters()
	{
		if (!isRotateOperation()) {
			Log.w(TAG, "getRotateOperationParameters called though matrix is not for a scale operation.");
			return new float[4];
		}

		return new float[] {
			this.op.rotateFrom,
			this.op.rotateTo,
			this.op.anchorX,
			this.op.anchorY
		};
	}

	public void setRotationFromDegrees(float degrees)
	{
		if (this.op != null) {
			this.op.rotateFrom = degrees;
		}
	}
}
