
/* Each object of particle class is a separate particle.
 * Each particle has a pos array which defines 
 * the task to resource mapping from the graph.
 * */

package org.workflowsim.planning;
import java.util.Random;
public class Particle {
	
	double TEC,TET,pbestTEC,pbestTET;
	double[] vel,pos,pbestpos;
	double[] LET,LST;
	static double[][] m;

	public Particle()
	{
		pbestTEC=999999999;
		TEC=999999999;
		TET=0.0;
		pbestTET=0.0; 
		/*Postion of the particle*/
		pos=new double[PSOPlanningAlgorithm.dim];
		/*Velocity of the particle*/
		vel=new double[PSOPlanningAlgorithm.dim];
		
		/*Pbest or local best position*/
		pbestpos=new double[PSOPlanningAlgorithm.dim];
		
		LET=new double[PSOPlanningAlgorithm.res];
		LST=new double[PSOPlanningAlgorithm.res];
		
		/*m is the mapping of task to resources. m<resourceId,ST,ET>*/
		
		m=new double[PSOPlanningAlgorithm.dim][3];
		
		for(int j=0;j<PSOPlanningAlgorithm.dim;j++)
		{
		/*Generate random initial position of the particle in the search space.*/
		Random rand=new Random();
		pos[j]=(double) (1+(Math.random()*(PSOPlanningAlgorithm.res-1)*1.0));
		}
	}
}
