package Location_LDA;

import input_output_interface.data_storage;

import java.io.OutputStreamWriter;
import java.util.HashMap;

import Location_LDA.UILA_LDA.pair;

public class ILCA_LDA {

	public class pair{
		public int a, b;
		
		public pair(int a, int b){
			this.a = a;
			this.b = b;
		}
		public pair(){}
	}
	
	
	HashMap<Integer, UserProfile> user_item;
	   
    public int V; //Vocabulary Size (Tag size)
    public int K; //The number of topics
    public int U; //The number of Users
    public int M; //The number of item locations
    public int C;//The number of item category
	
    // Dirichlet parameter (user--topic associations)
    public double alpha[];
    public double alpha_sum;
    
    // Dirichlet parameter (location--topic associations)
    public double alpha_2[];
    public double alpha_2_sum;
    
    // Dirichlet parameter (topic--term associations)
    public double beta[];
    public double beta_sum;
    
    // Dirichlet parameter (topic--loc associations)
    public double beta_2[];
    public double beta_2_sum;
    
    // Beta parameter (affect s = 0 or s = 1)
    public double gamma[];
    public double gamma_sum;

    
    // [u][0]: number of times s = 0 has been sampled in user u's D_u
    // [u][1]: ... 
    public int[][] userSCount;
    
    // [u][z]: number of times that topic z has been sampled from 
    //		   the multinomial distribution specific to user u;
    public int[][] userTopicCount;
    // [u] : 
    public int[] userTopicCountSum;
    
    // [l][z] : number of times that topic z has been
    //          sampled from the multinomial distribution specific to location l;
    public int[][] locTopicCount;
    // [l]
    public int[] locTopicCountSum;
    
    // [z][v] : number of times item v has been generated by topic z
    public int[][] topicItemCount;
    public int[] topicItemCountSum;
    
    // [z][l] : number of times location l has been generated by topic z
    public int[][] topicCategoryCount;
    public int[] topicCategoryCountSum;
    

    // s[][] and z[][] is recorded in  HashMap *user_item*
  /*  
    // s[u][i] = 1: item[u][i] is generated by user interests
    // s[u][i] = 0: item[u][i] is generated by location interests
    public int[][] s;   
    // [u][i]: item[u][i] is assigned to topic z[u][i]
    public int[][] z;
  */
    
    // [u][z]: user - topic
    public double[][] userTopicDistribution;
    // [l][z]: loc - topic
    public double[][] locTopicDistribution;
    // [z][v]: topic - word
    public double[][] topicItemDistribution;
    // [z][l]: topic - loc
    public double[][] topicCategoryDistribution;
    // s = 1 : user
    public double[] lambda_u;
    
    // [i][j]: user - topic
    public double[][] userTopicSum;
    // [i][j]: time - topic
    public double[][] locTopicSum;
    // [i][j]: topic - word
    public double[][] topicItemSum;
    // [i][j]: topic - loc
    public double[][] topicCategorySum;
    //
    public double[] lambda_uSum;

    // test lambda_u
    private double[] lambda_uSum_test;
    private int lambdaNum_test;
    
    
    // [i][j]: user - topic
    public int userTopicNum;
    // [i][j]: time - topic
    public int locTopicNum;
    // [i][j]: topic - word
    public int topicItemNum;
    // [i][j]: topic - loc
    public int topicCategoryNum;
    // 
    public int lambdaNum;
    
    
    public int ITERATIONS;
    public int SAMPLE_LAG;
    public int BURN_IN;
    public String outputPath; 
    
    // test
    public int iter = 0;


    public ILCA_LDA(){}
    
    public ILCA_LDA(int V, int K, int U, int M,int C, double[] alpha, double[] alpha_2,
    				double[] beta, double[] beta_2, double[] gamma, int iterations,
    				   int sampleLag, int burnIn, String outputPath,
    				   HashMap<Integer, UserProfile> user_item){
    	this.V = V;
    	this.K = K;
    	this.U = U;
    	this.M = M;
    	this.C=C;
    	
    	this.ITERATIONS=iterations;
    	this.SAMPLE_LAG=sampleLag;
    	this.BURN_IN=burnIn;
    	this.outputPath = outputPath;
    	
    	this.alpha = alpha;
    	this.alpha_2 = alpha_2;
    	this.beta = beta;
    	this.beta_2 = beta_2;
    	this.gamma = gamma;
    

        userSCount = new int[U][2];
        userTopicCount = new int[U][K];
        locTopicCount = new int[M][K];
        
        userTopicDistribution = new double[U][K];
        locTopicDistribution = new double[M][K];
        topicItemDistribution = new double[K][V];
        topicCategoryDistribution = new double[K][C];
        lambda_u = new double[U];
        
        userTopicSum = new double[U][K];
        locTopicSum = new double[M][K];
        topicItemSum = new double[K][V];
        topicCategorySum = new double[K][C];
        lambda_uSum = new double[U];
        
        userTopicCountSum = new int[U];
        locTopicCountSum = new int[M];

        topicItemCount = new int[K][V];
        topicItemCountSum = new int[K];
        topicCategoryCount = new int[K][C];
        topicCategoryCountSum = new int[K];
        

        
        alpha_sum = 0;
        alpha_2_sum = 0;
        gamma_sum = 0;
        beta_sum = 0;
        beta_2_sum = 0;
        
        this.user_item = user_item;
        
        
        // just for test
        lambda_uSum_test = new double[U];
        lambdaNum_test = 0;
    }
    

    public void initialState(){
    	
    	// s
    	for(int i=0; i<U; i++){
    		int length = user_item.get(i).length;
    		
    		for(int j=0; j<length; j++){
    			int ran = (int) (Math.random() * 2);
    			
    			user_item.get(i).setS(j, ran);

    			userSCount[i][ran]++;
    		}
    	}
    	
    	
    	// z
    	for(int i=0; i<U; i++){
    		int length = user_item.get(i).length;
    		
    		for(int j=0; j<length; j++){
    			int ran = (int) (Math.random() * K);
    			user_item.get(i).setZ(j, ran);
    			topicItemCount[ran][user_item.get(i).getItem(j)]++;
    			topicItemCountSum[ran]++;
    			
       			topicCategoryCount[ran][user_item.get(i).getItemCategory(j)]++;
    			topicCategoryCountSum[ran]++;
    			
    			if(user_item.get(i).getS(j) == 0){
    				locTopicCount[user_item.get(i).getItemLoc(j)][ran]++;
    				locTopicCountSum[user_item.get(i).getItemLoc(j)]++;
    			}
    			else{
    				userTopicCount[i][ran]++;
    				userTopicCountSum[i]++;
    			}
    		
    		}
    	}
    	
    	
    	for(int i=0; i<K; i++){
    		alpha_sum += alpha[i];
    		alpha_2_sum += alpha_2[i];
    	}
    	for(int i=0; i<V; i++){
    		beta_sum += beta[i]; 
    	}
    	for(int i=0; i<C; i++){
    		beta_2_sum += beta_2[i];
    	}
    	
    	gamma_sum = gamma[0] + gamma[1];

    	
    	testLambdaU();
    }

    
    public void updateParameter(){    
        // userTopic
        for(int i=0; i<U; i++){
        	for(int j=0; j<K; j++){
        		userTopicSum[i][j] +=
        			(userTopicCount[i][j] + alpha[j])
        			/ (userTopicCountSum[i] + alpha_sum);
        	}
        }
        userTopicNum++;
       
        // locTopic
        for(int i=0; i<M; i++){
        	for(int j=0; j<K; j++){
        		locTopicSum[i][j] +=
        			(locTopicCount[i][j] + alpha_2[j])
        			/ (locTopicCountSum[i] + alpha_2_sum);
        	}
        }
        locTopicNum++;
       
        // topicWord
        for(int i=0; i<K; i++){
        	for(int j=0; j<V; j++){
        		topicItemSum[i][j] +=
        			(topicItemCount[i][j] + beta[j])
        			/ (topicItemCountSum[i] + beta_sum);
        	}
        }
        topicItemNum++;
        
        // topicCategory
        for(int i=0; i<K; i++){
        	for(int j=0; j<C; j++){
        		topicCategorySum[i][j] +=
        			(topicCategoryCount[i][j] + beta_2[j])
        			/ (topicCategoryCountSum[i] + beta_2_sum);
        	}
        }
        topicCategoryNum++;
        
        
        // lambda
        for(int i=0; i<U; i++){
        	lambda_uSum[i] += (userSCount[i][1] + gamma[1]) 
        					/ (userSCount[i][0] + userSCount[i][1] + gamma_sum);
        }
        lambdaNum++;
       
    }
        

    public void testLambdaU(){
    	int count_over_09 = 0;
    	int count_over_05 = 0;
    	double curLambda_u;
    	
    	lambdaNum_test++;
    	for(int i=0; i<U; i++){
        	lambda_uSum_test[i] += (userSCount[i][1] + gamma[1]) 
								/ (userSCount[i][0] + userSCount[i][1] + gamma_sum);
        	
        	curLambda_u = lambda_uSum_test[i] / (double)lambdaNum_test;
        	if(curLambda_u >= 0.9)
        		count_over_09++;
        	if(curLambda_u >= 0.5)
        		count_over_05++;
    	}
    	
    	System.out.println(">0.9 " + (double)count_over_09/U 
    						+ "; >0.5 " +(double)count_over_05/U);
    }

    public void gibbsSampling(){
    	for (int it = 1; it <= this.ITERATIONS; it++){
    		iter = it;
    		
    		// (1) S
    		// user i
        	for(int i=0; i<U; i++){
        		int length = user_item.get(i).length;
        		// user i's item j
        		for(int j=0; j<length; j++){
        			pair p=sample(i,j);
        			int new_s = p.a; 
        			user_item.get(i).setS(j, new_s);
        			int new_z = p.b; 
        			user_item.get(i).setZ(j, new_z);
        		/*	
        			int new_s = sample_s(i, j); 
        			user_item.get(i).setS(j, new_s);
        			int new_z = sample_z(i, j); 
        			user_item.get(i).setZ(j, new_z);
        		*/
        		}
        	}
        
    		// (2) Z
    		// user i
        	/*for(int i=0; i<U; i++){
        		int length = user_item.get(i).length;
        		// user i's item j
        		for(int j=0; j<length; j++){
        			int new_z = sample_z(i, j); 
        			user_item.get(i).setZ(j, new_z);
        		}
        	}*/

    		
            // get statistics after burn-in    		
            if ((it >= BURN_IN) && (it % SAMPLE_LAG == 0)) { 
                this.updateParameter();
                
                if(it % 100 == 0){
                	calDistribution();
                	output_model(it);
                }
            }
            
            System.out.println("iteration "+it+" done");
           // testLambdaU();
    	}
    }
 
    
    public pair sample(int i,int j)
    {
    	pair p = new pair();
    	UserProfile user = user_item.get(i);
    	//int loc = user.location;
    	int loc = user.getItemLoc(j);
    	//int itemLoc = user.getItemLoc(j);
    	int itemCat = user.getItemCategory(j);
    	int topic = user.getZ(j);
    	int s = user.getS(j);
    	int item = user.getItem(j);
    	int length = user.length;
    	
    	userSCount[i][user.getS(j)]--;
		if(s == 0){
			locTopicCount[loc][topic]--;
			locTopicCountSum[loc]--;
		}
		else{
			userTopicCount[i][topic]--;
			userTopicCountSum[i]--;
		}
		topicItemCount[topic][item]--;
		topicItemCountSum[topic]--;
		topicCategoryCount[topic][itemCat]--;
		topicCategoryCountSum[topic]--;
		
    	
		double probability[][]=new double[2][K];
		
		for(int st=0;st<2;st++)
			for(int z=0;z<K;z++)
			{
				double first_term, second_term,third_term,fourth_term;	
				if(st == 0){
					//System.out.println(i+" "+topic);
					
					first_term = 
						(locTopicCount[loc][z] + alpha_2[z]) / (locTopicCountSum[loc] + alpha_2_sum);

					second_term = 
						(userSCount[i][0] + gamma[0]) / (length - 1 + gamma_sum); 
					third_term=
							(topicItemCount[z][item] + beta[item]) / (topicItemCountSum[z] + beta_sum); 
					
					fourth_term =
							(topicCategoryCount[z][itemCat] + beta_2[itemCat]) 
							/ (topicCategoryCountSum[z] + beta_2_sum); 
				}
				else{
					first_term = 
						(userTopicCount[i][z] + alpha[z]) / (userTopicCountSum[i] + alpha_sum);
					
					second_term = 
						(userSCount[i][1] + gamma[1]) / (length - 1 + gamma_sum);
					
					third_term=
							(topicItemCount[z][item] + beta[item]) / (topicItemCountSum[z] + beta_sum);
					
					fourth_term =
							(topicCategoryCount[z][itemCat] + beta_2[itemCat]) 
							/ (topicCategoryCountSum[z] + beta_2_sum);  
				}
				
				probability[st][z] = first_term * second_term * third_term * fourth_term;
			}
		
		double temp=0;
		for(int st=0;st<2;st++)
			for(int z=0;z<K;z++)
		{
				probability[st][z]=probability[st][z]+temp;
				temp=probability[st][z];
		}
		double t = Math.random() *probability[1][K-1];
		
		for(int st=0;st<2;st++)
			for(int z=0;z<K;z++)
			{
				if(t<probability[st][z])
				{
					p.a=st;
					p.b=z;
					break;
					
				}
			}
		
		if(p.a<0||p.b<0)
		{
			System.out.println("sampling error");
		}
		userSCount[i][p.a]++;
		if(p.a == 0){
			locTopicCount[loc][p.b]++;
			locTopicCountSum[loc]++;
		}
		else{
			userTopicCount[i][p.b]++;
			userTopicCountSum[i]++;
		}
		
	
		topicItemCount[p.b][item]++;
		topicItemCountSum[p.b]++;
		topicCategoryCount[p.b][itemCat]++;
		topicCategoryCountSum[p.b]++;
    	
    	return p;
    }
    
    
   
    // i: user, j: item in the user
    public int sample_s(int i, int j){
    	UserProfile user = user_item.get(i);
    	int loc = user.getItemLoc(j);
    	int topic = user.getZ(j);
    	int length = user.length;
    	
    	userSCount[i][user.getS(j)]--;
		if(user.getS(j) == 0){
			locTopicCount[loc][topic]--;
			locTopicCountSum[loc]--;
		}
		else{
			userTopicCount[i][topic]--;
			userTopicCountSum[i]--;
		}
		
		
		// probability
		double[] p = new double[2];
		for(int u=0; u<2; u++){
			double first_term, second_term;
			if(u == 0){
				first_term = 
					(locTopicCount[loc][topic] + alpha_2[topic]) / (locTopicCountSum[loc] + alpha_2_sum);

				second_term = 
					(userSCount[i][0] + gamma[0]) / (length - 1 + gamma_sum); 
			}
			else{
				first_term = 
					(userTopicCount[i][topic] + alpha[topic]) / (userTopicCountSum[i] + alpha_sum);
				
				second_term = 
					(userSCount[i][1] + gamma[1]) / (length - 1 + gamma_sum); 
			}
			
			p[u] = first_term * second_term;
		}
		
    	// sampling
		p[1] += p[0];
		double t = Math.random() * p[1];
		int u;
		for(u=0; u<2; u++){
			if(t < p[u])
				break;
		}
		
		// test
		if(u > 1){
			System.err.println(p[1] + " " + t);
		}
		
		// update
    	userSCount[i][u]++;
		if(u == 0){
			locTopicCount[loc][topic]++;
			locTopicCountSum[loc]++;
		}
		else{
			userTopicCount[i][topic]++;
			userTopicCountSum[i]++;
		}

		return u;
    }
    
    
    // i: user, j: item in the user
    public int sample_z(int i, int j){
    	UserProfile user = user_item.get(i);
    	int loc = user.getItemLoc(j);
    	int topic = user.getZ(j);
    	//int length = user.length;
    	int s = user.getS(j);
    	int item = user.getItem(j);
    	int itemCateorgy = user.getItemCategory(j);
    	
    	
		if(s == 0){
			locTopicCount[loc][topic]--;
			locTopicCountSum[loc]--;
		}	
		else{
			userTopicCount[i][topic]--;
			userTopicCountSum[i]--;
		}
    	
		topicItemCount[topic][item]--;
		topicItemCountSum[topic]--;
		
		topicCategoryCount[topic][itemCateorgy]--;
		topicCategoryCountSum[topic]--;


		// probability
		double[] p = new double[K];
		for(int u=0; u<K; u++){
			double first_term, second_term, third_term;
			
			if(s == 0){
				first_term = 
					(locTopicCount[loc][u] + alpha_2[u]) / (locTopicCountSum[loc] + alpha_2_sum);

				second_term = 
					(topicItemCount[u][item] + beta[item]) / (topicItemCountSum[u] + beta_sum);
				
				third_term =
					(topicCategoryCount[u][itemCateorgy] + beta_2[itemCateorgy]) / (topicCategoryCountSum[u] + beta_2_sum); 
			}
			else{
				first_term = 
					(userTopicCount[i][u] + alpha[u]) / (userTopicCountSum[i] + alpha_sum);
				
				second_term = 
					(topicItemCount[u][item] + beta[item]) / (topicItemCountSum[u] + beta_sum); 
				
				third_term =
					(topicCategoryCount[u][itemCateorgy] + beta_2[itemCateorgy]) / (topicCategoryCountSum[u] + beta_2_sum); 
			}
		
			
			p[u] = first_term * second_term * third_term;
		}
		
    	// sampling
		for(int ii=1; ii<K; ii++)
			p[ii] += p[ii-1]; 
		double t = Math.random() * p[K-1];
		int u;
		for(u=0; u<K; u++){
			if(t < p[u])
				break;
		}
		
		// test
		if(u >= K){
			System.err.println(p[K-1] + " " + t);
		}
		
		// update
		
		if(s == 0){
			locTopicCount[loc][u]++;
			locTopicCountSum[loc]++;
		}
		else{
			userTopicCount[i][u]++;
			userTopicCountSum[i]++;
		}
    	
		topicItemCount[u][item]++;
		topicItemCountSum[u]++;
		
		topicCategoryCount[u][itemCateorgy]++;
		topicCategoryCountSum[u]++;

		return u;
    }
    
    
    public void calDistribution(){	     
     // userTopic
     for(int i=0; i<U; i++){
     	for(int j=0; j<K; j++){
     		userTopicDistribution[i][j] =
     			userTopicSum[i][j] / (double)userTopicNum;
     	}
     }
    
     // locTopic
     for(int i=0; i<M; i++){
     	for(int j=0; j<K; j++){
     		locTopicDistribution[i][j] =
     			locTopicSum[i][j] / (double)locTopicNum;

     	}
     }
         
     // topicWord
     for(int i=0; i<K; i++){
     	for(int j=0; j<V; j++){
     		topicItemDistribution[i][j] =
     			topicItemSum[i][j] / (double)topicItemNum;
     	}
     }
     
     // topicCategory
     for(int i=0; i<K; i++){
     	for(int j=0; j<C; j++){
     		topicCategoryDistribution[i][j] =
     			topicCategorySum[i][j] / (double)topicCategoryNum;
     	}
     }
     
     
     // lambda_u
     for(int i=0; i<U; i++){
    	 lambda_u[i] =
    		 lambda_uSum[i] / (double)lambdaNum;
      }
    
   }
    
    public void train(){
    	initialState();
    	
    	gibbsSampling();
    	
    	calDistribution();
    	
    	output_model();
    }
    
    
    public void output_model(){
    	System.out.println("output model ...");
    	
    	// parameter
    	String parameter_file = outputPath + "matrix/parameter.txt";
    	OutputStreamWriter oswpf = data_storage.file_handle(parameter_file);
    	output_parameter(oswpf);
    	
    	// matrix
    	output_matrix(outputPath+"matrix/");
    	
    	System.out.println("output model ... done");
    }
    
    public void output_model(int i){
    	System.out.println("output model " + i);
    	
    	// parameter
    	String parameter_file = outputPath + i + "/parameter.txt";
    	OutputStreamWriter oswpf = data_storage.file_handle(parameter_file);
    	output_parameter(oswpf);
    	
    	// matrix
    	output_matrix(outputPath + i +"/");
    	
    	System.out.println("output model ... done" + i);
    }
 /*  
    public void output_model_path(String base_path){
    	System.out.println("output model ...");
    	
    	// parameter
    	String parameter_file = base_path + "parameter.txt";
    	OutputStreamWriter oswpf = data_storage.file_handle(parameter_file);
    	output_parameter(oswpf);
    	
    	// matrix
    	output_matrix(base_path);
    	
    	System.out.println("output model ... done");
    }
  */
    
    public void output_matrix(String base_path){
    	
    try{
        // userTopic
    	String userTopic_file = base_path + "userTopic.txt";
    	OutputStreamWriter oswpf = data_storage.file_handle(userTopic_file);
    	oswpf.write(U+" "+K+"\n");
        for(int i=0; i<U; i++){
        	for(int j=0; j<K; j++){
        		oswpf.write(userTopicDistribution[i][j]+" ");
        	}
        	oswpf.write("\n");
        }
		oswpf.flush();
		oswpf.close();
       
        // locTopic
    	String locTopic_file = base_path + "locTopic.txt";
    	oswpf = data_storage.file_handle(locTopic_file);
    	oswpf.write(M+" "+K+"\n");
        for(int i=0; i<M; i++){
        	for(int j=0; j<K; j++){
        		oswpf.write(locTopicDistribution[i][j]+" ");
        	}
        	oswpf.write("\n");
        }
		oswpf.flush();
		oswpf.close();
       
        // topicWord
    	String topicWord_file = base_path + "topicWord.txt";
    	oswpf = data_storage.file_handle(topicWord_file);
    	oswpf.write(K+" "+V+"\n");
        for(int i=0; i<K; i++){
        	for(int j=0; j<V; j++){
        		oswpf.write(topicItemDistribution[i][j]+" ");
        	}
        	oswpf.write("\n");
        }
		oswpf.flush();
		oswpf.close();
		
        // topicLoc
    	String topicLoc_file = base_path + "topicCategory.txt";
    	oswpf = data_storage.file_handle(topicLoc_file);
    	oswpf.write(K+" "+C+"\n");
        for(int i=0; i<K; i++){
        	for(int j=0; j<C; j++){
        		oswpf.write(topicCategoryDistribution[i][j]+" ");
        	}
        	oswpf.write("\n");
        }
		oswpf.flush();
		oswpf.close();
		
        
        // lambda_u
    	String lambda_file = base_path + "lambda_u.txt";
    	oswpf = data_storage.file_handle(lambda_file);
    	oswpf.write(U+"\n");
        for(int i=0; i<U; i++){
        	oswpf.write(lambda_u[i]+" ");
        }
    	oswpf.write("\n");
		oswpf.flush();
		oswpf.close();
       
    }
	catch(Exception e){
		e.printStackTrace();
	}
    }
    
 
    public void output_parameter(OutputStreamWriter oswpf){
    	try{
    		oswpf.write("U: " + U + "\n");
    		oswpf.write("V: " + V + "\n");
    		oswpf.write("K: " + K + "\n");
    		oswpf.write("M: " + M + "\n");
    		oswpf.write("C: " + C + "\n");
    		// alpha
			oswpf.write("alpha:\n");
    		for(int i=0; i<K; i++){
    			oswpf.write(alpha[i]+" ");
    		}
    		oswpf.write("\n");
    		
    		// alpha_2
			oswpf.write("alpha_2:\n");
    		for(int i=0; i<K; i++){
    			oswpf.write(alpha_2[i]+" ");
    		}
    		oswpf.write("\n");
    	
    		// beta
			oswpf.write("beta:\n");
    		for(int i=0; i<V; i++){
    			oswpf.write(beta[i]+" ");
    		}
    		oswpf.write("\n");
    		// beta_2
			oswpf.write("beta_2:\n");
    		for(int i=0; i<C; i++){
    			oswpf.write(beta_2[i]+" ");
    		}
    		oswpf.write("\n");
    		
    		// gamma
			oswpf.write("gamma:\n");
    		for(int i=0; i<2; i++){
    			oswpf.write(gamma[i]+" ");
    		}
    		oswpf.write("\n");
    	 
    		
    		oswpf.write("ITERATIONS: " + ITERATIONS + "\n");
    		oswpf.write("SAMPLE_LAG: " + SAMPLE_LAG + "\n");
    		oswpf.write("BURN_IN: " + BURN_IN + "\n");
    		oswpf.write("outputPath: " + outputPath + "\n");
    		
			oswpf.flush();
			oswpf.close();
    	}
		catch(Exception e){
			e.printStackTrace();
		}
    }
}
