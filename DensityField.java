//*****************************************
//DensityField.java
//author: Non-Euclidean Dreamer
// Main Method for compressible Fluid simulation
//**********************************************

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import javax.imageio.ImageIO;

public class DensityField { 

	PotentialField[] density;
	ForceField [] velocity,  
	color;	
	static Random rand=new Random();
	public static int[]scale= {2560,1440}; // Dimensions of the screen= Toroidal world
	public static String name="vibe", //name of files 
			type="png";	
	static ForceField cl=	ForceField.multistripes(new double[][] {{100,100,255},{0,100,255},{0,155,0},{100,0,255},{0,50,5},{155,100,255},{0,55,255},{0,0,200},{100,200,0},{0,0,100},});
			
	public static int steps=1,
			start=3980, bla=0,
					maxit=600;	//breakoff of the simulation				//format type of saved picture
	public static double delt=0.1,delx=1, //discrete steps in time&space 
			visc=2,						//visvosity of medium. Must be <1/delt
			sqrt2=Math.sqrt(2),
			 c=1,						//pressure coefficient 
			b=1,d=1,a=1,ex=0.0001; 
	public static int qterm=0,			//needed for the relaxation scheme when solvin Poisson
			x=400,y=700, r=5;				//For the Gust force
	
	public static double[][]normal;
	public static DecimalFormat df=new DecimalFormat("0000");
	public int time;	
	static boolean force=false, flocking=false;
	public static void main(String[] args)
	{
		VectorField.scale=scale;
		BufferedImage canvas=new BufferedImage(scale[0],scale[1],BufferedImage.TYPE_3BYTE_BGR);
		
	
		DensityField euler=new DensityField();
		euler.time=0;
		euler.density[0]=parsedens();
		//for(int i=0;i<2;i++)System.out.println(v[i].print());System.out.println(name);
		
		//initialize Obstacle(array of pixel coordinates, must be ordered&accompanied by array of normal vectors
			for(int x0=0;x0<20;x0++) {
			bla=x0;
			start=600*x0+3980;
	
		//needed for gust-force
		x=rand.nextInt(scale[0]);
		r=(int) Math.abs(rand.nextGaussian(75,25))+1;
		//	System.out.println("x="+x+", r="+r+", strength="+(1600/Math.sqrt(r)));
	System.out.println(VectorField.v[0].print());
	System.out.println(VectorField.v[1].print());
		while(euler.time<maxit*steps)
		{
			
			
			for(int i=0;i<steps;i++)
			{

				euler.update();//
			euler.time++;
			}
			
			System.out.println(euler.time/steps+start);
			euler.densdraw(canvas);
		}
		VectorField.reset();
		euler.velocity[euler.time%2]=new ForceField(VectorField.v,0);
		euler.time=0;
		}
	}
	
	private static PotentialField parsedens() {
		File file=new File(name+(start-1)+".png");
		try {
			BufferedImage image=ImageIO.read(file);
			double[][]c=new double[scale[0]][scale[1]];
			for(int i=0;i<scale[0];i++) 
				for(int j=0;j<scale[1];j++)
				{	
					Color col=new Color( image.getRGB(i, j));
					c[i][j]=invspectrum(col.getRed(),col.getGreen(),col.getBlue())-255;
					if(c[i][j]>1535)c[i][j]-=1536;
					c[i][j]*=0.0025;
				}
			return  new PotentialField(c);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void update() 
	{
		int now=time%2, next=(time+1)%2;
		if(force)
			//choose a force pattern, vary parameters for random gusts uncomment above if-statement
			velocity[now].
						//stir(time*0.01, 20, 1, 2);
						//diamondstir(time*0.01,20,1);
						//diamond stir
						comb(time*0.01,100,4,20,1);
					//hgust(time/25.0, x,y,r,1/Math.sqrt(r));
						//levelcomb(time*0.02,scale[1]/8,6);
		
		
		for(int i=0;i<scale[0];i++)for(int j=0;j<scale[1];j++)
		{	
			double factor=1;if(flocking)factor=density[now].potential[i][j]-1.5;
			for(int k=0;k<2;k++)
			{
				int[]indp= {i,j},indm= {i,j};
				indp[k]+=1;indp[k]%=scale[k];
				indm[k]+=scale[k]-1;indm[k]%=scale[k];//pressure
				velocity[next].vectorfield[i][j][k]=velocity[now].vectorfield[i][j][k]-delt*c*(density[now].potential[indp[0]][indp[1]]-density[now].potential[indm[0]][indm[1]])/2*factor
						//alignement
						+b*delt*(uv((i-1+scale[0])%scale[0],j,k,0)+uv(i,(j-1+scale[1])%scale[1],k,0)+uv((i+1)%scale[0],j,k,0)+uv(i,(j+1)%scale[1],k,0)-4*uv(i,j,k,0));//(density[now].potential[i][j]+1);		
				//	+((uv(indm[0],indm[1],k,0)+uv(indp[0],indp[1],k,0)-2*uv(i,j,k,1))*b+(uv((i+1)%scale[0],(j+1)%scale[1],k,0)+uv((i+scale[0]-1)%scale[0],(j+scale[1]-1)%scale[1],k,0)-uv((i+1)%scale[0],(j+scale[1]-1)%scale[1],k,0)-uv((i+scale[0]-1)%scale[0],(j+1)%scale[1],k,0))*d)*delt;
			}
		}
		//Advection
		for(int i=0;i<scale[0];i++)for(int j=0;j<scale[1];j++)
		{
			density[next].potential[i][j]=density[now].potential[i][j]-(uv(i+1,j,0,1)- uv(i-1,j,0,1)+uv(i,j+1,1,1)-uv(i,j-1,1,1))*a/2*delt;
			
		}	
	//	density[now].potential=density[next].potential.clone();
		
		for(int i=0;i<scale[0];i++)for(int j=0;j<scale[1];j++)
		{
			 //double vx=velocity[next].vectorfield[i][j][0],vy=velocity[next].vectorfield[i][j][1];
			double[]loc= {((i-velocity[next].vectorfield[i][j][0]*delt/delx)%scale[0]+scale[0])%scale[0],((j-velocity[next].vectorfield[i][j][1]*delt/delx)%scale[1]+scale[1])%scale[1]};
			//for(int k=0;k<2;k++)
			velocity[now].vectorfield[i][j]=//[k]=velocity[next].vectorfield[i][j][k]-delt*vx*(velocity[next].vectorfield[(i+1)%scale[0]][j][k]-velocity[next].vectorfield[(i+scale[0]-1)%scale[0]][j][k])			-delt*vy*(velocity[next].vectorfield[i][(j+1)%scale[1]][k]-velocity[next].vectorfield[i][(j+scale[1]-1)%scale[1]][k]);
			average(velocity[next].vectorfield,loc);
			
			//for(int k=0;k<3;k++) 
			{color[next].vectorfield[i][j]
					//[k]=color[now].	vectorfield[i][j][k]-	delt*vx*(color[now].vectorfield[(i+1)%scale[0]][j][k]-			color[now].vectorfield[(i+scale[0]-1)%scale[0]][j][k])	-delt*vy*(color[now].vectorfield[i][(j+1)%scale[1]][k]-color[now].vectorfield[i][(j+scale[1]-1)%scale[1]][k]);}
				=	average(color[now].vectorfield,loc);
		//	density[next].potential[i][j]=average(density[now].potential,loc);
			}
		}
	
	
		velocity[next].vectorfield=velocity[now].vectorfield.clone();
	}

	private double uv(int i, int j,int k,int l) {
		i=(i+scale[0])%scale[0];
		j=(j+scale[1])%scale[1];
		return density[time%2].potential[i][j]*velocity[(time+l)%2].vectorfield[i][j][k];
	}
 
	public DensityField()
	{
		if(start>0)		cl=parse(name+df.format(start-1)+".png");
		density=new PotentialField[] { new PotentialField(1),new PotentialField(1)}; 
		velocity=new ForceField[] {new ForceField(VectorField.v,0),new ForceField()};
		color=new ForceField[] {cl
			//	ForceField.multistripes(new double[][] {{100,100,255},{0,100,255},{0,155,0},{100,0,255},{0,50,5},{155,100,255},{0,55,255},{0,0,200},{100,200,0},{0,0,100},})
				,new ForceField(3)};
	}
	 
	//drawing the color field
		void densdraw(BufferedImage canvas )
		{
			int now=time%2;
			for(int i=0;i<scale[0];i++)
				for(int j=0;j<scale[1];j++)
				{
					
					int	color=spectrum((int)(density[now].potential[i][j]*400+255),1,1)  ;
					canvas.setRGB(i, j, color);
				}

			File outputfile = new File(name+df.format((time-1)/steps+start)+"."+type);
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
		
		private int spectrum(int n, int d, int l) {
			double full=d*l, term=(1-l)*(1+d)*255/2;
			n=(n%(6*256)+6*256)%(6*256);
			if (n<256)
				return new Color((int) (255*full+term),(int) (n*full+term),(int) term).getRGB();
			n-=256;
			if (n<256)
				return new Color((int) ((255-n)*full+term),(int) (255*full+term),(int) term).getRGB();
			n-=256;
			if (n<256)
				return new Color((int) term,(int) (255*full+term),(int) (n*full+term)).getRGB();
			n-=256;
			if (n<256)
				return new Color((int) term,(int) ((255-n)*full+term),(int) (255*full+term)).getRGB();
			n-=256;
			if (n<256)
				return new Color((int) (n*full+term),(int) term,(int) (255*full+term)).getRGB();
			n-=256;
				return new Color((int) (255*full+term),(int) term,(int) ((255-n)*full+term)).getRGB();
		}	
	public static int invspectrum(int r, int g, int b)
	{
		if(r==255) {
			if(b==0)return g;
			return 6*256-1-b;
		}
		if(r==0) {
			if(g==255)return 2*256+b;
			return 4*256-g;
		}
		if(b==0)return 256*2-1-r;
		return 4*256+r;
	}
		void colordraw(BufferedImage canvas )
		{
			int now=time%2;
			for(int i=0;i<scale[0];i++)
				for(int j=0;j<scale[1];j++)
				{
					
					int	red=(int)Math.max(0, Math.min(255, color[now].vectorfield[i][j][0])) ,
						green=(int) Math.max(0, Math.min(255, color[now].vectorfield[i][j][1])) ,
						blue=(int) Math.max(0, Math.min(255, color[now].vectorfield[i][j][2])) ;
					int	x=new Color(red,green,blue).getRGB();
					canvas.setRGB(i, j, x);
				}

			File outputfile = new File(name+df.format((time-1)/steps+start)+"."+type);
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
}
