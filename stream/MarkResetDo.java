import java.io.*;

public class MarkResetDo {
	public static void main(String[] args) throws Exception {
      
		InputStream is = null;
		BufferedInputStream bis = null;
      
		try
		{       
			is = new FileInputStream("./bin/data/number.txt");
			bis = new BufferedInputStream(is);
			
			//read and print characters one by one
			System.out.println("Char : " + (char)bis.read());
			System.out.println("Char : " + (char)bis.read());
			
			//mark is set on the input stream
			System.out.println("mark() invoked");
			bis.mark(0);
			
			System.out.println("Char : " + (char)bis.read());
			System.out.println("Char : " + (char)bis.read());
			
			//reset is called
			System.out.println("reset() invoked");
			bis.reset();
			
			System.out.println("Char : " + (char)bis.read());
			System.out.println("Char : " + (char)bis.read());
		}
		catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(is != null) is.close();
			if(bis != null) bis.close();
		}
	}
}