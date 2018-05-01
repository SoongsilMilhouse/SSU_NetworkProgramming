import java.io.*;

public class FileReaderDo
{
	public static void main(String[] args)
	{
		try
		{
			FileReader fr = new FileReader("./bin/data/software.txt");
			BufferedReader br = new BufferedReader(fr);
			
			String s;
			while((s = br.readLine()) != null) {
				System.out.println(s);
			}
			
			fr.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}