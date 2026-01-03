//****************************************
//VectorField.java
//by Non-Euclidean Dreamer
// Main Class foe letting the Vector Field deform the Color Field
//********************************************

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
 
import javax.imageio.ImageIO;

public class VectorField 
{
	static Random rand=new Random();

	public static boolean viscosity=false, //viscosity only implemented for vist<=1/delt
						  gradientfree=false,
						  advect=false,
						  function=true, 
						  force=false; 
	public static int[]scale= {1080,1080}; // Dimensions of the screen= Toroidal world
	public static String name="vibe", //name of files
			type="png";					//format type of saved picture
	public static double delt=1,delx=1, //discrete steps in time&space 
			visc=2,						//visvosity of medium. Must be <1/delt
			sqrt2=Math.sqrt(2);  
	public static int qterm=0,			//needed for the relaxation scheme when solvin Poisson
			x=400,y=700, r=5;				//For the Gust force
	public static int steps=1, 
			start=0,
					maxit=1440;	//breakoff of the simulation
	public static double[][]normal;
	public static DecimalFormat df=new DecimalFormat("0000");
	public static PotentialField[] q= {new PotentialField(), new PotentialField()};
	static int i=4,what=81;
	public static Function[] trig=trig(6), v=new Function[]{new Product(trig1(what),trig1(what)),new Product(trig1(what),trig1(what))};//new Function[2];// {new Sum(new Function[] {new Monom(1,new int[] {2,0}),new Monom(-1,new int[] {0,2}),new Monom(-1,new int[] {0,0})}),new Monom(2,new int[] {1,1})};
			//{new Sum(new Function[] {new Monom(1,new int[] {1,0}),Function.bogdanov(0,1.2,0)}),Function.bogdanov(0,1.2,0)};
			//{new Product(sin(i%10-5,0,1,0),cos(2*(i/10%10-5),1,0,0)),new Product(sin(2*(i%10-5),1,0,0),cos(i/10%10-5,0,1,0))};
			//{new Sum(new Function[] {new Func("cos",new Sum(new Function[] { new Monom(2,new int[] {0,1,0}),new Monom(.1,new int[] {0,0,1})})),new Monom(-.1,new int[] {1})}),new Func("abs",new Monom(1,new int[] {0,0,1}))};
	
	//FlowField consisting of velocity field, color field and current time
	public ForceField[] velocity, 
				color;	
	public int time;	
	static	double[] red= {256,0,0},yellow= {256,256,0},g= {0,256,0},c= {0,256,256},b= {0,0,256},m= {256,0,256},w= {256,256,256},s= {0,0,0},p= {128,0,256},o={256,128,0},
				l= {128,256,0}, mint=  {0,256,128},a= {0,85,170},wine= {170,0,85},gold= {255,215,0}, silver= {192,192,192},forest= {46,111,64};
		
	//Constructor, initialize color field, there could be an initial velocity field too 
	public VectorField() 
	{
		ForceField.scale=scale;
	time=0;
		velocity=new ForceField[] {new ForceField(v,time),new ForceField(2)};
		System.out.println(v[0].print()+" and "+v[1].print());
		//Choose an initial condition for the color field
		ForceField initcol=//ForceField.stripes(r,y,o,g,c,mint,b,m,p});
							//ForceField.rgbw();
							//ForceField.ryb(); 
					//		ForceField.multistripes(new double[][] {g,w,mint,wine,forest,silver,l,gold,c,red});
					parse(name+df.format(start)+".png");
		color=new ForceField[] {initcol,new ForceField()};////colors(wine,offset )),ForceField.stripes(colors(wine, offset)) };//
	}
	
	public VectorField(ForceField v, ForceField c) {
		velocity=new ForceField[] { v,new ForceField(2)};
		color=new ForceField[] {c,new ForceField()};
		
	}
	
	
	


	private ForceField parse(String string) {
		File file=new File(string);
		try {
			BufferedImage image=ImageIO.read(file);
			double[][][]c=new double[scale[0]][scale[1]][3];
			for(int i=0;i<scale[0];i++)
				for(int j=0;j<scale[1];j++)
				{	Color cl=new Color(image.getRGB(i,j));
					c[i][j]=new double[] {cl.getRed(),cl.getGreen(),cl.getBlue()};
				}
			return new ForceField(c);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}





	private ForceField circle(int i) {
		// TODO Auto-generated method stub
		return null;
	}


	public static void main(String[] args)
	{
		
		BufferedImage canvas=new BufferedImage(scale[0],scale[1],BufferedImage.TYPE_3BYTE_BGR);
		
	/*	Function[]v1=new Function[rand.nextInt(10)+1],v2=new Function[rand.nextInt(10)+1];
		for(int i=0;i<v1.length;i++) v1[i]=trig[rand.nextInt((int)Math.pow(6,3)*2)];
		for(int i=0;i<v2.length;i++) v2[i]=trig[rand.nextInt((int)Math.pow(6,3)*2)];
		
		v[0]=new Sum(v1);
		v[1]=new Sum(v2);*/
		VectorField euler=new VectorField();
		for(int i=0;i<2;i++)System.out.println(v[i].print());System.out.println(name);
		
		//initialize Obstacle(array of pixel coordinates, must be ordered&accompanied by array of normal vectors
		
		//needed for gust-force
		x=rand.nextInt(scale[0]);
		r=(int) Math.abs(rand.nextGaussian(75,25))+1;
	
		while(euler.time<maxit*steps)
		{
			
			
			for(int i=0;i<steps;i++)
			{

				euler.update();
			euler.time++;
			}
			euler.colordraw(canvas);
			System.out.println(euler.time/steps+start);
		}
	}

	
	// Making a time step: adding the vectorial velocity, applying pressure, advecting, and viscosity if chosen

	
	public void update()
	{
		
			if(time%200==101)//for a gust force
			{
				x=rand.nextInt(scale[0]);
				y=rand.nextInt(scale[1]);
				r=(int) Math.abs(rand.nextGaussian(75/2,25/2))+1;
				System.out.println("x="+x+", r="+r+", strength="+(5000/Math.sqrt(r)));
			}
		int next=(time+1)%2, now=time%2;

		if(force)
		//choose a force pattern, vary parameters for random gusts uncomment above if-statement
		velocity[now].
					//stir(time*0.01, 20, 1, 2);
					//diamondstir(time*0.01,20,1);
					//diamond stir
					//comb(time*0.01,100,4,20,1);
				hgust(time/25.0, x,y,r,200/Math.sqrt(r));
					//levelcomb(time*0.02,scale[1]/8,6);

		int k=0;
		
		
		{
			//Pressureterm; Making the velocity gradientfree
			if(gradientfree) {
				qterm=relax(velocity[now]);
			velocity[now].subtract(q[qterm].gradient());}
			
			if(viscosity)
			{
				for(int i=0;i<scale[0];i++)
					for(int j=0;j<scale[1];j++)
					{  
						double[]del2=velocity[now].del2(i,j);
						for(int k1=0;k1<2;k1++)
							velocity[next].vectorfield[i][j][k1]=velocity[now].vectorfield[i][j][k1]-visc*del2[k1];
					}
				velocity[now].vectorfield=velocity[next].vectorfield.clone();
			}
		}
		//advection of u-field
		for(int i=0;i<scale[0];i++)
			for(int j=0;j<scale[1];j++)
				{  
					int ip=(i+1)%scale[0],im=(i+scale[0]-1)%scale[0],jp=(j+1)%scale[1],jm=(j+scale[1]-1)%scale[1],id=200,jd=200;//torus	
					double[]loc= {((i-velocity[now].vectorfield[i][j][0]*delt/delx)%scale[0]+scale[0])%scale[0],((j-velocity[now].vectorfield[i][j][1]*delt/delx)%scale[1]+scale[1])%scale[1]};
				if(advect)	velocity[next].vectorfield[i][j]=average(velocity[now].vectorfield,loc);else velocity[next].vectorfield[i][j]=velocity[now].vectorfield[i][j];
					color[next].vectorfield[i][j]=average(color[now].vectorfield,loc);
				}
	if(function)	velocity[next]=new ForceField(v,time);
	}


	//****************
	// Drawing Methods
	//****************
	
	//drawing the color field
	void colordraw(BufferedImage canvas )
	{
		int now=time%2;
		for(int i=0;i<scale[0];i++)
			for(int j=0;j<scale[1];j++)
			{
				
				double	red=color[now].vectorfield[i][j][0],
					green=color[now].vectorfield[i][j][1],
					blue=color[now].vectorfield[i][j][2];
				int	x=sRGB(red,green,blue);
					//new Color(red,green,blue).getRGB();
				canvas.setRGB(i, j, x);
			}

		File outputfile = new File(name+df.format(time/steps+start)+"."+type);
		try 
		{  
			ImageIO.write(canvas, type, outputfile);
		} 
		catch (IOException e) 		
		{
			System.out.println("IOException");
			e.printStackTrace();
		}
		
	}
	
	//drawing the color field and the obstacle in black
	private void colordraw(BufferedImage canvas , int[][]obstacle)
	{
		int now=time%2,k=0;
		for(int i=0;i<scale[0];i++)
			for(int j=0;j<scale[1];j++)
			{
				if(obstacle[k][0]==i&&obstacle[k][1]==j) {canvas.setRGB(i, j, Color.black.getRGB());k++;}
				else {
				double	red=color[now].vectorfield[i][j][0],
					green=color[now].vectorfield[i][j][1],
					blue=color[now].vectorfield[i][j][2];
				int	x=sRGB(red,green,blue);//new Color(red,green,blue).getRGB();
				canvas.setRGB(i, j, x);}
			}
		

		File outputfile = new File(name+df.format(time)+"."+type);
		try 
		{  
			ImageIO.write(canvas, type, outputfile);
		} 
		catch (IOException e) 		
		{
			System.out.println("IOException");
			e.printStackTrace();
		}
		
	}
	
	//color by velocity field(I only used it before adding the color field...)
	private void draw(BufferedImage canvas) {
		int now=time%2;
		for(int i=0;i<scale[0];i++)
			for(int j=0;j<scale[1];j++)
			{
				
				int	red=0,
					blue=(int)Math.max(0, Math.min(255, (velocity[now].vectorfield[i][j][0]*64+128))),
					green=(int)Math.max(0, Math.min(255, velocity[now].vectorfield[i][j][1]*64+128)),
					x=new Color(red,green,blue).getRGB();
				canvas.setRGB(i, j, x);
			}
		
		File outputfile = new File(name+df.format(time)+"."+type);
		try 
		{  
			ImageIO.write(canvas, type, outputfile);
		} 
		catch (IOException e) 		
		{
			System.out.println("IOException");
			e.printStackTrace();
		}
		
	}

	//converting the color to srgb, uncomment stuff to actually do that...
	private static int sRGB(double r, double g, double b) 
	{
		double[] y=new double[3];
		 y[0]=r;//(3.2406*r-1.5372*g-.4986*b);
		y[1]=g;//(-.9689*r+1.8758*g+.0415*b);
		y[2]=b;//(.0557*r-.204*g+1.057*b);
		
		int[]x=new int[3];
		/*for(int i=0;i<3;i++)
		{if(y[i]>.8015)x[i]=(int)(Math.pow(y[i],1/2.4)*26.795-14.08);
		else x[i]=(int) (12.92*y[i]);}
	*/
		for(int i=0;i<3;i++)
		x[i]=(int) Math.min(255, Math.max(0, y[i]));
		
		return new Color(x[0],x[1],x[2]).getRGB();
	}
	
	//color list with fixed color fallback and rainboish in between
	public static double[][]colors(double[]fallback,double d)
	{
		int base=2;
		double[][]out=new double[12][3];
		for(int i=1;i<12;i+=2)
			out[i]=fallback.clone();
	
		out[0]=new double[] {(1-2*d)*fallback[0]+d*256,256-fallback[1],fallback[2]};
		out[2]=new double[] {256-fallback[0],(2*d-1)*fallback[1]+(1-d)*256,fallback[2]};
		out[8]=new double[] {fallback[0],(1-2*d)*fallback[1]+d*256,256-fallback[2]};
		out[10]=new double[] {fallback[0],256-fallback[1],(2*d-1)*fallback[2]+(1-d)*256};
		out[4]=new double[] {256-fallback[0],fallback[1],(1-2*d)*fallback[2]+d*256};
		out[6]=new double[] {(2*d-1)*fallback[0]+(1-d)*256,fallback[1],256-fallback[2]};
		return out;
	}
	
	//***************************************************
	// Relaxation scheme for solving the Poisson Equation
	//***************************************************
	public static int relax (ForceField f)
	{
		q[0].set(0);
		PotentialField div=f.divergence(delx);
		int l=0, now=qterm,next=1-now;
		double max=10,bound=0.001;
		while(l<200&&max>bound)//600
		{
			max=q[next].poisson(q[now],div,delx);
			l++;
			now=1-now;
			next=1-next;
		}
		return now;
	}
	public static int relax (ForceField f, int[][]obstacle)
	{
		q[0].set(0);
		PotentialField div=f.divergence(delx);
		int l=0, now=0,next=1-now;
		double max=10,bound=0.01;
		//System.out.println("i="+f.vectorfield[0][0].length);
		while(max>bound&&l<150)//500l<400&&
		{
			max=q[next].poisson(q[now],div,delx,obstacle, normal	,f);
			l++;
			now=1-now;
			next=1-next;
		}
	//	System.out.println(q[now].potential[109][50]);
		return now;
	}
	
	//********************************************************************************
	// Taking the weighted average of the field value of the for cells surrounding loc
	//********************************************************************************
	public static double[] average(double[][][] vectorfield, double[] loc) 
	{
		int x=(int)loc[0], y=(int)loc[1], xp=(x+1)%vectorfield.length, yp=(y+1)%vectorfield[0].length;
		double dx=loc[0]%1, dy=loc[1]%1;
		double[]out=new double[vectorfield[x][y].length];
		for(int i=0;i<vectorfield[x][y].length;i++)
		{
			out[i]=dx*(dy*vectorfield[xp][yp][i]+(1-dy)*vectorfield[xp][y][i])+(1-dx)*(dy*vectorfield[x][yp][i]+(1-dy)*vectorfield[x][y][i]);
		//   if(Double.isNaN(out[i])) {System.out.println("NaN bei "+vectorfield[xp][yp][i]+","+vectorfield[xp][y][i]);}
		}
		
		return out;
		
	}
	public static double average(double[][] field, double[] loc) 
	{
		int x=(int)loc[0], y=(int)loc[1], xp=(x+1)%field.length, yp=(y+1)%field[0].length;
		double dx=loc[0]%1, dy=loc[1]%1;
		double out=dx*(dy*field[xp][yp]+(1-dy)*field[xp][y])+(1-dx)*(dy*field[x][yp]+(1-dy)*field[x][y]);
		return out;
	}
	
	
	//****************************************************
	// printing field values to the terminal for debugging
	//****************************************************
	static void print(double[][][]tensor)
	{
		System.out.print("{");
		for(int i=0;i<tensor.length;i++)
		{
			System.out.print("{");
			for(int j=0;j<tensor[i].length;j++)
			{
				System.out.print(" {");
				for(int k=0;k<tensor[i][j].length;k++)
					System.out.print(tensor[i][j][k]+", ");
				System.out.print("},");
			}
			System.out.println("},");
		}
		System.out.println("}");
	}
	static void print(double[][] m) 
	{
		System.out.print("{");
		for(int i=0;i<m.length;i++)
		{
			print(m[i]);
			System.out.print(",");
		}
		System.out.println("}");
	}
	static void print(double[] m) 
	{
		System.out.print("{");
		for(int i=0;i<m.length;i++)
		{
			System.out.print(m[i]+",");	
		}
		System.out.println("},");
	}
	
		static Function[] trig(int n)
	{ int three=2;
		Function[]out=new Function[n*n*three*2];
		for(int i=0;i<n;i++)
			for(int j=0;j<three;j++)
					for(int l=0;l<n;l++)
						for(int amp=0;amp<2;amp++){
						double p=i+1, phase=scale[1]*1.0*l/n;
						int[]v=new int[3];v[j]=1;
		out[i+n*j+n*three*l+n*n*2*amp]= new Product(new Func("sin",new Sum (new Monom[] { new Monom(p,v),new Monom(phase,new int[] {0,0,0})})),new Monom((amp+1.0),new int[] {0,0,0}));
					}
		
		return out;
	}
	static Function trig1(int n)
	{
		int[]v=new int[3];v[rand.nextInt(3)]=1;
		return new Product(new Func("sin",new Sum (new Monom[] { new Monom(rand.nextDouble()*n,v),new Monom(scale[1]*1.0*rand.nextInt(n)/n,new int[] {0,0,0})})),new Monom((rand.nextInt(3)+1.0),new int[] {0,0,0}));
		
	}
	private static Function cos(int i, int j, int k, int l) {
		// TODO Auto-generated method stub
		return new Func("cos",new Monom(i,new int[] {j,k,l}));
	}
	private static Function sin(int i, int j, int k, int l) {
		// TODO Auto-generated method stub
		return new Func("sin",new Monom(i,new int[] {j,k,l}));
	}

	public void colordraw(BufferedImage canvas, ArrayList<Flake> flakes) {
		//drawing the color field
		
			int now=time%2;
			for(int i=0;i<scale[0];i++)
				for(int j=0;j<scale[1];j++)
				{
					
					double	red=color[now].vectorfield[i][j][0],
						green=color[now].vectorfield[i][j][1],
						blue=color[now].vectorfield[i][j][2];
					int	x=sRGB(red,green,blue);
						//new Color(red,green,blue).getRGB();
					canvas.setRGB(i, j, x);
				}
			for(Flake flake:flakes)
				flake.draw(canvas);

			File outputfile = new File(name+df.format(time+start)+"."+type);
			try 
			{  
				ImageIO.write(canvas, type, outputfile);
			} 
			catch (IOException e) 		
			{
				System.out.println("IOException");
				e.printStackTrace();
			}
			
		
	}

	public static void reset() {
		v=new Function[]{new Product(trig[rand.nextInt(trig.length)],trig[rand.nextInt(trig.length)]),new Product(trig[rand.nextInt(trig.length)],trig[rand.nextInt(trig.length)])};//new Function[2];// {new Sum(new Function[] {new Monom(1,new int[] {2,0}),new Monom(-1,new int[] {0,2}),new Monom(-1,new int[] {0,0})}),new Monom(2,new int[] {1,1})};
		
	}
}