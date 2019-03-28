package com.example.administrator.notes;

/*
	
*/

public class GetCutBitmapLocation {
	private float cutLeft = 0;
	private float cutTop = 0;
	private float cutRight = 0;
	private float cutBottom = 0;
	
	public void init(float x,float y){
		cutLeft = x;
		cutRight = x;
		cutTop = y;
		cutBottom = y;
	}
	public void setCutLeftAndRight(float x,float y){
		
		cutLeft = (x < cutLeft ? x : cutLeft);
		cutRight = (x > cutRight ? x : cutRight);
		cutTop = (y < cutTop ? y : cutTop);
		cutBottom = (y > cutBottom ? y : cutBottom);
	}
	

	public float getCutLeft(){
		return cutLeft;
	}
	public float getCutTop(){
		return cutTop;
	}
	public float getCutRight(){
		return cutRight;
	}
	public float getCutBottom(){
		return cutBottom;
	}
	
}
