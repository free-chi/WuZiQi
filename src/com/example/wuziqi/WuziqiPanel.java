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
	
	//����
	private Bitmap mWhitePiece;
	private Bitmap mBlackPiece;
	
	//�����̲���ȷ��֮�󣬹涨���ӵĴ�С����3/4�ı���
	private float ratioPieceOfLineHeight = 3 * 1.0f / 4;
	
	//���ÿ��aciton_down������
	private ArrayList<Point> mWhiteArray = new ArrayList<>();
	private ArrayList<Point> mBlackArray = new ArrayList<>();
	
	//�������֣���ǰ�ֵ�����
	private boolean mIsWhite = true;
	
	//��Ϸ������Ӯ���Ƿ�Ϊ����
	private Boolean mIsGameOver = false;
	private Boolean mIsWhiteWinner;

	public WuziqiPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
//		setBackgroundColor(0x44ff0000);
		init();//��mPaint���г�ʼ��
	}
	
	private void init() {
		
		mPaint.setColor(0x88000000);//���û�����ɫ
		mPaint.setAntiAlias(true);//����ݣ���Ե����
		mPaint.setDither(true);//���÷�����
		mPaint.setStyle(Paint.Style.STROKE);//stroke��
		
		//��ʼ������
		mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
		mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
	}

	/*
	 * onMeasure���������Ǵ�xml�����л�ȡ���ȣ����Ϊmatch���������������ֿ��
	 * */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		int width = Math.min(widthSize, heightSize);
		
		//��ֹ��Ƕ������view���ֵ�ʱ���ȡ����width or heightΪ0,��unspecified������ʾ���˲���
		if(widthMode == MeasureSpec.UNSPECIFIED){
			
			width = heightSize;
		}else if(heightMode == MeasureSpec.UNSPECIFIED){
			
			width = widthSize;
		}

		setMeasuredDimension(width, width);
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// �ڿ�߷����ı��ʱ��ص�
		super.onSizeChanged(w, h, oldw, oldh);
		
		mPanelWidth = w;
		mLineHeight = mPanelWidth * 1.0f / MAX_LINE;
		
//		LogUtil.e("���̿�ȣ� "+mPanelWidth+" �иߣ� "+mLineHeight);
		
		//���Ӹ������̵Ĵ�С�ı��Լ��Ĵ�С
		int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);
		mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
		mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
	}
	
	/*
	 * return true action���𵽵ĺ���Ҫ�����ã����ǵ�����view�з���aciton_down��ʱ�򣬸��߸�����������ʱ��
	 * ����Ȥ�����ҽ����Լ�����View������������action_down���Ƕ���ڹ���View�µĻ������ܻ�����ڹ����ĵط�
	 * ֱ�����ӣ����ԣ�ֻ�ܸ���aciton_up
	 * invalidate();�ػ�
	 * */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
//		LogUtil.e("misGameOver :" +mIsGameOver);//���Ϊnull�����mIsGameOverû�г�ʼ���ᱨ�� NullPointerException�쳣
		
		if(mIsGameOver) {
			
			return false;//����Ϸ����ʱ��view���ٶ�down_up����Ȥ
		}
		int action = event.getAction();
		if(action == MotionEvent.ACTION_UP){
			
			int x = (int) event.getX();
			int y = (int) event.getY();
			
//			LogUtil.e("getx: "+x);
//			LogUtil.e("getY: "+y);
			
			Point p = getValidPoint(x,y);
			/*
			 * �����p��getValidpoint����new������һ�����󣬰���������˵�ǲ��������Array�е�
			 * ���ǣ�conttains(p)�жϵ���view���ڵ����꣬������p�������ԣ���������б�ʹ��
			 * ����return false
			 * */
			if(mWhiteArray.contains(p) || mBlackArray.contains(p)){
				
//				LogUtil.e("������ͬ����ĵ�");
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
	 * �ѱ��浽array�е�������(0,0),(0,1)....������array֮���ж������Ƿ�ʹ�ã�
	 * */
	private Point getValidPoint(int x, int y) {
		
//		LogUtil.e("����λ�� x��"+(int) (x/mLineHeight));
//		LogUtil.e("����λ�� y��"+(int) (y/mLineHeight));

		return new Point((int) (x/mLineHeight),(int)(y/mLineHeight));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawBoard(canvas);//�滭����
		drawPieces(canvas);//��������
		checkGameOver();
	}
	private void checkGameOver() {
	 
		boolean whiteWin = checkFiveInLine(mWhiteArray);
		boolean blackWin = checkFiveInLine(mBlackArray);
		
		if(whiteWin || blackWin){
			
			mIsGameOver = true;
			mIsWhiteWinner = whiteWin;
			
			String text = mIsWhiteWinner?"����ʤ��":"����ʤ��";
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
	 * �ж�x��yλ�õ����ӣ��Ƿ�������ڵ����һ��
	 * */
	private boolean checkHorizontal(int x, int y, ArrayList<Point> points) {
		
		int count = 1;
		//��
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x-i,y))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		//��
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
	 * �ж�x��yλ�õ����ӣ��Ƿ��������ڵ����һ��
	 * */
	private boolean checkVertical(int x, int y, ArrayList<Point> points) {
		
		int count = 1;
		//��
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x,y-i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		//��
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
	 * �ж�x��yλ�õ����ӣ��Ƿ���б�����ڵ����һ��
	 * */
	private boolean checkLeftDiagonal(int x, int y, ArrayList<Point> points) {
		
		int count = 1;
		//��
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x-i,y+i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		//��
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
	 * �ж�x��yλ�õ����ӣ��Ƿ���б�����ڵ����һ��
	 * */
	private boolean checkRightDiagonal(int x, int y, ArrayList<Point> points) {
		
		int count = 1;
		//��
		for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
			if(points.contains(new Point(x-i,y-i))){
				count++;
			}else{
				break;
			}
		}
		if(count == MAX_COUNT_IN_LINE) return true;
		//��
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
		
//		LogUtil.e("��������������� "+mWhiteArray.size());
//		LogUtil.e("��������������� "+mBlackArray.size());
		
		for(int i = 0,n = mWhiteArray.size(); i < n; i++){
			Point whitePoint  = mWhiteArray.get(i);
			canvas.drawBitmap(mWhitePiece, 
					(whitePoint.x + ((1-ratioPieceOfLineHeight)/2))*mLineHeight, 
					(whitePoint.y + ((1-ratioPieceOfLineHeight)/2))*mLineHeight,null);
			
//			LogUtil.e("��������x�� " + ((whitePoint.x + ((1-ratioPieceOfLineHeight)/2))*mLineHeight));
//			LogUtil.e("��������y�� " + ((whitePoint.y + ((1-ratioPieceOfLineHeight)/2))*mLineHeight));
		}
		for(int i = 0,n = mBlackArray.size(); i < n; i++){
			Point blackPoint  = mBlackArray.get(i);
			canvas.drawBitmap(mBlackPiece, 
					(blackPoint.x + ((1-ratioPieceOfLineHeight)/2))*mLineHeight, 
					(blackPoint.y + ((1-ratioPieceOfLineHeight)/2))*mLineHeight,null);
			
//			LogUtil.e("��������x�� " + ((blackPoint.x + ((1-ratioPieceOfLineHeight)/2))*mLineHeight));
//			LogUtil.e("��������y�� " + ((blackPoint.y + ((1-ratioPieceOfLineHeight)/2))*mLineHeight));
		}
		
	}

	private void drawBoard(Canvas canvas) {
		
		int w = mPanelWidth;
		float lineHeight = mLineHeight;
		
//		LogUtil.e("���̿�ȣ� "+w+" �иߣ� "+lineHeight);
		
		for(int i = 0; i < MAX_LINE; i++){
			int startX = (int) (lineHeight/2);
			int endX = (int) (w-lineHeight/2);
			int y = (int) ((0.5+i)*lineHeight);
			canvas.drawLine(startX, y, endX, y, mPaint);
			canvas.drawLine(y, startX, y, endX, mPaint);
//			LogUtil.e("y�ı仯�� "+y);
		}
	}
	
	/*
	 * view�Ĵ洢�ڻָ�
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
	 * ����һ��
	 * */
	public void restart(){
		
		mWhiteArray.clear();
		mBlackArray.clear();
		mIsGameOver = false;
		mIsWhiteWinner = false;
		invalidate();
	}
}
