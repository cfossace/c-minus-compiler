import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Interpreter
{
	//enum that describes all possible commands.
	public enum COMMAND
	{
	   ADD,SUB,MUL,DIV,LDW,STW,RD,WRD,ADDI,SUBI,MULI,DIVI
	}

	private int[] m_Memory;
	private int[] m_Registers;
	private BufferedReader m_reader1;
	private BufferedReader m_reader2;

	//-----------------------------------------------------------------------------------
	// Function: main
	// Description: creating an interpreter & starting to read commands using it.
	//-----------------------------------------------------------------------------------
	public static void main(String args[])
	{
		Interpreter interpreter = new Interpreter(args[0],args[1]);
		interpreter.readCommands();
	}

	//-----------------------------------------------------------------------------------
	// Function: Constructor
	// Description: initializing readers for the 2 input files,
	//				initializing the memory & registers arrays.
	//-----------------------------------------------------------------------------------
	public Interpreter(String file1, String file2)
	{
		try
		{
			m_Memory = new int[1024];
			m_Registers = new int[32];
			m_reader1 = new BufferedReader(new FileReader(new File(file1)));
			m_reader2 = new BufferedReader(new FileReader(new File(file2)));
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	//----------------------------------------------------------------------------------------
	// Function: readCommands()
	// Description: reading the first file line by line, and executing the requested command.
	//----------------------------------------------------------------------------------------
	public void readCommands()
	{
		try
		{
			String[] cutBySpace = new String[2];
			String[] values = new String[3];
			String line;
			while((line = m_reader1.readLine()) != null)
			{
				cutBySpace = line.split(" ");
				values = cutBySpace[1].split(",");
				executeCommand(cutBySpace[0], values[0], values[1], values[2]);
			}
		}
		catch (IOException e) {System.out.println(e.getMessage());}
	}

	//-----------------------------------------------------------------------------------
	// Function: executeCommand
	// Description: getting the command that needs to be executed & the 3 variables.
	//				performing the requested command, using the 3 parameters.
	//-----------------------------------------------------------------------------------
	public void executeCommand(String toDo, String val1, String val2, String val3)
	{
		COMMAND command = COMMAND.valueOf(toDo);	//parsing the string into COMMAND type
		int a = Integer.parseInt(val1);
		int b = Integer.parseInt(val2);
		int c = Integer.parseInt(val3);

		switch (command)
		{
			case ADD:
			{
				m_Registers[a] = m_Registers[b] + m_Registers[c];
				break;
			}
			case SUB:
			{
				m_Registers[a] = m_Registers[b] - m_Registers[c];
				break;
			}
			case MUL:
			{
				m_Registers[a] = m_Registers[b] * m_Registers[c];
				break;
			}
			case DIV:
			{
				m_Registers[a] = m_Registers[b] / m_Registers[c];
				break;
			}
			case LDW:
			{
				m_Registers[a] = m_Memory[(m_Registers[b] + c)/4];
				break;
			}
			case STW:
			{
				m_Memory[(m_Registers[b]+c)/4] = m_Registers[a];
				break;
			}
			case RD:
			{
				m_Registers[c] =readNumFromFile2();
				break;
			}
			case WRD:
			{
				System.out.println(m_Registers[c]);
				break;
			}
			case ADDI:
			{
				m_Registers[a] = m_Registers[b] + c;
				break;
			}
			case SUBI:
			{
				m_Registers[a] = m_Registers[b] - c;
				break;
			}
			case MULI:
			{
				m_Registers[a] = m_Registers[b] * c;
				break;
			}
			case DIVI:
			{
				m_Registers[a] = m_Registers[b] / c;
				break;
			}
		}
	}


	//-----------------------------------------------------------------------------------
	// Function: readNumFromFile2
	// Description: this function is reading numbers from the seconds file
	//-----------------------------------------------------------------------------------
	private int readNumFromFile2()
	{
		try
		{
			int val = Integer.parseInt(m_reader2.readLine());
			return val;

		}
		catch (IOException e){System.out.println(e.getMessage());}
		return 0;
	}



}
