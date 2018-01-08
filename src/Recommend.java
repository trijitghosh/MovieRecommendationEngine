import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Recommend {

	protected HashMap<Integer,HashMap<Integer,Float>> item = null;
	protected int minThresholdUsers=5;
	
	Recommend(){
		item = new HashMap<Integer,HashMap<Integer,Float>>();
	}
	
	public void load(String line){
		
		String[] arg=line.split(",");
		
		int movieID=Integer.parseInt(arg[1]);
		int userID=Integer.parseInt(arg[0]);
		float rating=Float.parseFloat(arg[2]);
		
		//see if the movieId exists or not
		if(Main.metadata.containsKey(movieID)){
			if(item.containsKey(movieID)){ 								// if it exists, insert the user rating
				HashMap<Integer,Float> userRating=item.get(movieID);
				userRating.put(userID, rating);
			}else{
				HashMap<Integer,Float> userRating=new HashMap<Integer,Float>();
				userRating.put(userID, rating);
				item.put(movieID, userRating);
			}
		}
		
	}
	
	public void print(){
		
		for(Integer movieID : item.keySet()){
			HashMap<Integer,Float> userRating=item.get(movieID);
			System.out.print(movieID+": ");
			for(Integer userID: userRating.keySet()){
				System.out.print(userRating.get(userID)+",");
			}
			System.out.println();
		}
	}
	
	public Set<Integer> findItems(int userID,int movieID){
		
		Set<Integer> movies=new HashSet<Integer>();
		movies.addAll(item.keySet());
		movies.remove(movieID);
		Iterator<Integer> i=movies.iterator();

		while(i.hasNext()){

			int temp=i.next();
			HashMap<Integer,Float> userRating=item.get(temp);
			
			if(userRating.containsKey(userID)){
				i.remove();
			}
		}
		movies.add(movieID);
		
		return movies;
		
	}
	
	public Set<Integer> findUsers(int movieID,int userID){
		
		HashMap<Integer,Float> userRating=item.get(movieID);
		Set<Integer> userIDs=userRating.keySet();
		userIDs.remove(userID);
		System.out.println();
		
		return userIDs;
	}
	
	public Set<Integer> getPreference(Set<Integer> users,Set<Integer> movies){
		
		Set<Integer> tempSet=new HashSet<Integer>();
		Set<Integer> set=new HashSet<Integer>();
		
		Iterator<Integer> i=movies.iterator();
		HashMap<Integer,Float> userRatings=null;
		
		
		while(i.hasNext()){//for(Integer i : movies){
			userRatings=item.get(i.next());
			
			if(tempSet.size()==0){
				tempSet.addAll(userRatings.keySet());
				tempSet.retainAll(users);
			}else{
				tempSet.retainAll(userRatings.keySet());
			}
			if(tempSet.size()<3){
				tempSet.clear();
				tempSet.addAll(set);
				i.remove();
			}else{
				if(set.size()==0){
					set.addAll(tempSet);
				}else{
					set.retainAll(tempSet);
				}
				
			}
			
		}
		
		return set;
	}
	
	public double[][] pearsonCoeff(double[][] array){
			
			double[][] ret =new double[array[0].length][array[0].length];
			double sumX,sumY,sumXY,sumXSQ,sumYSQ,size=array.length;
			int col;
			
			for(int i=0;i<array[0].length;i++){
				col=i;
				while(col<array[0].length){
					sumX=0;sumY=0;sumXY=0;sumXSQ=0;sumYSQ=0;
					for(int j=0;j<array.length;j++){
						sumX+=array[j][i];
						sumY+=array[j][col];
						sumXY+=array[j][i]*array[j][col];
						sumXSQ+=Math.pow(array[j][i],2);
						sumYSQ+=Math.pow(array[j][col],2);
					}
					ret[i][col]=ret[col][i]=((size*sumXY)-(sumX*sumY))/Math.pow((size*sumXSQ - Math.pow(sumX,2))*(size*sumYSQ - Math.pow(sumY,2)),0.5);
					col++;
				}
			}
			
			return ret;
			
	} 
	
	public void run(int movieID,int userID){
		
		Set<Integer> users=null;
		Set<Integer> movies=null;
		
		try{
			users=findUsers(movieID, userID);
			movies=findItems(userID,movieID);
		}catch(NullPointerException e){
			if(Main.metadata.containsKey(movieID)){
				System.out.println("the movie has not been rated by anyone else as such cannot recommend...Plz try any other movie");
			}else{
				System.out.println("Either movie doesnt exist");
			}
			
			return;
		}
		int start=0,point=0;
		
		
		Set<Integer> finalUsers=getPreference(users, movies);
		Integer[] moviesArray=new Integer[movies.size()];
		
		for(Integer i : movies){
			moviesArray[start]=i;
			if(i==movieID)
				point=start;
			start++;
		}
		
		if(finalUsers.size()==0){
			System.out.println("No recommendation");
			return;
		}
		
		double[][] array=new double[finalUsers.size()][movies.size()];
		
		start=0;
		
		for(Integer i : finalUsers){
			//System.out.println("users "+i);
			for(int j=0;j<moviesArray.length;j++){
				//System.out.println("movies "+moviesArray[j]);
				array[start][j]=item.get(moviesArray[j]).get(i);
			}
			start++;
		}
		
		double[][] coeff=pearsonCoeff(array);
		
		HashSet<Integer> comp=new HashSet<Integer>();
		ArrayList<Result> result=new ArrayList<Result>();
		//int count=0;
		
		comp.addAll(Main.metadata.get(movieID).hm.keySet());
		
		
		for(int i=0;i<coeff[0].length;i++){
			
			comp.addAll(Main.metadata.get(movieID).hm.keySet());
			//count=comp.size();
			
			if(coeff[point][i]>0 && i!=point){
				try{
					comp.removeAll(Main.metadata.get(moviesArray[i]).hm.keySet());
					result.add(new Result(comp.size(),coeff[point][i],moviesArray[i]));
				}catch(NullPointerException e){
					continue;
				}
			}
			
			comp.clear();
		}
		
		Collections.sort(result);
		
		int num=result.size()>6?6:result.size();
		
		if(result.size()==0){
			System.out.println("No movies to recommend");
		}else{
			System.out.println("movies to watch are : ");
			for(int i=0;i<num;i++){
				
				try{
					System.out.println(Main.metadata.get(result.get(i).movieID).title);
				}catch(NullPointerException e){
					continue;
				}
			}
		}
		
	}
	
}
