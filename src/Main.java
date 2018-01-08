import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main {
	
	static HashMap<Integer,Metadata> metadata=null;

	public static void main(String[] args) throws IOException, JSONException {
		
		loadMetadata();
		int movieId,userId,cont=1;
		
		FileInputStream fr=new FileInputStream("Data/ratings_small.csv");
		BufferedInputStream bis=new BufferedInputStream(fr);
		int read;
		boolean firstLine=true;
		StringBuilder sb=new StringBuilder();
		Recommend recom = new Recommend();
		
		while((read=bis.read())>0){
			if(firstLine){
				if(read==10){
					firstLine=false;
				}
				continue;
			}
				
			if(read==10){
				recom.load(sb.toString());
				sb.delete(0, sb.length());
				continue;
			}
			sb.append((char)read);
		}
		
		fr.close();
		Scanner scan=new Scanner(System.in);
		
		System.out.println("Enter ur userID");
		userId=scan.nextInt();
		
		do{
			System.out.println("Enter the id of the movie that U liked");
			movieId=scan.nextInt();
			try{
				System.out.println("U liked: "+metadata.get(movieId).title);
			}catch(NullPointerException e){
				System.out.println("The movie doesnt exist");
				continue;
			}
			recom.run(movieId, userId);
			
			System.out.println("Enter 1 to continue");
			cont=scan.nextInt();
		}while(cont==1);
		
		scan.close();
		
		

	}
	
	public static void loadMetadata() throws IOException, JSONException{
		FileReader fileReader=new FileReader("Data/movieGenre.csv");
		BufferedReader br=new BufferedReader(fileReader);
		String line,title;
		boolean firstLine=true;
		int position;
		JSONArray ja=null;
		int id;
		metadata=new HashMap<Integer,Metadata>();
		
		while((line=br.readLine())!=null){
			
			if(firstLine){
				firstLine=false;
				continue;
			}
			
			try{
				position=line.indexOf(",");
				id=Integer.parseInt(line.substring(0,position));
				title=line.substring(position+1,line.indexOf(",",position+1));
				
				metadata.put(id, new Metadata(title));
				position=line.indexOf("[", line.indexOf(",",position+1));
				
				ja=new JSONArray(line.substring(position, line.length()));
				
				for(int i=0;i<ja.length();i++){
					JSONObject jo=(JSONObject)ja.get(i);
					
					metadata.get(id).hm.put(jo.getInt("id"), jo.getString("name"));
				}
			}catch(NumberFormatException e){
				continue;
			}
			
		}
		
		br.close();
	}

}
