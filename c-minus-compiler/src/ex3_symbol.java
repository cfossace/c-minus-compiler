import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

//--------------------------------------------------------------------------------------------------
//CLASS: ex3_symbol
//Description: This class is responsible for creating an appropriate symbol table for a given text
//--------------------------------------------------------------------------------------------------
public class ex3_symbol
{
	private Lexer m_lexer;
	private LinkedList<Record> m_list;
	private Integer m_memoryAddress;
	private Integer m_tableAddress;
	private Integer m_index;
	private ArrayList<String> m_structVars;
	private BufferedWriter m_writer;
	private LinkedList<Token> m_rowsWithoutStatements;
	private ArrayList<Integer> m_varsWithValues;

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
			m_structVars = new ArrayList<String>();
			m_index = -1;
			m_rowsWithoutStatements = new LinkedList<Token>();
			m_varsWithValues = new ArrayList<Integer>();
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
		ArrayList<ArrayList<Token>> lines = m_lexer.getLines();
		int check = -1;
		for (int i=0; i<lines.size(); i++)
		{
			check = handleLine(lines.get(i),i);
			if (check != -1)
			{
				i = check;
				m_index = -1;
			}
		}
	}

	//-----------------------------------------------------------------------------------------
	// Function: handleLine
	// Description: Getting an array list of tokens that represents a line from the input file.
	//				For every line- creating the correct Record & inserting it to the list.
	//-----------------------------------------------------------------------------------------
	public int handleLine(ArrayList<Token> line,int rowNum)
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
				ArrayList<ArrayList<Token>> declarations = this.dismantleIntegerLine(line);
				for (int i=0; i<declarations.size(); i++)
				{
					ArrayList<Token> lineParts = declarations.get(i);
					Record record = new Record();
					record.setName(lineParts.get(0).getToken());
					record.setType("var");
					record.setDefinition("int");
					record.setMemAddress(m_memoryAddress.toString());
					if (lineParts.size()==3)						//if we had a variable that got a value in the statement - save it !
					{
						m_varsWithValues.add(m_memoryAddress);		//we add the address and the value it needs to get
						m_varsWithValues.add(Integer.parseInt(lineParts.get(2).getToken()));
					}

					m_memoryAddress += 4;
					m_tableAddress += 20;
					record.setNext(m_tableAddress.toString());

					this.addToList(record);
				}
			}
			return -1;
		}
		//-------------------------------------------------------------------------------------------
		//if we have a "const" statement. examples: const int a; const int a=3,b; const int a=3,b=5;
		//-------------------------------------------------------------------------------------------
		else if (firstToken.equals("const"))
		{
			line.remove(0);														//removing the word "const"
			ArrayList<ArrayList<Token>> declarations = this.dismantleIntegerLine(line);
			for (int i=0; i<declarations.size(); i++)
			{
				ArrayList<Token> lineParts = declarations.get(i);
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
			return -1;
		}
		//------------------------------------------------------------------------------------------------
		//if we have a "struct" statement. examples: struct z {int a;int b;} s, t;  struct Z {int a;} s;
		//------------------------------------------------------------------------------------------------
		else if (firstToken.equals("struct"))
		{

			line = this.handleStructEnters(line, rowNum);

			ArrayList<ArrayList<Token>> struct = this.getStructDefinition(line);
			ArrayList<Token> variables = this.getStructVars(line);

			//----------------------------------
			// creating the struct's name record
			//----------------------------------
			Record structName = new Record();
			structName.setName(line.get(1).getToken());
			structName.setType("type");
			m_tableAddress += 20;
			structName.setDefinition(m_tableAddress.toString());
			structName.setMemAddress("");
			Integer next = m_tableAddress+8+20*struct.size();
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
				ArrayList<Token> currentElement = struct.get(i);
				Record element = new Record();

				//only if the variable was not already defined in the current struct- we continue (cannot be: int a; int a; in one struct)
				if (!this.setFieldName(currentElement.get(1).getToken()))
				{
					element.setName(currentElement.get(1).getToken());
					this.m_structVars.add(currentElement.get(1).getToken());
				}
				else
				{
					System.out.println("Double definition of variable "+currentElement.get(1).getToken()+" in struct "+line.get(1).getToken()+" !");
					System.exit(1);
				}
				element.setType("field");
				element.setDefinition("int");
				element.setMemAddress("");
				m_tableAddress += 20;

				if ( i != struct.size()-1)
					element.setNext(m_tableAddress.toString());
				else
					element.setNext("NIL");

				this.addStructVarToList(element);
			}
			this.m_structVars.clear();

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
		else
		{
			for (int i=0; i<line.size(); i++)
				m_rowsWithoutStatements.add(line.get(i));
		}

		return m_index;
	}

	//-----------------------------------------------------------------------------------------------------------
	// Function: handleStructEnters(int index)
	// Description: if there is a struct defined, but inside its statement there are enter chars,
	//				we read all the lines until the end of the statement and only then continue to deal with it.
	//-----------------------------------------------------------------------------------------------------------
	private ArrayList<Token> handleStructEnters(ArrayList<Token> currentLine, int index)
	{
		if ( (searchToken("{", currentLine)) && !(searchToken("}", currentLine)))
		{
			ArrayList<ArrayList<Token>> lines = m_lexer.getLines();
			ArrayList<Token> newLine = new ArrayList<Token>();


			while ( !searchToken("}", currentLine))
			{
				newLine.addAll(lines.get(index));
				index++;
				currentLine = lines.get(index);
			}
			newLine.addAll(currentLine);
			m_index = index;
			return newLine;
		}
		return currentLine;
	}


	//-----------------------------------------------------------------------------------------
	// Function: setFieldName
	// Description: Checking the there aren't 2 variables in the same name inside one struct.
	//-----------------------------------------------------------------------------------------
	private boolean setFieldName(String name)
	{
		for (int i=0 ; i<m_structVars.size(); i++)
			if (m_structVars.get(i).equals(name))
				return true;
		return false;
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
	// Function: addStructVarToList
	// Description: a struct's components can have the same names as variables that were
	//				already defined in the program, thats why we don't need to check it.
	//-----------------------------------------------------------------------------------------
	public void addStructVarToList(Record record)
	{
		m_list.add(record);
	}


	//-----------------------------------------------------------------------------------------
	// Function: printTable()
	// Description: printing all records in the Linked List.
	//-----------------------------------------------------------------------------------------
	public void printTable()
	{
		this.lastToNull();
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
	public boolean searchToken(String token, ArrayList<Token> array)
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
		this.m_list.getLast().setNext("NIL");
	}

	//------------------------------------------------------------------------------------------------
	// Function: dismantleIntegerLine
	// Description: receiving an array list of tokens that represents a line that started with "int".
	//				The function is creating a separate array list of tokens for each declaration.
	//				example: int a=3,b; --> 1st array list: a = 3
	//										2nd array list: b
	//------------------------------------------------------------------------------------------------
	public ArrayList<ArrayList<Token>> dismantleIntegerLine(ArrayList<Token> line)
	{
		ArrayList<ArrayList<Token>> toReturn = new ArrayList<ArrayList<Token>>();
		ArrayList<Token> temp = new ArrayList<Token>();

		for (int i=1; i<line.size(); i++) 	//starting from 1 to ignore the word "int"
		{
			if (   (line.get(i).getToken().compareTo(";"))!=0 && (line.get(i).getToken().compareTo(","))!=0 )
				temp.add(line.get(i));
			else
			{
				toReturn.add(temp);
				temp = new ArrayList<Token>();
			}
		}

		return toReturn;
	}

	//-----------------------------------------------------------------------------------------
	// Function: getStructDef
	// Description: getting an array list of tokens that represents the definition of a struct.
	//				returning a single array list for every variable in the declaration.
	//-----------------------------------------------------------------------------------------
	public ArrayList<ArrayList<Token>> getStructDefinition(ArrayList<Token> line)
	{
		ArrayList<ArrayList<Token>> toReturn = new ArrayList<ArrayList<Token>>();
		ArrayList<Token> temp = new ArrayList<Token>();

		for (int i=3; i<line.size(); i++)  							//ignoring : "const x {"
		{
			if ( (line.get(i).getToken().equals("}")) )
				return toReturn;
			else if (   !(line.get(i).getToken().equals(";")) )
				temp.add(line.get(i));
			else
			{
				toReturn.add(temp);
				temp = new ArrayList<Token>();
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
	public ArrayList<Token> getStructVars(ArrayList<Token> line)
	{
		int index = 0;
		ArrayList<Token> toReturn = new ArrayList<Token>();

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



	//------------------------------------------------------------------------------------------------------
	// Function: containsVariable
	// Description: This function return true if the symbol table contains a VARIABLE with the given name
	//------------------------------------------------------------------------------------------------------
	public boolean containsVariable(String name)
	{
		Iterator<Record> iterator = m_list.iterator();
		while (iterator.hasNext())
		{
			Record current = iterator.next();
			if (current.getName().equals(name) && (current.getType().equals("var")) )
				return true;
		}
		return false;
	}

	//--------------------------------------------------------------------------------------------------
	// Function: getTokenMemAddress
	// Description: This function is looking for a specific token buy its name & returning its address
	//---------------------------------------------------------------------------------------------------
	public String getTokenMemAddress(String name)
	{
		Iterator<Record> iterator = m_list.iterator();
		while (iterator.hasNext())
		{
			Record current = iterator.next();
			if (current.getName().equals(name))
				return current.getMemAddress();
		}
		return null;
	}

	//--------------------------------------------------------------------------------------------------
	// Function: getTokenMemAddress
	// Description: This function is looking for a specific token buy its name & returning it.
	//---------------------------------------------------------------------------------------------------
	public Record getRecordByName(String name)
	{
		Iterator<Record> iterator = m_list.iterator();
		while (iterator.hasNext())
		{
			Record current = iterator.next();
			if (current.getName().equals(name))
				return current;
		}
		return null;
	}

	//--------------------------------------------------------------------------------------------
	// Function: getRowsToDo
	// description: returning the linked list of all token from the lines that are not statements
	//				(these are lines with operation to do)
	//--------------------------------------------------------------------------------------------
	public LinkedList<Token> getRowsToDo()
	{
		return m_rowsWithoutStatements;
	}

	//-----------------------------------------------------------------------------------------
	// Function: GetSymbolAddress
	// Description: this function is searching for an existing variable in the table and
	//				returning its memory address.
	//-----------------------------------------------------------------------------------------
    public String GetSymbolAddress(String str)
    {
        //hello world - this is my first program
        int count = -1;
        String [] parse = new String[2];
        parse = str.split("[.]");
        if (this.serachListByName(parse[0])) //check if ZZ is exist
        {
            Record currentRec = this.getRecordByName(parse[0]); //get record by name from the list
            Iterator<Record> it = m_list.iterator();
            while (it.hasNext()) //looking for Z type
            {
                Record rec = it.next();
                if (rec.getDefinition()!=null && rec.getDefinition().equals(currentRec.getDefinition())) //got Z type
                {
                    while (it.hasNext())
                    {
                         Record z_rec = it.next();
                         if (z_rec.getName().equals(parse[1]))
                             return Integer.toString(Integer.parseInt(currentRec.getMemAddress())+4*count);
                         count ++;
                    }
                }
            }
        }
        return Integer.toString(-1);
    }

    //-----------------------------------------------------------------------------------------
	// Function: main getVarsWithValues()
    // Description: This function returns all variables that were initialized with a value.
    // it actually return an array of pairs- an address followed by the value it needs to get
	//-----------------------------------------------------------------------------------------
    public ArrayList<Integer> getVarsWithValues()
    {
    	return m_varsWithValues;
    }

	//-----------------------------------------------------------------------------------------
	// Function: MAIN
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













