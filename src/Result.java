public class Result implements Comparable<Result>{
	
	int diffGenre,movieID;
	double coefficient;
	
	Result(int diffGenre,double coefficient, int movieID){
		this.diffGenre=diffGenre;
		this.coefficient=coefficient;
		this.movieID=movieID;
	}

	@Override
	public int compareTo(Result o) {
		
		if(o.diffGenre==this.diffGenre){
			if(o.coefficient==this.coefficient){
				return 0;
			}else if(o.coefficient>this.coefficient){
				return 1;
			}else
				return -1;
		}else if(o.diffGenre<this.diffGenre){
			return 1;
		}else{
			return -1;
		}
		
	}
	
	
}
