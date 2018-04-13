
/* In Schedule class we calculate the start time, end time, 
 * cost for each task. Also, the least start time, lease
 * end time is calculate for each resource in the resource pool.
 */
package org.workflowsim.planning;
import java.util.ArrayList;
import java.util.List;
public class Schedule {

	/*Initialising the dimension and number of resources*/ 
	int dim=PSOPlanningAlgorithm.dim;
	int res=PSOPlanningAlgorithm.res;
	
	/*n and s are the dimensions of the mapping array m*/
	int n=PSOPlanningAlgorithm.dim;
	int s=PSOPlanningAlgorithm.res;
	 
	static double[][] exeTime;
	static double[][] transferTime;
	static double[][] graph;
	 
	double[] ET=new double[PSOPlanningAlgorithm.dim];
	double[] ST=new double[PSOPlanningAlgorithm.dim];
	
	double[] LET=new double[PSOPlanningAlgorithm.res];
	double[] LST=new double[PSOPlanningAlgorithm.res];
	
	//m is the final mapping of task to resources. m<resourceId,ST,ET>
	double[][] m=new double[PSOPlanningAlgorithm.dim][PSOPlanningAlgorithm.res];//mapping of tasks to resources
	
	static double[] C;
	int tou=1;
	
	//Total execution cost and Total execution time of the best schedule
	double TEC=0,TET=0;
	
	List<Integer> R=new ArrayList<Integer>();
	List<Integer> M=new ArrayList<Integer>();
	List<Integer> parents=new ArrayList<Integer>();
	List<Integer> children=new ArrayList<Integer>();
	
	
	/*To check whether a given task k has any parent tasks. 
	  If yes, it returns the maximum end time of the parent tasks
	  else returns 0
	*/
	public double hasParent(int k,double[] ET)
	{
		double flag=0,maxet=0;
		int j=k;
		for(int i=0;i<n;i++)
		{if(graph[i][j]!=0)
			{
			flag++;
			parents.add(i);
			if(maxet<ET[i])
				maxet=ET[i];
			}			
		}
		if(flag!=0)
			return maxet;
		else 		
			return 0;
	}
	
	/* To find all the child tasks of a given task k
	adds all the child tasks to the list "children"
	 */	
	public void child(int k)
	{
		int i=k;
		for(int j=0;j<n;j++)
		{
			if(graph[i][j]!=0)
			{
				children.add(j);
			}
		}
	}
	
	/* Returns the maximum end time of all the tasks. */
	public double max(double[] ET)
	{
		double max=ET[0];
		for(int i=0;i<ET.length;i++)
			if(max<ET[i])
				max=ET[i];
		return max;
	}
	
	public void Schedulejob(double[] pos,int particle_no)
	{
		int transfer,t=0,f,r;
		double exe=0;
		double[] PT=new double[dim];
		for(int i=0;i<n;i++)
		{
			transfer=0;f=0;
		//4.1
			
			t=i;
			r=(int)pos[i]-1;
		//4.2
			
			if(hasParent(t,ET)==0)
				{
				ST[i]=LET[r];
				}
			else
				{
				ST[i]=Math.max(hasParent(t,ET),LET[r]);
				}
		//4.3
			
			exe=exeTime[i][(int)pos[i]-1];
		//4.4
			child(i);
			for(int l=0;l<children.size();l++)
			{
				int d=children.get(l);
				int c=(int)pos[d]-1;//children's resource
				if(c!=r)
				{
						transfer+=transferTime[i][d];
				}
			}
			children.clear();
		//4.5
			
			PT[i]=(double) (exe+transfer);
		//4.6
			
			ET[i]=PT[i]+ST[i];
		
		//4.7
			
			m[i][0]=r;
			m[i][1]=ST[i];
			m[i][2]=ET[i];
			PSOPlanningAlgorithm.particle[particle_no].m[i][0]=r;
			PSOPlanningAlgorithm.particle[particle_no].m[i][1]=ST[i];
			PSOPlanningAlgorithm.particle[particle_no].m[i][2]=ET[i];
		//4.9
		
			if(!R.contains((Integer)r))
			{
				f=1;;
				LST[r]=Math.max(ST[i], 0);
				R.add((Integer)r);
			}
		//4.10
		
			if(f==1)
				LET[r]=PT[i]+LST[r];
			else
				LET[r]=ET[i];
		}/*end of for loop*/
	for(int i=0;i<res;i++)
		{
			PSOPlanningAlgorithm.particle[particle_no].LET[i]=LET[i];
			PSOPlanningAlgorithm.particle[particle_no].LST[i]=LST[i];
			TEC+=C[i]*((LET[i]-LST[i])/tou);
		}
	
		PSOPlanningAlgorithm.particle[particle_no].TEC=TEC;
		TET=max(ET);
		PSOPlanningAlgorithm.particle[particle_no].TET=TET;
	}/*end of schedule job*/
	
}/*end of class*/



