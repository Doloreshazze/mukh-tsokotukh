package com.doloreshazze.mukhtsokotukh;

import android.app.*; import android.os.*; import android.view.*; import android.graphics.*; import android.graphics.drawable.*; import android.media.*; import java.util.*;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setFlags(1024,1024);setContentView(new GameView());}
  class GameView extends View {
    Paint p=new Paint(3); Bitmap bg; float x=160,y=350,vx=0,vy=0,cam=0; boolean left,right,jump,dead=false,won=false; int coins=0; long last=0; Random rnd=new Random(); ArrayList<Float> coinX=new ArrayList<>();
    float[] traps={620,1040,1510,2070}; float[] webs={820,1300,1810}; final float ground=405;
    GameView(){super(MainActivity.this); bg=BitmapFactory.decodeResource(getResources(),R.drawable.garden_background); for(int i=0;i<18;i++)coinX.add(340+i*108f); p.setTypeface(Typeface.create("sans",Typeface.BOLD)); setFocusable(true);}
    void sound(int tone){try{ToneGenerator t=new ToneGenerator(AudioManager.STREAM_MUSIC,45);t.startTone(tone,80); new Handler().postDelayed(t::release,110);}catch(Exception e){}}
    protected void onDraw(Canvas c){super.onDraw(c); long n=System.currentTimeMillis(); float dt=last==0?.016f:Math.min(.04f,(n-last)/1000f);last=n; int w=getWidth(),h=getHeight();
      c.drawColor(Color.rgb(111,222,245)); Rect dst=new Rect(0,0,w,h); c.drawBitmap(bg,null,dst,p);
      if(!dead&&!won) update(dt); cam=Math.max(0,x-w*.35f); c.save(); c.translate(-cam,0); drawWorld(c,w,h); c.restore(); drawHud(c,w,h); if(!dead&&!won){invalidate();}
    }
    void update(float d){float speed=240; vx=left?-speed:right?speed:0; x+=vx*d; if(jump&&y>=ground-1){vy=-550;sound(ToneGenerator.TONE_PROP_BEEP2);} jump=false; vy+=1250*d; y+=vy*d; if(y>ground){y=ground;vy=0;} if(vy>0&&y<ground&&right){vy-=600*d;} // tiny flight while holding right in air
      for(int i=coinX.size()-1;i>=0;i--)if(Math.abs((coinX.get(i)+18)-x)<35&&Math.abs(y-ground)<85){coinX.remove(i);coins++;sound(ToneGenerator.TONE_PROP_ACK);}
      for(float t:traps)if(Math.abs(x-t)<35&&y>330)hit(); for(float t:webs)if(Math.abs(x-t)<32&&y>300)hit(); if(x>2250&&coins>=10)won=true;
    }
    void hit(){dead=true;sound(ToneGenerator.TONE_PROP_NACK);}
    void drawWorld(Canvas c,int w,int h){p.setStyle(Paint.Style.FILL); // platforms
      p.setColor(Color.rgb(86,112,63));c.drawRoundRect(0,435,2550,500,18,18,p);p.setColor(Color.rgb(185,222,100));c.drawRoundRect(0,425,2550,450,16,16,p);
      for(float q:coinX) coin(c,q,360); for(float t:traps) swatter(c,t,390); for(float t:webs) web(c,t,395); kiosk(c,2260,260); hero(c,x,y);
    }
    void coin(Canvas c,float a,float b){p.setColor(Color.rgb(255,193,32));c.drawCircle(a,b,16,p);p.setColor(Color.WHITE);p.setTextSize(20);p.setTextAlign(Paint.Align.CENTER);c.drawText("₵",a,b+7,p);}
    void web(Canvas c,float a,float b){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(Color.argb(210,245,250,255));for(int i=0;i<4;i++)c.drawCircle(a,b,i*13,p);c.drawLine(a-42,b,a+42,b,p);c.drawLine(a,b-42,a,b+12,p);p.setStyle(Paint.Style.FILL);}
    void swatter(Canvas c,float a,float b){p.setColor(Color.rgb(115,74,42));c.rotate(-25,a,b);c.drawRoundRect(a-5,b-95,a+10,b,8,8,p);p.setColor(Color.rgb(236,88,75));c.drawRoundRect(a-37,b-135,a+42,b-85,15,15,p);p.setColor(Color.rgb(255,187,176));for(int i=-20;i<=20;i+=20)for(int j=-120;j<=-100;j+=15)c.drawCircle(a+i,b+j,4,p);c.rotate(25,a,b);}
    void kiosk(Canvas c,float a,float b){p.setColor(Color.rgb(86,166,133));c.drawRoundRect(a-90,b,a+90,430,10,10,p);p.setColor(Color.rgb(255,209,72));c.drawRoundRect(a-112,b-32,a+112,b+8,18,18,p);p.setColor(Color.WHITE);p.setTextSize(22);p.setTextAlign(Paint.Align.CENTER);c.drawText("ЛИМОНАД · 10",a,b-6,p);p.setColor(Color.rgb(255,213,62));c.drawRect(a-48,b+40,a-3,b+105,p);p.setColor(Color.WHITE);c.drawCircle(a-26,b+50,11,p);}
    void hero(Canvas c,float a,float b){float bob=(float)Math.sin(System.currentTimeMillis()/100.0)*3;p.setColor(Color.argb(145,220,245,255));c.drawOval(a-35,b-72+bob,a,b-40+bob,p);c.drawOval(a,b-72+bob,a+35,b-40+bob,p);p.setColor(Color.rgb(46,52,67));c.drawOval(a-25,b-60+bob,a+25,b-12+bob,p);p.setColor(Color.rgb(255,204,61));c.drawCircle(a-10,b-42+bob,4,p);c.drawCircle(a+10,b-42+bob,4,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(Color.WHITE);c.drawCircle(a-10,b-45+bob,11,p);c.drawCircle(a+10,b-45+bob,11,p);c.drawLine(a+1,b-45+bob,a-1,b-45+bob,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(230,65,61));c.drawRect(a-28,b-25+bob,a+34,b-15+bob,p);c.drawCircle(a,b-4+bob,11,p);}
    void drawHud(Canvas c,int w,int h){p.setTextAlign(Paint.Align.LEFT);p.setTextSize(30);p.setColor(Color.WHITE);c.drawText("МУХ ЦОКОТУХ",28,45,p);p.setTextSize(25);c.drawText("Монеты: "+coins+" / 10",30,78,p); button(c,45,h-105,100,"◀");button(c,160,h-105,100,"▶");button(c,w-150,h-105,105,"↑"); if(dead||won){p.setColor(Color.argb(215,31,50,60));c.drawRoundRect(w*.22f,h*.22f,w*.78f,h*.70f,28,28,p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(40);p.setColor(Color.WHITE);c.drawText(won?"Лимонад получен!":"Ой! Ловушка!",w/2,h*.40f,p);p.setTextSize(25);c.drawText(won?"Ты спас жаркий день.":"Нажми здесь, чтобы попробовать снова",w/2,h*.52f,p);}}
    void button(Canvas c,float a,float b,float s,String z){a=z.equals("◀")?getWidth()*.09f:z.equals("▶")?getWidth()*.27f:getWidth()*.83f;b=getHeight()-110;s=Math.min(180,getWidth()*.16f);p.setColor(Color.argb(220,255,255,255));c.drawRoundRect(a-s/2,b-85,a+s/2,b+85,26,26,p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(56);p.setColor(Color.rgb(52,96,97));c.drawText(z,a,b+18,p);}
    public boolean onTouchEvent(android.view.MotionEvent e){
      int action=e.getActionMasked();
      if((dead||won)&&action==MotionEvent.ACTION_DOWN){dead=won=false;left=right=jump=false;x=160;y=350;vy=vx=cam=0;last=0;coins=0;coinX.clear();for(int i=0;i<18;i++)coinX.add(340+i*108f);invalidate();return true;}
      left=right=false;
      if(action==MotionEvent.ACTION_CANCEL||action==MotionEvent.ACTION_UP){invalidate();return true;}
      for(int i=0;i<e.getPointerCount();i++){
        if(action==MotionEvent.ACTION_POINTER_UP&&i==e.getActionIndex())continue;
        float px=e.getX(i),py=e.getY(i);
        if(py>getHeight()-220){if(px<getWidth()*.18f)left=true;else if(px<getWidth()*.36f)right=true;else if(px>getWidth()*.65f&&(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_POINTER_DOWN))jump=true;}
      }
      invalidate();return true;
    }
  }
}
