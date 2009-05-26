import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

//--------------------------------------------------------------------------------------------------
// CLASS: ex3_symbol
// Description: This class is responsible for creating an appropriate symbol table for a given text
//--------------------------------------------------------------------------------------------------
public class ex3_symbol
{
	private Lexer m_lexer;
	private LinkedList<Record> m_list;
	private Integer m_memoryAddress;
	private Integer m_tableAddress;
	private BufferedWriter m_writer;

	//-----------------------------------------------------------------------------------------
	// Function: ex3_symbol
	// Description: Constructor
	//-----------------------------------------------------------------------------------------
	public ex3_symbol()
	{
		try
		{
			m_writer = new BufferedWriter(new FileWriter("output.txt"));
			m_lexer = new Lexer();
			m_list = new LinkedList<Record>();
			m_memoryAddress = 1000;
			m_tableAddress = 0;
		}
		catch (IOException e)
		{
			System.out.println("error " + e.getMessage());
		}
	}

	//-----------------------------------------------------------------------------------------
	// Function: readWithLexer
	// Description: This function reads all of the input file using the Lexer
	//-----------------------------------------------------------------------------------------
	public void readWithLexer()
	{
		m_lexer.init_tokens();
		m_lexer.get();
	}

	//-----------------------------------------------------------------------------------------
	// Function: createTable()
	// Description: For every line that was read by the Lexer - we create a new record
	//				And entering it to the list (using "handleLine" function).
	//-----------------------------------------------------------------------------------------
	public void createTable()
	{
		ArrayList<ArrayList<Lexer.Token>> lines = m_lexer.getLines();
		for (int i=0; i<lines.size(); i++)
			handleLine(lines.get(i));
	}

	//-----------------------------------------------------------------------------------------
	// Function: handleLine
	// Description: Getting an array list of tokens that represents a line from the input file.
	//				For every line- creating the correct Record & inserting it to the list.
	//-----------------------------------------------------------------------------------------
	public void handleLine(ArrayList<Lexer.Token> line)
	{
		String firstToken = line.get(0).getToken();
		if (firstToken.equals("int"))
		{
			//-----------------------------------------------------
			//if we have an array statement (example: int a[10];)
			//-----------------------------------------------------
			if (this.searchToken("[", line))
			{
				Record record = new Record();
				record.setName(line.get(1).getToken());
				record.setType("var");
				m_tableAddress += 20;
				record.setDefinition(m_tableAddress.toString());
				record.setMemAddress(m_memoryAddress.toString());

				m_memoryAddress += (4*Integer.parseInt(line.get(3).getToken()));
				m_tableAddress += 12;
				record.setNext(m_tableAddress.toString());

				Record ArrayDef = new Record();
				ArrayDef.setName("array");
				ArrayDef.setType("int");
				ArrayDef.setMemAddress(line.get(3).getToken());						//entering the number of cells in the array as the 3rd field in the record


				this.addToList(record);
				this.addToList(ArrayDef);
			}

			//--------------------------------------------------------------------------------------
			//if we have a regular 'int' statement. examples: int a,b; int a=3,b,c=5; int a=3; etc.
			//--------------------------------------------------------------------------------------
			else
			{
				ArrayList<ArrayList<Lexer.Token>> declarations = this.dismantleIntegerLine(line);
				for (int i=0; i<declarations.size(); i++)
				{
						ArrayList<Lexer.Token> lineParts = declarations.get(i);
						Record record = new Record();
						record.setName(lineParts.get(0).getToken());
						record.setType("var");
						record.setDefinition("int");
						record.setMemAddress(m_memoryAddress.toString());
						m_memoryAddress += 4;
						m_tableAddress += 20;
						record.setNext(m_tableAddress.toString());

						this.addToList(record);
				}
			}

		}
		//-------------------------------------------------------------------------------------------
		//if we have a "const" statement. examples: const int a; const int a=3,b; const int a=3,b=5;
		//-------------------------------------------------------------------------------------------
		else if (firstToken.equals("const"))
		{
			line.remove(0);														//removing the word "const"
			ArrayList<ArrayList<Lexer.Token>> declarations = this.dismantleIntegerLine(line);
			for (int i=0; i<declarations.size(); i++)
			{
				ArrayList<Lexer.Token> lineParts = declarations.get(i);
				if (lineParts.size() ==1 )
				{
					Record record = new Record();
					record.setName(lineParts.get(0).getToken());
					record.setType("const");
					record.setDefinition("int");
					record.setMemAddress("");		//there was no initialization- no value to the variable & no memory address for it.
					m_tableAddress += 20;
					record.setNext(m_tableAddress.toString());

					this.addToList(record);
				}
				if (lineParts.size() ==3)
				{
					Record record = new Record();
					record.setName(lineParts.get(0).getToken());
					record.setType("const");
					record.setDefinition("int");
					record.setMemAddress(lineParts.get(2).getToken());	//there was an initialization- assigning value instead of an address
					m_tableAddress += 20;
					record.setNext(m_tableAddress.toString());

					this.addToList(record);
				}
			}
		}
		//------------------------------------------------------------------------------------------------
		//if we have a "struct" statement. examples: struct z {int a;int b;} s, t;  struct Z {int a;} s;
		//------------------------------------------------------------------------------------------------
		else
		{
			ArrayList<ArrayList<Lexer.Token>> struct = this.getStructDefinition(line);
			ArrayList<Lexer.Token> variables = this.getStructVars(line);

			//----------------------------------
			// creating the struct's name record
			//----------------------------------
			Record structName = new Record();
			structName.setName(line.get(1).getToken());
			structName.setType("type");
			m_tableAddress += 20;
			structName.setDefinition(m_tableAddress.toString());
			structName.setMemAddress("");
			Integer next = m_tableAddress+8+20*variables.size();
			structName.setNext(next.toString());

			this.addToList(structName);

			//-----------------------------------------
			// creating the struct's definition record
			//-----------------------------------------
			Record structDefinition = new Record();
			Integer structDefAddress = m_tableAddress;
			structDefinition.setName("struct "+line.get(1).getToken());
			m_tableAddress += 8;
			structDefinition.setNext(m_tableAddress.toString());

			this.addToList(structDefinition);

			//-------------------------------------------------------------
			// creating a record for each variable in the struct definition
			//-------------------------------------------------------------
			for (int i=0; i<struct.size(); i++)
			{
				ArrayList<Lexer.Token> currentElement = struct.get(i);
				Record element = new Record();
				element.setName(currentElement.get(1).getToken());
				element.setType("field");
				element.setDefinition("int");
				element.setMemAddress("");
				m_tableAddress += 20;

				if ( i != struct.size()-1)
					element.setNext(m_tableAddress.toString());
				else
					element.setNext("null");

				this.addToList(element);
			}
			//---------------------------------------------------------------
			//creating a record for each actual variable of the type 'struct'
			//---------------------------------------------------------------
			for (int i=0; i<variables.size(); i++)
			{
				Record var = new Record();
				var.setName(variables.get(i).getToken());
				var.setType("var");
				var.setDefinition(structDefAddress.toString());
				var.setMemAddress(m_memoryAddress.toString());
				m_memoryAddress += struct.size()*4;

				m_tableAddress += 20;
				var.setNext(m_tableAddress.toString());

				this.addToList(var);
			}
		}
	}

	//-----------------------------------------------------------------------------------------
	// Function: addToList
	// Description: Checking if there isn't a duplicate statement of the same variable.
	//				If not - adding the new record of the variable into the list.
	//-----------------------------------------------------------------------------------------
	public void addToList(Record record)
	{
		if (!this.serachListByName(record.getName()))
			m_list.add(record);
		else
		{
			System.out.println("Variable already exists!");
			System.exit(1);
		}
	}

	//-----------------------------------------------------------------------------------------
	// Function: printTable()
	// Description: printing all records in the Linked List.
	//-----------------------------------------------------------------------------------------
	public void printTable()
	{
		int i=0;
		Iterator<Record> iterator = m_list.iterator();
		while (iterator.hasNext())
		{
			Record current = iterator.next();
			i++;
			current.printRecord(m_writer, i);
		}
	}

	//-----------------------------------------------------------------------------------------
	// Function: searchList
	// Description: Checking if a record with the given name already exists in the linked list.
	//-----------------------------------------------------------------------------------------
	public boolean serachListByName(String name)
	{
		Iterator<Record> iterator = m_list.iterator();
		while (iterator.hasNext())
		{
			Record current = iterator.next();
			if (current.getName().equals(name))
				return true;
		}
		return false;
	}

	//-----------------------------------------------------------------------------------------
	// Function: searchToken
	// Description: searching a specific token (by its name) in an arrayList of tokens
	//-----------------------------------------------------------------------------------------
	public boolean searchToken(String token, ArrayList<Lexer.Token> array)
	{
		for (int i=0; i<array.size(); i++)
		{
			if (array.get(i).getToken().equalsIgnoreCase(token))
				return true;
		}
		return false;
	}

	//-----------------------------------------------------------------------------------------
	// Function: lastToNull()
	// Description: making the last record on the list to point to null.
	//-----------------------------------------------------------------------------------------
	public void lastToNull()
	{
		this.m_list.getLast().setNext("null");
	}

	//------------------------------------------------------------------------------------------------
	// Function: dismantleIntegerLine
	// Description: receiving an array list of tokens that represents a line that started with "int".
	//				The function is creating a separate array list of tokens for each declaration.
	//				example: int a=3,b; --> 1st array list: a = 3
	//										2nd array list: b
	//------------------------------------------------------------------------------------------------
	public ArrayList<ArrayList<Lexer.Token>> dismantleIntegerLine(ArrayList<Lexer.Token> line)
	{
		ArrayList<ArrayList<Lexer.Token>> toReturn = new ArrayList<ArrayList<Lexer.Token>>();
		ArrayList<Lexer.Token> temp = new ArrayList<Lexer.Token>();

		for (int i=1; i<line.size(); i++) 	//starting from 1 to ignore the word "int"
		{
			if (   (line.get(i).getToken().compareTo(";"))!=0 && (line.get(i).getToken().compareTo(","))!=0 )
				temp.add(line.get(i));
			else
			{
				toReturn.add(temp);
				temp = new ArrayList<Lexer.Token>();
			}
		}

		return toReturn;
	}

	//-----------------------------------------------------------------------------------------
	// Function: getStructDef
	// Description: getting an array list of tokens that represents the definition of a struct.
	//				returning a single array list for every variable in the declaration.
	//-----------------------------------------------------------------------------------------
	public ArrayList<ArrayList<Lexer.Token>> getStructDefinition(ArrayList<Lexer.Token> line)
	{
		ArrayList<ArrayList<Lexer.Token>> toReturn = new ArrayList<ArrayList<Lexer.Token>>();
		ArrayList<Lexer.Token> temp = new ArrayList<Lexer.Token>();

		for (int i=3; i<line.size(); i++)  							//ignoring : "const x {"
		{
			if ( (line.get(i).getToken().equals("}")) )
				return toReturn;
			else if (   !(line.get(i).getToken().equals(";")) )
				temp.add(line.get(i));
			else
			{
				toReturn.add(temp);
				temp = new ArrayList<Lexer.Token>();
			}
		}

		return toReturn;
	}

	//------------------------------------------------------------------------------------------------
	// Function: getStructVars
	// Description: getting an array list of tokens representing a line.
	//				returning an array list of tokens representing the variables of the 'struct' kind.
	//				example: struct z{int x; int y;}s,t; --> will return only the tokens s,t
	//------------------------------------------------------------------------------------------------
	public ArrayList<Lexer.Token> getStructVars(ArrayList<Lexer.Token> line)
	{
		int index = 0;
		ArrayList<Lexer.Token> toReturn = new ArrayList<Lexer.Token>();

		for (int i=0; i<line.size(); i++)
		{
			if (line.get(i).getToken().equals("}"))
			{
				index = i;
				break;
			}
		}

		if (index+1 == line.size()) //if there are no variables after the struct declaration - returning an empty array list
			return toReturn;

		for (int i=index+1;i<line.size(); i++)
		{
			if (line.get(i).getToken_num() == 37) //if the token is an identifier- add it
				toReturn.add(line.get(i));
		}

		return toReturn;
	}

	//-----------------------------------------------------------------------------------------
	// Function: main
	//-----------------------------------------------------------------------------------------
	public static void main (String args[])
	{
		try
		{
			ex3_symbol symbolTable = new ex3_symbol();
			symbolTable.readWithLexer();
			symbolTable.createTable();
			symbolTable.lastToNull();
			symbolTable.printTable();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

}













