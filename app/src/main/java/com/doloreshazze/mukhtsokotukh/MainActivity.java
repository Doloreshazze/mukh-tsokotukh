package com.doloreshazze.mukhtsokotukh;

import android.app.*; import android.os.*; import android.view.*; import android.graphics.*; import android.graphics.drawable.*; import android.media.*; import java.util.*;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setFlags(1024,1024);setContentView(new GameView());}
  class GameView extends View {
    Paint p=new Paint(3); Bitmap bg; float x=160,y=350,vx=0,vy=0,cam=0; boolean left,right,jump,dead=false,won=false; int coins=0; long last=0; Random rnd=new Random(); ArrayList<Float> coinX=new ArrayList<>();
    float[] traps={620,1040,1510,2070}; float[] webs={820,1300,1810}; final float ground=405;
    boolean immortal=false;float clock=0,idle=0;ArrayList<float[]> pickups=new ArrayList<>();
    float strike(float a){return (clock+a*.003f)%2.8f;}
    float spiderY(float a){return 195+210*(.5f+.5f*(float)Math.sin(clock*.9f+a));}
    GameView(){super(MainActivity.this); bg=BitmapFactory.decodeResource(getResources(),R.drawable.garden_background); for(int i=0;i<18;i++)coinX.add(340+i*108f); p.setTypeface(Typeface.create("sans",Typeface.BOLD)); setFocusable(true);}
    void sound(int tone){try{ToneGenerator t=new ToneGenerator(AudioManager.STREAM_MUSIC,45);t.startTone(tone,80); new Handler().postDelayed(t::release,110);}catch(Exception e){}}
    protected void onDraw(Canvas c){super.onDraw(c); long n=System.currentTimeMillis(); float dt=last==0?.016f:Math.min(.04f,(n-last)/1000f);last=n; int w=getWidth(),h=getHeight();
      if(!dead&&!won) update(dt);float scale=h/600f;cam=Math.max(0,x-w/scale*.35f);
      drawScenery(c,w,h,scale);
      c.save();c.translate(0,h*.80f-(ground+21)*scale);c.scale(scale,scale);c.translate(-cam,0);drawWorld(c,w,h);c.restore();
      drawLeaves(c,w,h,scale);drawHud(c,w,h);p.setTextAlign(Paint.Align.RIGHT);p.setTextSize(24);p.setColor(immortal?Color.GREEN:Color.WHITE);c.drawText(immortal?"DEV: БЕССМЕРТИЕ ВКЛ":"DEV: БЕССМЕРТИЕ ВЫКЛ",w-24,40,p);if(!dead&&!won){postInvalidateOnAnimation();}
    }
    void update(float d){float speed=240; vx=left?-speed:right?speed:0; x+=vx*d; if(jump&&y>=ground-1){vy=-550;sound(ToneGenerator.TONE_PROP_BEEP2);} jump=false; vy+=1250*d; y+=vy*d; if(y>ground){y=ground;vy=0;} if(vy>0&&y<ground&&right){vy-=600*d;} // tiny flight while holding right in air
      clock+=d;idle=Math.abs(vx)<1&&y>=ground-1?idle+d:0;
      for(int i=pickups.size()-1;i>=0;i--){pickups.get(i)[1]+=d;if(pickups.get(i)[1]>=.65f)pickups.remove(i);}
      for(int i=coinX.size()-1;i>=0;i--)if(Math.abs(coinX.get(i)-x)<48&&Math.abs(y-ground)<40){pickups.add(new float[]{coinX.remove(i),0});coins++;sound(ToneGenerator.TONE_PROP_ACK);}
      for(float t:traps)if(strike(t)>.95f&&strike(t)<1.25f&&Math.abs(x-t)<65&&y>280)hit();
      for(float t:webs)if(Math.abs(x-t)<37&&spiderY(t)>y-145&&spiderY(t)<y+18)hit();if(x>2250&&coins>=10)won=true;
    }
    AudioTrack buzz; boolean buzzing=false;
    void buzz(boolean active){
      if(active==buzzing)return;
      try{
        if(active){
          if(buzz==null){int rate=22050;short[] wave=new short[2205];for(int i=0;i<wave.length;i++){double t=i/(double)rate;wave[i]=(short)(2300*(Math.sin(2*Math.PI*220*t)+.35*Math.sin(2*Math.PI*440*t))*(.75+.25*Math.sin(2*Math.PI*30*t)));}buzz=new AudioTrack(AudioManager.STREAM_MUSIC,rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,wave.length*2,AudioTrack.MODE_STATIC);buzz.write(wave,0,wave.length);buzz.setLoopPoints(0,wave.length,-1);}buzz.play();
        }else if(buzz!=null){buzz.pause();buzz.reloadStaticData();}
        buzzing=active;
      }catch(Exception ignored){}
    }
    @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(visibility!=VISIBLE)buzz(false);}
    @Override protected void onDetachedFromWindow(){buzz(false);if(buzz!=null){buzz.release();buzz=null;}super.onDetachedFromWindow();}
    void hit(){if(immortal)return;dead=true;buzz(false);sound(ToneGenerator.TONE_PROP_NACK);}
    BitmapShader skyShader,gardenShader,pathShader;Bitmap skyImage,gardenImage,pathImage;
    void layer(Canvas c,BitmapShader shader,Bitmap image,float top,float bottom,float offset){
      // Scale from the actual decoded pixels, including Android resource density.
      float sy=(bottom-top)/image.getHeight();float sx=Math.max(sy,getWidth()/(float)image.getWidth());
      Matrix matrix=new Matrix();matrix.setScale(sx,sy);matrix.postTranslate(-offset,top);shader.setLocalMatrix(matrix);p.setShader(shader);c.drawRect(0,top,getWidth(),bottom,p);p.setShader(null);
    }
    // Full independent artwork with transparent foliage, never slices of a scene.
    void drawScenery(Canvas c,int w,int h,float scale){
      p.setColor(Color.WHITE);p.setAlpha(255);p.setFilterBitmap(true);
      if(skyShader==null){
        BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
        skyImage=BitmapFactory.decodeResource(getResources(),R.drawable.sky_layer,options);gardenImage=BitmapFactory.decodeResource(getResources(),R.drawable.garden_layer,options);pathImage=BitmapFactory.decodeResource(getResources(),R.drawable.path_layer,options);
        skyShader=new BitmapShader(skyImage,Shader.TileMode.MIRROR,Shader.TileMode.CLAMP);gardenShader=new BitmapShader(gardenImage,Shader.TileMode.MIRROR,Shader.TileMode.CLAMP);pathShader=new BitmapShader(pathImage,Shader.TileMode.MIRROR,Shader.TileMode.CLAMP);
      }
      // Mirrored repeat guarantees identical boundary pixels, even at different speeds.
      layer(c,skyShader,skyImage,0,h*.73f,cam*scale*.15f);
      layer(c,pathShader,pathImage,h*.64f,h,cam*scale);
      layer(c,gardenShader,gardenImage,0,h*.73f,cam*scale*.48f);
      // Soft contact shade where the planted wall meets the paving.
      p.setShader(new LinearGradient(0,h*.725f,0,h*.755f,Color.argb(65,65,79,40),Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawRect(0,h*.725f,w,h*.755f,p);p.setShader(null);
    }
    void drawLeaves(Canvas c,int w,int h,float scale){
      float time=(System.nanoTime()/1000000L%600000)/1000f;
      for(int i=-1;i<9;i++){
        float spacing=340*scale;float base=i*spacing-(cam*scale*1.35f)%spacing;
        c.save();c.translate(base,h+20*scale);c.rotate((float)Math.sin(time*1.7f+i*.9f)*5-12);
        p.setColor(Color.rgb(56,98,46));p.setStrokeWidth(5*scale);c.drawLine(0,0,26*scale,-135*scale,p);
        for(int j=0;j<4;j++){
          c.save();c.translate(j*6*scale,-j*30*scale);c.rotate((j%2==0?-40:48)+(float)Math.sin(time*2+j+i)*7);
          Path leaf=new Path();leaf.moveTo(0,0);leaf.cubicTo(-24*scale,-40*scale,-18*scale,-65*scale,0,-82*scale);leaf.cubicTo(28*scale,-62*scale,27*scale,-23*scale,0,0);
          p.setColor(j%2==0?Color.rgb(82,137,55):Color.rgb(115,157,62));c.drawPath(leaf,p);
          p.setColor(Color.rgb(172,190,92));p.setStrokeWidth(1.5f*scale);c.drawLine(0,-5*scale,0,-72*scale,p);c.restore();
        }c.restore();
      }
    }
    void drawWorld(Canvas c,int w,int h){p.setStyle(Paint.Style.FILL);
      for(float q:coinX){p.setColor(Color.argb(65,50,40,20));c.drawOval(q-19,ground+12,q+19,ground+22,p);c.save();c.scale(1,.38f,q,ground+10);coin(c,q,ground+10);c.restore();}
      for(float t:traps) swatter(c,t,390);for(float t:webs)web(c,t,165);kiosk(c,2260,260);hero(c,x,y);
      for(float[] f:pickups){float u=Math.min(1,f[1]/.65f);float cx=f[0]+(x+29-f[0])*u;float cy=(ground+10)*(1-u)+(y-48)*u-100*(float)Math.sin(Math.PI*u);c.save();c.scale(1-u*.65f,1-u*.65f,cx,cy);coin(c,cx,cy);c.restore();}
    }
    void coin(Canvas c,float a,float b){p.setColor(Color.rgb(255,193,32));c.drawCircle(a,b,16,p);p.setColor(Color.WHITE);p.setTextSize(20);p.setTextAlign(Paint.Align.CENTER);c.drawText("₵",a,b+7,p);}
    void web(Canvas c,float a,float b){
      p.setColor(Color.rgb(105,73,48));p.setStrokeWidth(11);c.drawLine(a-75,b-59,a+65,b-50,p);c.drawLine(a+40,b-53,a+75,b-78,p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);p.setColor(Color.argb(220,240,248,248));
      for(int i=1;i<=4;i++)c.drawCircle(a,b,i*13,p);for(int i=0;i<8;i++){double angle=i*Math.PI/4;c.drawLine(a,b,a+52*(float)Math.cos(angle),b+52*(float)Math.sin(angle),p);}
      float sy=spiderY(a);c.drawLine(a,b,a,sy,p);p.setColor(Color.rgb(62,45,69));p.setStrokeWidth(2.5f);
      for(int i=0;i<4;i++){float dy=(i-1.5f)*5;float kick=(float)Math.sin(clock*7+i)*3;for(int side=-1;side<=1;side+=2){c.drawLine(a+side*5,sy+dy,a+side*17,sy+dy-6+kick,p);c.drawLine(a+side*17,sy+dy-6+kick,a+side*22,sy+dy+4,p);}}
      p.setStyle(Paint.Style.FILL);c.drawOval(a-9,sy-12,a+9,sy+9,p);c.drawCircle(a,sy+7,7,p);p.setColor(Color.WHITE);c.drawCircle(a-3,sy+8,2,p);c.drawCircle(a+3,sy+8,2,p);
    }
    void swatter(Canvas c,float a,float b){float phase=strike(a);float angle=phase<.95f?-70:phase<1.1f?-70+160*(phase-.95f)/.15f:phase<1.25f?90:90-160*Math.min(1,(phase-1.25f)/.65f);c.save();c.translate(a,b-110);c.rotate(angle);p.setColor(Color.rgb(115,74,42));c.drawRoundRect(-5,-10,8,100,6,6,p);p.setColor(Color.rgb(236,88,75));c.drawRoundRect(-40,85,40,140,12,12,p);p.setColor(Color.rgb(255,187,176));for(int i=-25;i<=25;i+=16)for(int j=96;j<135;j+=14)c.drawCircle(i,j,4,p);c.restore();}
    void kiosk(Canvas c,float a,float b){p.setColor(Color.rgb(86,166,133));c.drawRoundRect(a-90,b,a+90,430,10,10,p);p.setColor(Color.rgb(255,209,72));c.drawRoundRect(a-112,b-32,a+112,b+8,18,18,p);p.setColor(Color.WHITE);p.setTextSize(22);p.setTextAlign(Paint.Align.CENTER);c.drawText("ЛИМОНАД · 10",a,b-6,p);p.setColor(Color.rgb(255,213,62));c.drawRect(a-48,b+40,a-3,b+105,p);p.setColor(Color.WHITE);c.drawCircle(a-26,b+50,11,p);}
    void hero(Canvas c,float a,float b){
      boolean air=y<ground-1;float t=System.currentTimeMillis()/1000f;
      // Ground projection stays on the paving while the fly rises above it.
      float height=Math.max(0,ground-y),spread=1+height/260f;
      for(int i=4;i>=0;i--){p.setColor(Color.argb((int)((18+i*3)/(1+height/100)),52,48,39));float radius=(37+i*3)*spread;c.drawOval(a-radius,ground+17-(8+i)*spread,a+radius,ground+17+(8+i)*spread,p);}
      buzz(air&&!dead&&!won&&hasWindowFocus());
      float step=!air&&Math.abs(vx)>0?(float)Math.sin(t*14)*12:0;
      float bob=air?0:Math.abs(step)*.22f;
      c.save();c.translate(a,b-bob);if(idle>3)c.rotate((float)Math.sin(clock*1.5f)*3,0,-30);
      // Two human-like legs and chunky shoes, alternating as he walks.
      p.setStrokeWidth(11);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Color.rgb(42,48,61));
      c.drawLine(-18,-25,-20+step,13-(air?12:0),p);c.drawLine(18,-25,20-step,13-(air?12:0),p);
      c.drawRoundRect(-36+step,8-(air?12:0),-7+step,21-(air?12:0),8,8,p);c.drawRoundRect(8-step,8-(air?12:0),39-step,21-(air?12:0),8,8,p);
      // Small wings fold when walking and beat rapidly during a jump.
      float flap=air?(float)Math.sin(t*90)*22:3;
      p.setColor(Color.argb(210,216,248,255));c.drawOval(-64,-92-flap,-29,-71+flap*.25f,p);c.drawOval(29,-92-flap,64,-71+flap*.25f,p);
      p.setColor(Color.rgb(43,53,68));c.drawOval(-49,-104,49,-16,p);
      p.setColor(Color.rgb(79,91,107));c.drawOval(-33,-86,37,-26,p);
      p.setColor(Color.rgb(103,117,130));c.drawOval(-20,-76,29,-38,p);
      // Arms, round head, spectacles and red scarf.
      p.setStrokeWidth(9);p.setColor(Color.rgb(42,48,61));c.drawLine(-40,-82,-54,-48-step*.5f,p);c.drawLine(40,-82,54,-48+step*.5f,p);
      p.setColor(Color.rgb(54,65,79));c.drawCircle(0,-115,32,p);
      p.setStrokeWidth(4);c.drawLine(-12,-140,-22,-157,p);c.drawLine(12,-140,24,-155,p);c.drawCircle(-23,-158,4,p);c.drawCircle(25,-156,4,p);
      p.setColor(Color.rgb(255,237,178));c.drawCircle(-14,-117,14,p);c.drawCircle(14,-117,14,p);
      p.setColor(Color.rgb(30,42,55));c.drawCircle(-11,-117,5,p);c.drawCircle(17,-117,5,p);
      if(clock%4.3f<.14f||idle>5&&clock%3<.8f){p.setColor(Color.rgb(91,102,106));c.drawRect(-28,-131,28,-113,p);p.setStrokeWidth(2);p.setColor(Color.rgb(36,45,54));c.drawLine(-24,-116,-4,-116,p);c.drawLine(4,-116,24,-116,p);}
      p.setColor(Color.rgb(48,60,74));c.drawRoundRect(17,-58,39,-37,5,5,p);p.setColor(Color.rgb(158,170,178));p.setStrokeWidth(2);c.drawLine(19,-55,37,-55,p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(Color.rgb(234,249,255));c.drawCircle(-14,-117,16,p);c.drawCircle(14,-117,16,p);c.drawLine(-1,-117,1,-117,p);
      c.drawArc(-10,-106,12,-94,10,150,false,p);p.setStyle(Paint.Style.FILL);
      p.setColor(Color.rgb(229,57,58));c.drawRoundRect(-30,-96,30,-82,7,7,p);
      Path scarf=new Path();scarf.moveTo(23,-90);scarf.lineTo(64,-94+(float)Math.sin(t*10)*7);scarf.lineTo(55,-73);scarf.lineTo(24,-80);scarf.close();c.drawPath(scarf,p);c.restore();
    }
    void drawHud(Canvas c,int w,int h){p.setTextAlign(Paint.Align.LEFT);p.setTextSize(30);p.setColor(Color.WHITE);c.drawText("МУХ ЦОКОТУХ",28,45,p);p.setTextSize(25);c.drawText("Монеты: "+coins+" / 10",30,78,p); button(c,45,h-105,100,"◀");button(c,160,h-105,100,"▶");button(c,w-150,h-105,105,"↑"); if(dead||won){p.setColor(Color.argb(215,31,50,60));c.drawRoundRect(w*.22f,h*.22f,w*.78f,h*.70f,28,28,p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(40);p.setColor(Color.WHITE);c.drawText(won?"Лимонад получен!":"Ой! Ловушка!",w/2,h*.40f,p);p.setTextSize(25);c.drawText(won?"Ты спас жаркий день.":"Нажми здесь, чтобы попробовать снова",w/2,h*.52f,p);}}
    void button(Canvas c,float a,float b,float s,String z){a=z.equals("◀")?getWidth()*.09f:z.equals("▶")?getWidth()*.27f:getWidth()*.83f;b=getHeight()-110;s=Math.min(180,getWidth()*.16f);p.setColor(Color.argb(220,255,255,255));c.drawRoundRect(a-s/2,b-85,a+s/2,b+85,26,26,p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(56);p.setColor(Color.rgb(52,96,97));c.drawText(z,a,b+18,p);}
    public boolean onTouchEvent(android.view.MotionEvent e){
      int action=e.getActionMasked();
      if(action==MotionEvent.ACTION_DOWN&&e.getY()<85&&e.getX()>getWidth()*.55f){immortal=!immortal;invalidate();return true;}
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

