import java.io.*;

public class FileWriterDo
{
	public static void main(String[] args)
	{
		String what1 = new String("Hello~?\r\n");
		String what2 = new String("I am learning Java Network programming...");

		try
		{
			FileWriter fw = new FileWriter("./bin/data/software.txt");
			fw.write(what1);
			fw.write(what2);
			fw.close();
		}
		catch(IOException e) 
		{
			System.out.println(e);
		}
	}
}