package Template.src.main.java.com.epam;

import Template.src.main.java.com.epam.api.GpsNavigator;
import Template.src.main.java.com.epam.api.Path;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class app demonstrates how your implementation of {@link com.epam.api.GpsNavigator} is intended to be used.
 */
public class ExampleApp {

	
    public static void main(String[] args) throws Exception {
        final GpsNavigator navigator = new StubGpsNavigator();
        navigator.readData("D:\\Gps\\road_map.ext");
    
        //Не обработана ситуация с несколькими однинаково кратчайшими путями.
    	
       if (navigator.CountMap() > 0) {
	        final Path path = navigator.findPath("A", "Metro");
	        System.out.println(path);
       }else
    	   System.out.println("нет данных для расчета");
        
    }

    private static class StubGpsNavigator implements GpsNavigator {

    	//Коллекция для создания матрицы: i,j, sum;
    	//List dfmList = new ArrayList<dataForMatrix>();
    	ArrayList dfmList = new ArrayList();
    	
    	//Коллекция адрес и id в матрице 
    	Map<String, Integer> CollectionAddress = new HashMap<String, Integer>();
    	
    	public int CountMap()
    	{
    		return CollectionAddress.size();
    	}
    	
        @Override
        public void readData(String filePath){
            // Read data from file.
        	
        	//Будем хранить соответствие имени и порядкового номера
        	String[] subStr;
        	String delimeter = " "; 
              
        	try{
    		   FileInputStream fstream = new FileInputStream(filePath);
    		   BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
    		   String strLine;
    		   while ((strLine = br.readLine()) != null){
    		     // System.out.println(strLine);
    		      subStr = strLine.split(delimeter, 4);
    		
    		       int i = 0;
    		      if (!CollectionAddress.containsKey(subStr[0])) {
    	        		CollectionAddress.put(subStr[0], CollectionAddress.size());
    	        		i = CollectionAddress.size()-1;}
    		      else {
    		    	  i = CollectionAddress.get(subStr[0]);
    	          }
    		      
    		      int j = 0;
    		      if (!CollectionAddress.containsKey(subStr[1])) {
	  	        		CollectionAddress.put(subStr[1], CollectionAddress.size());
	  	        		j = CollectionAddress.size()-1;}
    		      else {
    		    	  j = CollectionAddress.get(subStr[1]);
    	          }
    		  	
    		      dfmList.add(new dataForMatrix(i, j, Integer.valueOf(subStr[2])*Integer.valueOf(subStr[3])));
    		      
    		   }    		   
        	
        	}catch (Exception e) {
        		dfmList.clear();
        		CollectionAddress.clear();
        	   System.out.println("Ошибка чтения файла");
        	}
        	
        }

        public static int[][] createMatrix(ArrayList dfmList, Map<String, Integer> CollectionAddress) {
            
        	int matrix[][]  = new int[CollectionAddress.size()][CollectionAddress.size()];
            
            for(int i = 0; i < matrix.length; ++i){
                for(int j = 0; j < matrix[i].length; ++j) {
                	matrix[i][j] = Integer.MAX_VALUE;
                }
            }
                   
		   for (int iter = 0; iter < dfmList.size(); iter++) {
			   dataForMatrix dfm = (dataForMatrix) dfmList.get(iter);
			   matrix[dfm.getI()][dfm.getJ()] = dfm.getSum();
		   }
      
            
            return matrix;
        }   
        
        @Override
        public Path findPath(String pointA, String pointB) {
        	//points - название точек прописью.
        	// в матрице
        	int start, end;
        	try {
	        	start = CollectionAddress.get(pointA);
        	}catch (Exception e)
        	{
             	return new Path(Arrays.asList("Указанная точка начала маршрута не найдена"), 0);
        	}
        	try {
	        	end = CollectionAddress.get(pointB);
        	}catch (Exception e)
        	{
             	return new Path(Arrays.asList("Указанная точка окончания маршрута не найдена"), 0);
        	}
        	
        	int[][] matrix = createMatrix(dfmList, CollectionAddress);
            boolean[] visit = new boolean[matrix.length];
            Vector<Integer> vector = new Vector<Integer>();                
    		//создаем матрицу 
            
            
            int[] mdate = new int[matrix.length];
            //Забиваем макс. значениями
            for(int i = 0; i < mdate.length; ++i)
            	mdate[i] = Integer.MAX_VALUE;
      
            //в вектор будем записывать путь
            vector.setSize(matrix.length);
                    
            mdate[start] = 0;
            int sel;
            for(int i = 0; i < matrix.length; ++i){
                sel = -1;
                for(int j = 0; j < matrix[i].length; ++j){
                    if(!visit[j] && (sel == -1 || mdate[j] < mdate[sel]))
                        sel = j;
                }
                            
                if(mdate[sel] == Integer.MAX_VALUE)
                    break;
                
                //отмечаем посещение
                visit[sel] = true;
                        
                for(int j = 0; j < matrix[sel].length; ++j){
                    if(matrix[sel][j] == Integer.MAX_VALUE)
                        continue;
                    
                    if((mdate[sel] + matrix[sel][j]) < mdate[j]){
                    	mdate[j] = mdate[sel] + matrix[sel][j];
                        vector.set(j, sel);
                    }
                }
            }
            visit = null;
            
            if(end >= mdate.length || mdate[end] == Integer.MAX_VALUE)
            {
            	System.out.println("Ошибка");
            	return new Path(Arrays.asList("Не найден путь"), 0);
            }
            
            Vector<Integer> ps = new Vector<Integer>();
            for(int i = end; i != start; i = vector.get(i)) // выделяем путь
                ps.add(i);
            ps.add(start);
     
            List<String> array = new ArrayList<String>();
            
            for(int i = ps.size() - 1; i >= 0; --i)
            	array.add(getKeyCollection(ps.get(i)));//.toString());
            
               
            return new Path(array, mdate[end]);
        }
        
        public String getKeyCollection(int value)
        {
        	
        	for (Map.Entry<String, Integer> entry: CollectionAddress.entrySet())
        	{
        		if (value == entry.getValue()){
        			return entry.getKey();
        		}
        	}

        	return "";
        }
    }
}

final class dataForMatrix{
	private int m[] = new int[3];//i,j, sum;
	
	public dataForMatrix(int i, int j, int sum)
	{
		m[0] = i;
		m[1] = j;
		m[2] = sum;
	}
	
		
	public int getI()
	{
		return m[0];
	}	
	public int getJ()
	{
		return m[1];
	}	
	public int getSum()
	{
		return m[2];
	}	
}

