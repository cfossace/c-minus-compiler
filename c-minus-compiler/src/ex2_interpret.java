import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

//------------------------------------------------------------------------------------------
// Class: Interpreter
// Description: this class is reading commands in the c-- language and executing them.
//------------------------------------------------------------------------------------------
public class ex2_interpret
{
	//enum that describes all possible commands.
	public enum COMMAND
	{
		ADD, SUB, MUL, DIV, LDW, STW, RD, WRD, ADDI, SUBI, MULI, DIVI
	}
	private int[] m_Memory;
	private int[] m_Registers;
	private BufferedReader m_reader1;
	private BufferedReader m_reader2;

	//--------------------------------------------------------------------------------------
	// Function: main
	// Description: creating an interpreter that will read & execute commands.
	//--------------------------------------------------------------------------------------
	public static void main(String args[])
	{
		ex2_interpret interpreter = new ex2_interpret();
		interpreter.readCommands();
	}

	//-----------------------------------------------------------------------------------
	// Function: Constructor
	// Description: initializing readers for the 2 input files,
	//				initializing the memory & registers arrays.
	//-----------------------------------------------------------------------------------
	public ex2_interpret()
	{
		try
		{
			m_Memory = new int[1024];
			m_Registers = new int[32];
			m_reader1 = new BufferedReader(new FileReader(new File("prog.txt")));
			m_reader2 = new BufferedReader(new FileReader(new File("input.txt")));
		}
		catch (IOException e)
		{
			System.out.println("error " + e.getMessage());
			System.exit(1);
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
			String[] cutByTab = new String[2];
			String[] values = new String[3];
			String line;
			while ((line = m_reader1.readLine()) != null)
			{
				cutByTab = line.split("\t");
				values = cutByTab[1].split(",");
				executeCommand(cutByTab[0], values[0], values[1], values[2]);
			}
		}
		catch (IOException e)
		{
			System.out.println("error " + e.getMessage());
			System.exit(1);
		}
	}

	//-----------------------------------------------------------------------------------
	// Function: executeCommand
	// Description: getting the command that needs to be executed & the 3 variables.
	//				performing the requested command, using the 3 parameters.
	//-----------------------------------------------------------------------------------
	public void executeCommand(String toDo, String val1, String val2, String val3)
	{
		try
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
						m_Registers[a] = m_Memory[(m_Registers[b] + c) / 4];
						break;
					}
				case STW:
					{
						m_Memory[(m_Registers[b] + c) / 4] = m_Registers[a];
						break;
					}
				case RD:
					{
						m_Registers[c] = readNumFromFile2();
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
		//if there was an error in one of the commands - print "error" and the line with the mistake,then exit.
		catch (Exception e)
		{
			System.out.println("error " + toDo + "\t" + val1 + "," + val2 + "," + val3);
			System.exit(1);
		}
	}


	//--------------------------------------------------------------------------------------------
	// Function: readNumFromFile2
	// Description: this function is reading numbers from the seconds file (one number in a row)
	//--------------------------------------------------------------------------------------------
	private int readNumFromFile2()
	{
		try
		{
			int val = Integer.parseInt(m_reader2.readLine());
			return val;

		}
		catch (IOException e)
		{
			System.out.println("error " + e.getMessage());
			System.exit(1);
		}
		return 0;
	}



}
