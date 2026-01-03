//********************************
//Flake.java
//author: Non-Euclidean Dreamer
// Main Class: Let flakes fly through vector field
//*************************************************

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Flake {
	
	String type;
	 
	int color, size;
 
	double or;   
	  
	double[]loc;   
 
	public static int[]scale= {1080,1080}; // Dimensions of the screen= Toroidal world
	static int maxit=6072, 
			steps=1,
			start=0,
			n=750; 
	static double delt=1;
	static Random rand=new Random(); 
	static boolean gravity=true;
	static String leaf="leaf",snow="snow";
	
	public Flake(String t,int c, int s, double o, double[]l)
	{
		type=t; 
		color=c;
		size=s;
		or=o;
		loc=l; 
	}
	
	public Flake(String t, double[] ds) { 
		type=t;
		size=rand.nextInt(10)+10;
		or=Math.PI/2; 
		loc=ds;
		if(t==leaf) {
			int[]mv=new int[3];
			int k=rand.nextInt(3);
			mv[k]=(int)(128*rand.nextDouble()*rand.nextDouble());
			int l=(k+rand.nextInt(2)+1)%3; 
			mv[l]=(int)((128-mv[k])*rand.nextDouble()); 
			mv[3-l-k]=(int)((128-mv[k]-mv[l])*rand.nextDouble());
			int c=new Color(255-mv[0],mv[1]+mv[k],mv[2]).getRGB();  
			
			color=c;  
		} 
		if(t==snow) {
			int v=25;
			color=new Color(255-rand.nextInt(4*v),255-rand.nextInt(2*v),255-rand.nextInt(v)).getRGB();
		}
	}

	public static void main(String[]args) 
	{ 
		VectorField air=new VectorField(new ForceField(VectorField.v,0),// 
				ForceField.multistripes(new double[][] {{100,100,255},{0,100,255},{0,0,155},{100,0,255},{0,0,55},{100,155,255},{0,55,255},{0,0,200},{100,0,200},{0,0,100},}) );
		ArrayList<Flake>flakes=new ArrayList<Flake>();
		System.out.println(VectorField.v[0].print());System.out.println(VectorField.v[1].print());
		for(int i=0;i<n;i++)flakes.add(new Flake(snow,new double[] {rand.nextInt(scale[0]),scale[1]-1-rand.nextInt(scale[1]/2)}));
		 
		BufferedImage canvas=new BufferedImage(scale[0],scale[1],BufferedImage.TYPE_3BYTE_BGR);
		
		while(air.time<maxit) 
		{
			air.colordraw(canvas,flakes);
			 
			for(int i=0;i<steps;i++)
			{

				air.update();//
				update(flakes,air);
			}
			air.time++;
			System.out.println(air.time+start);
		}
	}

	private static void update(ArrayList<Flake> flakes, VectorField air) {double grav=0;if(gravity)grav=1;  
		for(int i=flakes.size()-1;i>-1;i--)
		{
			Flake flake=flakes.get(i);
			flake.or-=0.5*delt*(air.velocity[air.time%2].value(flake.loc[0]+.5,flake.loc[1],1)-air.velocity[air.time%2].value(flake.loc[0]-.5,flake.loc[1],1)-air.velocity[air.time%2].value(flake.loc[0],flake.loc[1]+.5,0)+air.velocity[air.time%2].value(flake.loc[0],flake.loc[1]-.5,0));
					flake.loc=new double[] {(flake.loc[0]+delt*air.velocity[air.time%2].value(flake.loc[0], flake.loc[1], 0)+scale[0])%scale[0],(flake.loc[1]+delt*air.velocity[air.time%2].value(flake.loc[0], flake.loc[1], 1)+scale[1]+1)%scale[1]};
				//	if(flake.loc[1]>=scale[1]||flake.loc[1]<0) {flakes.remove(i);}
		}
		
	/*	if(flakes.size()<n*rand.nextDouble())
		{
			int x=rand.nextInt(scale[0]),c=0;
			while(air.velocity[air.time%2].value(x, scale[1]-1,1)>0&&c<100) {x=rand.nextInt(scale[0]);c++;} 
			if(c<100) 
			flakes.add(new Flake(leaf,new double[] {x,scale[1]-1}));
		}*/
	}
  
	public void draw(BufferedImage canvas) {
		if(type==leaf)
		{
			int a=size*5/2;
			double[] o= {size*Math.sin(or),size*Math.cos(or)}, 
					l1=VG.add(loc, o), l2=VG.subtract(loc, o);
			for(int i=(int) Math.max(0, loc[0]-2*size);i<Math.min(loc[0]+2*size,scale[0]);i++)
				for(int j=(int) Math.max(0, loc[1]-2*size);j<Math.min(loc[1]+size*2,scale[1]);j++)if(VG.distance(l1,i,j)+VG.distance(l2,i,j)<a)canvas.setRGB(i, j, color);
		}
		if(type==snow)
		{
			for(int i=0;i<6;i++) {
			double[] o= {Math.sin(or+i*Math.PI/3),Math.cos(or+i*Math.PI/3)};
				for(int j=0;j<size;j++)
					canvas.setRGB((int)(loc[0]+j*o[0]+scale[0])%scale[0], (int)(loc[1]+j*o[1]+scale[1])%scale[1], color);
			}
		}
	}
}
