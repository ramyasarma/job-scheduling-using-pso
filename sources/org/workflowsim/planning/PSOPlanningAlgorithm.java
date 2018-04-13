/**
 * Copyright 2012-2013 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.planning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.Log;
import org.workflowsim.CondorVM;
import org.workflowsim.Task;
import org.workflowsim.utils.Parameters;

/**
 * The PSO planning algorithm.
 *	
 * @authors Ramya Sarma, Anusha Holla
 * @date April 15, 2015
 */
public class PSOPlanningAlgorithm extends BasePlanningAlgorithm {

    private Map<Task, Map<CondorVM, Double>> computationCosts;
    private Map<Task, Map<Task, Double>> transferCosts; 
    private double averageBandwidth;
    
    //n is the number of particles	
    static int n=25;
	/*dim is the dimension of the particle
    res is the number of resources taken from the resource pool
    deadline is the user define deadline
    iterations is the number of iterations for the PSO algorithm
    */
    static int dim,res,deadline,iterations=500;
	
    /*Initialise the Global best particle*/
	Particle gbest=new Particle();
	
	/*Initialise the array of particles*/
	static Particle[] particle=new Particle[n];

	public PSOPlanningAlgorithm() {
        computationCosts = new HashMap<>();
        transferCosts = new HashMap<>();
        
    }
    
	/*To find the particle with the minimum TEC out of all the particles*/
    public double min()
	{
		double min=999999999;
		for(int i=0;i<n;i++)
			if(min>particle[i].TEC)
				min=particle[i].TEC;
		return min;	
	}  
    
    
    
    /*run() is the main function*/
    @Override
    public void run() {
        Log.printLine("PSO planner running with " + getTaskList().size()
                + " tasks."); 
        
        /*To calculate the running time of our algorithm in ms*/
        long startTime = System.nanoTime();    

        averageBandwidth = calculateAverageBandwidth();
        
        dim=getTaskList().size();
		res=getVmList().size();
		Schedule.exeTime=new double[dim][res];
		Schedule.transferTime=new double[dim][dim];
		Schedule.graph=new double[dim][dim];
		Schedule.C=new double[res];
		
    	//CALCULATING VARIABLES FOR SCHEDULE.JAVA
		
		calculateComputationCosts();
		
        for(Task i:computationCosts.keySet())
        {
        	Map<CondorVM, Double> map = computationCosts.get(i);
        	for(CondorVM j: map.keySet())
        	{
        		Schedule.exeTime[i.getCloudletId()][j.getId()]=map.get(j);
        	}
        }
        
        /*Uncomment this to print the exeTime matrix*/
      /*System.out.println("\n exeTime matrix:");
        
        for(int i=0;i<dim;i++)
        {
        	System.out.println();
        	for(int j=0;j<res;j++)
        	{
        		System.out.print(Schedule.exeTime[i][j]);
        	}
        }*/
        
      
        calculateTransferCosts();
        for(Task i:transferCosts.keySet())
        {
        	Map<Task, Double> map = transferCosts.get(i);
        	for(Task j: map.keySet())
        	{
        		Schedule.transferTime[i.getCloudletId()][j.getCloudletId()]=map.get(j);
        		Schedule.graph[i.getCloudletId()][j.getCloudletId()]=map.get(j);
        	}
        }
        
        /*Uncomment this to print the transferTime matrix*/
       /* System.out.println("\n transferTime matrix:");
        for(int i=0;i<dim;i++)
        {
        	System.out.println();
        	for(int j=0;j<dim;j++)
        	{
        		System.out.print(Schedule.transferTime[i][j]+"       ");
        	}
        }
        */
        @SuppressWarnings("unchecked")
		List<CondorVM> list=getVmList();
        for(CondorVM i:list)
        {
        	Schedule.C[i.getId()]=1;
        }
        Scanner in = new Scanner(System.in);
   	 System.out.println("Enter the deadline for the completion of your workflow");
        deadline = in.nextInt();
        System.out.println("You entered deadline is  "+deadline);
        System.out.println("Please wait until the Optimum schedule is being calculated");
        int flag=0;
        Random rand=new Random();
		for(int g=0;g<iterations;g++)
		{
			for(int i=0;i<n;i++)
			{
			particle[i]=new Particle();
			Schedule s=new Schedule();
			
			/*Calculating fitness*/
			s.Schedulejob(particle[i].pos,i);
		
			//3.1
			/*Check for constraint violations*/
			if (i==0 )
			{
				
			particle[i].pbestTEC=particle[i].TEC;
			particle[i].pbestTET=particle[i].TET;
			for(int l=0;l<dim;l++)
				particle[i].pbestpos[l]=particle[i].pos[l];
			}
			
			else if(particle[i].pbestTEC > particle[i].TEC  && particle[i].TET<deadline && particle[i].pbestTET<deadline)
			
				{
				particle[i].pbestTEC=particle[i].TEC;
				particle[i].pbestTET=particle[i].TET;
				for(int l=0;l<dim;l++)
					particle[i].pbestpos[l]=particle[i].pos[l];
				}
			else if(particle[i].pbestTET>deadline && particle[i].TET>deadline)
				{
				System.out.println("crossed the deadline");
				int pbest_violation=(int) (particle[i].pbestTET-deadline);
				int present_violation=(int) (particle[i].TET-deadline);
				
				if(pbest_violation>present_violation)
					{
					particle[i].pbestTEC=particle[i].TEC;
					particle[i].pbestTET=particle[i].TET;
					for(int l=0;l<dim;l++)
						particle[i].pbestpos[l]=particle[i].pos[l];
					}
				}
			
			else if(particle[i].pbestTET>deadline && particle[i].TET<deadline)
			{
				System.out.println("crossed the deadline");
				particle[i].pbestTEC=particle[i].TEC;
				particle[i].pbestTET=particle[i].TET;
				for(int l=0;l<dim;l++)
					particle[i].pbestpos[l]=particle[i].pos[l];
			
			}
			else
			{}
			
			//3.2
			if(gbest.TEC>particle[i].pbestTEC)
			{ 
				//flag=1;
				gbest=particle[i];
				
			}
			/*else
			{continue;}*/
			
			//3.3 
			for(int k=0;k<dim;k++)			
				{
					/*Update the particle velocity*/
					int r1=((int)rand.nextInt((1-0)+1));
					int r2=((int)rand.nextInt((1-0)+1));
					particle[i].vel[k]=0.1*(particle[i].vel[k]+2*r1*((particle[i].pbestpos[k]-particle[i].pos[k]))+2*r2*((gbest.pos[k]-particle[i].pos[k])));
				}

			for(int k=0;k<dim;k++)
				{			
				/*Update the particle position*/
				particle[i].pos[k]=particle[i].pos[k]+particle[i].vel[k];
				}
			}
		}
		/*if(flag==0)
		{
		System.out.println("Deadline too low. ");
		System.exit(0);
		}*/
		//Selection phase
        allocateTasks(gbest);
        /*Calculate the runtime of the PSO planner*/
        long estimatedTime = (System.nanoTime() - startTime)/1000000;
        System.out.println("\n The approximate running time of PSO algorithm is: " + estimatedTime + " ms.");
    }

    private void allocateTasks(Particle gbest) {
    	int k=0;
    	System.out.println("\n As per our PSO Planner,");
    	System.out.println("The gbest TET is " + gbest.TET + " ms.");
    	System.out.println("The gbest TEC is "+gbest.TEC);
    	for (Object taskObject : getTaskList())
    	{
    		/*
    		 * Allocation of VMs to each task.
    		 * This is done as per the global best particle.
    		 * The gbest pos array has the task to resource mapping.
    		 */
            Task task = (Task) taskObject;
            int index=(int)gbest.pos[k++];
            CondorVM vm=(CondorVM) getVmList().get(index-1);
            int id=vm.getId();
            task.setVmId(id);
           
    	}
	}

	private void calculateComputationCosts() {
        for (Object taskObject : getTaskList()) {
            Task task = (Task) taskObject;

            Map<CondorVM, Double> costsVm = new HashMap<CondorVM, Double>();

            for (Object vmObject : getVmList()) {
                CondorVM vm = (CondorVM) vmObject;
                if (vm.getNumberOfPes() < task.getNumberOfPes()) {
                    costsVm.put(vm, Double.MAX_VALUE);
                } else {
                    costsVm.put(vm,
                            task.getCloudletTotalLength() / vm.getMips());
                }
            }
            computationCosts.put(task, costsVm);
        }
    }
    private double calculateAverageBandwidth() {
        double avg = 0.0;
        for (Object vmObject : getVmList()) {
            CondorVM vm = (CondorVM) vmObject;
            avg += vm.getBw();
        }
        return avg / getVmList().size();
    }

    /**
     * Populates the transferCosts map with the time in seconds to transfer all
     * files from each parent to each child
     */
    private void calculateTransferCosts() {
        // Initializing the matrix
        for (Object taskObject1 : getTaskList()) {
            Task task1 = (Task) taskObject1;
            Map<Task, Double> taskTransferCosts = new HashMap<Task, Double>();

            for (Object taskObject2 : getTaskList()) {
                Task task2 = (Task) taskObject2;
                taskTransferCosts.put(task2, 0.0);
            }

            transferCosts.put(task1, taskTransferCosts);
        }

        // Calculating the actual values
        for (Object parentObject : getTaskList()) {
            Task parent = (Task) parentObject;
            for (Task child : parent.getChildList()) {
            	
                transferCosts.get(parent).put(child,
                        calculateTransferCost(parent, child));
            }
        }
    }

    /**
     * Accounts the time in seconds necessary to transfer all files described
     * between parent and child
     *
     * @param parent
     * @param child
     * @return Transfer cost in seconds
     */
    private double calculateTransferCost(Task parent, Task child) {
        List<File> parentFiles = (List<File>) parent.getFileList();
        List<File> childFiles = (List<File>) child.getFileList();

        double acc = 0.0;

        for (File parentFile : parentFiles) {
            if (parentFile.getType() != Parameters.FileType.OUTPUT.value) {
                continue;
            }

            for (File childFile : childFiles) {
                if (childFile.getType() == Parameters.FileType.INPUT.value
                        && childFile.getName().equals(parentFile.getName())) {
                    acc += childFile.getSize();
                    break;
                }
            }
        }
        //file Size is in Bytes, acc in MB
        acc = acc / Consts.MILLION;
        // acc in MB, averageBandwidth in Mb/s
        return acc * 8 / averageBandwidth;
    }
}

   
    
    