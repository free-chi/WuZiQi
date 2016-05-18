package com.example.wuziqi;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class WuziqiPanel extends View{
	
	private int mPanelWidth;
	private final int MAX_LINE = 10;
	private final int MAX_COUNT_IN_LINE = 5;
	private float mLineHeight;
	
	private Paint mPaint= new Paint();
	
	//棋子
	private Bitmap mWhitePiece;
	private Bitmap mBlackPiece;
	
	//在棋盘布局确定之后，规定棋子的大小比例3/4的比例
	private float ratioPieceOfLineHeight = 3 * 1.0f / 4;
	
	//存放每次aciton_down的坐标
	private ArrayList<Point> mWhiteArray = new ArrayList<>();
	private ArrayList<Point> mBlackArray = new ArrayList<>();
	
	//白棋先手，当前轮到白棋
	private boolean mIsWhite = true;
	
	//游戏结束，赢家是否为白棋
	private Boolean mIsGameOver = false;
	private Boolean mIsWhiteWinner;

	public WuziqiPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
//		setBackgroundColor(0x44ff0000);
		init();//对mPaint进行初始化
	}
	
	private void init() {
		
		mPaint.setColor(0x88000000);//设置画笔颜色
		mPaint.setAntiAlias(true);//抗锯齿，边缘清晰
		mPaint.setDither(true);//设置防抖动
		mPaint.setStyle(Paint.Style.STROKE);//stroke线
		
		//初始化棋子
		mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
		mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
	}

	/*
	 * onMeasure测量方法是从xml布局中获取长度，如果为match，就是整个父布局宽度
	 * */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		int width = Math.min(widthSize, heightSize);
		
		//防止在嵌套其他view布局的时候获取到的width or height为0,（unspecified），显示不了布局
		if(widthMode == MeasureSpec.UNSPECIFIED){
			
			width = heightSize;
		}else if(heightMode == MeasureSpec.UNSPECIFIED){
			
			width = widthSize;
		}

		setMeasuredDimension(width, width);
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// 在宽高发生改变的时候回调
		super.onSizeChanged(w, h, oldw, oldh);
		
		mPanelWidth = w;
		mLineHeight = mPanelWidth * 1.0f / MAX_LINE;
		
//		LogUtil.e("棋盘宽度： "+mPanelWidth+" 行高： "+mLineHeight);
		
		//棋子根据棋盘的大小改变自己的大小
		int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);
		mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
		mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
	}
	
	/*
	 * return true action中起到的很重要的作用，就是当在字view中发生aciton_down的时候，告诉父类对这个触发时间
	 * 感兴趣，并且交给自己（字View）来处理，但是action_down如果嵌套在滚动View下的话，可能会出错，在滚动的地方
	 * 直接下子，所以，只能改用aciton_up
	 * invalidate();重绘
	 * */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
//		LogUtil.e("misGameOver :" +mIsGameOver);//结果为null，如果mIsGameOver没有初始化会报错 NullPointerException异常
		
		if(mIsGameOver) {
			
			return false;//当游戏结束时，view不再对down_up感兴趣
		}
		int action = event.getAction();
		if(action == MotionEvent.ACTION_UP){
			
			int x = (int) event.getX();
			int y = (int) event.getY();
			
//			LogUtil.e("getx: "+x);
//			LogUtil.e("getY: "+y);
			
			Point p = getValidPoint(x,y);
			/*
			 * 这里的p是getValidpoint方法new出来的一个对象，按照正常来说是不会包含在Array中的
			 * 但是，conttains(p)判断的是view对于的坐标，而不是p对象，所以，如果坐标有被使用
			 * 就是return false
			 * */
			if(mWhiteArray.contains(p) || mBlackArray.contains(p)){
				
//				LogUtil.e("存在相同坐标的点");
//				LogUtil.e("mWhiteArray.contains(p) : " + mWhiteArray.contains(p));
//				LogUtil.e("mBlackArray.contains(p) : " + mBlackArray.contains(p));
				
				return false;
			}
			if(mIsWhite){
				mWhiteArray.add(p);
			}else{
				mBlackArray.add(p);
			}
			invalidate(); 
			mIsWhite = !mIsWhite;
		}
		return true;
	}
	/*
	 * 把保存到array中的坐标变成(0,0),(0,1)....方便存进array之后判断坐标是否被使用，
	 * */
	private Point getValidPoint(int x, int y) {
		
//		LogUtil.e("棋子位置 x："+(int) (x/mLineHeight));
//		LogUtil.e("棋子位置 y："+(int) (y/mLineHeight));

		return new Point((int) (x/mLineHeight),(int)(y/mLineHeight));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawBoard(canvas);//绘画棋盘
		drawPieces(canvas);//绘制棋子
		checkGameOver();
	}
	private void checkGameOver() {
	 
		boolean whiteWin = checkFiveInLine(mWhiteArray);
		boolean blackWin = checkFiveInLine(mBlackArray);
		
		if(whiteWin || blackWin){
			
			mIsGameOver = true;
			mIsWhiteWinner = whiteWin;
			
			String text = mIsWhiteWinner?"白棋胜利":"黑棋胜利";
			Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
		}
	}

	private boolean checkFiveInLine(ArrayList<Point> points) {
		 
		for(Point p : points){
			int x = p.x;
			int y = p.y;
			boolean win = checkHorizontal(x,y,points);
			if(win) return true;
			win = checkVertical(x, y, points);
			if(win) return true;
			win = checkLeftDiagonal(x, y, points);
			if(win) return true;
			win = checkRightDiagonal(x, y, points);
			if(win) return true;
		}
		return false;
	}
	/*
	 * 判断x，y位置的棋子，是否横向相邻的五个一致
	 * */
	private boolean checkHorizontal(int x, int y, ArrayList<Point> points) {
		
		int count = 1;
		//左
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x-i,y))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		//右
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x+i,y))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		return false;
		
	}
	/*
	 * 判断x，y位置的棋子，是否纵向相邻的五个一致
	 * */
	private boolean checkVertical(int x, int y, ArrayList<Point> points) {
		
		int count = 1;
		//上
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x,y-i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		//下
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x,y+i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		return false;
		
	}
	/*
	 * 判断x，y位置的棋子，是否左斜向相邻的五个一致
	 * */
	private boolean checkLeftDiagonal(int x, int y, ArrayList<Point> points) {
		
		int count = 1;
		//左
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x-i,y+i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		//右
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x+i,y-i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		return false;
		
	}
	/*
	 * 判断x，y位置的棋子，是否右斜向相邻的五个一致
	 * */
	private boolean checkRightDiagonal(int x, int y, ArrayList<Point> points) {
		
		int count = 1;
		//左
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x-i,y-i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		//右
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x+i,y+i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		return false;
		
	}

	private void drawPieces(Canvas canvas) {
		
//		LogUtil.e("白棋坐标组个数： "+mWhiteArray.size());
//		LogUtil.e("黑棋坐标组个数： "+mBlackArray.size());
		
		for(int i = 0,n = mWhiteArray.size(); i < n; i++){
			Point whitePoint  = mWhiteArray.get(i);
			canvas.drawBitmap(mWhitePiece, 
					(whitePoint.x + ((1-ratioPieceOfLineHeight)/2))*mLineHeight, 
					(whitePoint.y + ((1-ratioPieceOfLineHeight)/2))*mLineHeight,null);
			
//			LogUtil.e("白棋坐标x： " + ((whitePoint.x + ((1-ratioPieceOfLineHeight)/2))*mLineHeight));
//			LogUtil.e("白棋坐标y： " + ((whitePoint.y + ((1-ratioPieceOfLineHeight)/2))*mLineHeight));
		}
		for(int i = 0,n = mBlackArray.size(); i < n; i++){
			Point blackPoint  = mBlackArray.get(i);
			canvas.drawBitmap(mBlackPiece, 
					(blackPoint.x + ((1-ratioPieceOfLineHeight)/2))*mLineHeight, 
					(blackPoint.y + ((1-ratioPieceOfLineHeight)/2))*mLineHeight,null);
			
//			LogUtil.e("黑棋坐标x： " + ((blackPoint.x + ((1-ratioPieceOfLineHeight)/2))*mLineHeight));
//			LogUtil.e("黑棋坐标y： " + ((blackPoint.y + ((1-ratioPieceOfLineHeight)/2))*mLineHeight));
		}
		
	}

	private void drawBoard(Canvas canvas) {
		
		int w = mPanelWidth;
		float lineHeight = mLineHeight;
		
//		LogUtil.e("棋盘宽度： "+w+" 行高： "+lineHeight);
		
		for(int i = 0; i < MAX_LINE; i++){
			int startX = (int) (lineHeight/2);
			int endX = (int) (w-lineHeight/2);
			int y = (int) ((0.5+i)*lineHeight);
			canvas.drawLine(startX, y, endX, y, mPaint);
			canvas.drawLine(y, startX, y, endX, mPaint);
//			LogUtil.e("y的变化： "+y);
		}
	}
	
	/*
	 * view的存储于恢复
	 * */
	private static final String INSTANCE = "instance";
	private static final String INSTANCE_GAME_OVER = "instance_game_over";
	private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
	private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";
	@Override
	protected Parcelable onSaveInstanceState() {
		 
		Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
		bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
		bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
		bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
		return bundle;
	}
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		
		if(state instanceof Bundle){
			Bundle bundle = (Bundle) state;
			mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
			mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
			mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
			super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
			return ;
		}
		
		super.onRestoreInstanceState(state);
	}
	/*
	 * 再来一局
	 * */
	public void restart(){
		
		mWhiteArray.clear();
		mBlackArray.clear();
		mIsGameOver = false;
		mIsWhiteWinner = false;
		invalidate();
	}
}
