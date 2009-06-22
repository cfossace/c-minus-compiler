import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

//------------------------------------------------------------------------------------------
//Class: ex4_parser
//Description: this class is the actual C-- compiler!
//------------------------------------------------------------------------------------------
public class ex4_parser
{
	private int m_currentRegister;
    private ex2_interpret interpreter;
	private ex3_symbol m_symbolTable;
	private BufferedWriter m_writer;
	private StringBuilder m_stringBuilder;
	private int m_integerValue;


	//------------------------------------------------------------------------------------------
	// Function: ex4_praser
	// Description: Constructor
	//------------------------------------------------------------------------------------------
	public ex4_parser()
	{
		try
		{
			m_symbolTable = new ex3_symbol();
			m_symbolTable.readWithLexer();
			m_symbolTable.createTable();
			m_writer = new BufferedWriter(new FileWriter("interpreter.txt"));
			m_currentRegister = 0;
			m_stringBuilder = new StringBuilder();
			m_integerValue = 0;
			interpreter = new ex2_interpret();

		}
		catch (IOException e){System.out.println("Error creating a writer to the file prog.txt! "+e.getMessage());}
	}

	//----------------------------------------------------------------------------------------------------
	// Function: getCurrentRegister
	// Description: returning the next register number that we can put values in (the next free register)
	//----------------------------------------------------------------------------------------------------
	private int getCurrentRegister()
	{
        m_currentRegister++;
		m_currentRegister = m_currentRegister%32;
        if (m_currentRegister==0)
            m_currentRegister++;
        return m_currentRegister;
	}

	//-------------------------------------------------------------------------------------------
	// Function: GO()
	// Description: This is the main function of this class- it gets tokens in a closed list
	//				that represents the lines of code we need to interpret.
	//				every line could contain a cin command (read), cout (print) or an assignment.
	//-------------------------------------------------------------------------------------------

	public void GO() throws IOException
	{
		LinkedList<Token> ToDo = m_symbolTable.getRowsToDo();		//getting the actions to be performed
		Token currentToken = null;
		String newLine = System.getProperty("line.separator");

		ToDo.poll();
		ToDo.poll();
		ToDo.poll();
		ToDo.poll();
        ToDo.removeLast();

		currentToken = ToDo.poll();
		while (!ToDo.isEmpty())
		{
			//---------------------
			//if we got cin command
			//---------------------
			if (currentToken.getToken().equals("cin"))
			{
				//checking we got the right tokens and that the variable to read into do exist
				currentToken = ToDo.poll();
				if (!currentToken.getToken().equals(">>"))
					throw new IOException ("Wrong cin command! " +currentToken.getToken());
				currentToken = ToDo.poll();
				if ( !m_symbolTable.containsVariable(currentToken.getToken()))
					throw new IOException("No such Variable to read into! "+currentToken.getToken());

				//adding the suitable assembly code to the string builder
				int registerNum = getCurrentRegister();
				m_stringBuilder.append("RD"+"\t"+"0,0,"+registerNum+newLine);
				m_stringBuilder.append("STW"+"\t"+registerNum+",0,"+m_symbolTable.getTokenMemAddress(currentToken.getToken())+newLine);

				currentToken = ToDo.poll();
 				if (!currentToken.getToken().equals(";"))
					throw new IOException("Wrong syntax after cin! "+currentToken);
			}

			//----------------------------
			// if we got cout command
			//----------------------------
			else if (currentToken.getToken().equals("cout"))
			{
				//checking we got the right tokens and that the variable toprint do exist
				currentToken = ToDo.poll();
				if (!currentToken.getToken().equals("<<"))
					throw new IOException ("Wrong cout command! " +currentToken.getToken());
				currentToken = ToDo.getFirst();
				if ( !m_symbolTable.containsVariable(currentToken.getToken()))
					throw new IOException("No such Variable to print! "+currentToken.getToken());

				//calculating the register to print from and adding the suitable assembly code to the string builder
				int registerNum = getRegisterNum(ToDo);
				m_stringBuilder.append("WRD"+"\t"+"0,0,"+registerNum+newLine);
                ToDo.poll();
			}
			//----------------------------------------------------------------
			// if we got an identifier (This is going to be an assignment)
			//----------------------------------------------------------------
			else
			{
				//getting the memory address of the first variable
				ToDo.addFirst(currentToken);
                int memAddress=getMemAddress(ToDo);
                ToDo.poll();

               //calculating the register to stor data in and adding the suitable assembly code to the string builder
                int registerNum = getRegisterNum(ToDo);
                m_stringBuilder.append("STW"+"\t"+registerNum+",0,"+memAddress+newLine);
                ToDo.poll();
			}

			if (!ToDo.isEmpty())
				currentToken = ToDo.poll();
		}
		//writing all assembly code to the file "interpreter.txt"
		//and using the interpreter to perform the commands written in the file
		m_writer.write(m_stringBuilder.toString());
		m_writer.flush();

		interpreter.setVarsValue(m_symbolTable.getVarsWithValues());
		interpreter.readCommands();
        m_writer.close();

        this.WriteOutput();
	}

	//-------------------------------------------------------------------------------------------
	// Function: WriteOutput()
	// Description: This function prints to the file "output.txt". it prints the original
	//program, its assembly code, and the output.
	//-------------------------------------------------------------------------------------------
    private void WriteOutput()
    {
        try
        {
            String newLine = System.getProperty("line.separator");
            BufferedWriter w = new BufferedWriter(new FileWriter("output.txt"));
            BufferedReader r = new BufferedReader(new FileReader("prog.txt"));
            String str;
            w.write("The Original Program:"+newLine);
            while ((str = r.readLine())!=null)
            {
                w.write(str+newLine);
            }
            w.newLine();
            w.flush();
            r.close();
            r = new BufferedReader(new FileReader("interpreter.txt"));
            w.write("The Assembly Code:"+newLine);
            while ((str = r.readLine())!=null)
            {
                w.write(str+newLine);
            }
            w.newLine();
            w.flush();
            w.write("The Output:"+newLine);
            for (int i = 0;i<interpreter.GetAns().size();++i)
                w.write("output "+(i+1)+": "+interpreter.GetAns().get(i)+newLine);
            w.close();
            r.close();
        }catch(IOException e){System.out.println(e.getMessage());}
    }

    //-------------------------------------------------------------------------------------------
	// Function: getMemAddress
	// Description: This function calculates the value of the memory address of the variables
    // 				that is currently used in an assignment.
	//-------------------------------------------------------------------------------------------
	public int getMemAddress(LinkedList<Token> ToDo) throws IOException
	{
		Token token=ToDo.getFirst();
		if (token.getToken_num()==34) //if its a numebr
		{
			ToDo.poll();
			m_integerValue =Integer.parseInt(token.getToken());
			return -1;
		}
		ToDo.poll();
        String ZZ = token.getToken();
		Record rec = m_symbolTable.getRecordByName(ZZ);
		token = ToDo.getFirst();

		if ((token.getToken_num()==18) || (token.getToken_num()==30))//if we got "[" or "."
		{
			if(token.getToken_num()==18)
			{
				ToDo.poll();
				token = ToDo.poll();

				String inName=token.getToken();
				String name=ZZ+"."+inName;
                String ZZ_adress = m_symbolTable.GetSymbolAddress(name);
                return Integer.parseInt(ZZ_adress);
			}
			else
			{

				ToDo.poll();
				token=ToDo.poll();
				int index=Integer.parseInt(token.getToken());
				ToDo.poll();
				return (Integer.parseInt(rec.getMemAddress())+index*4);
			}

		}
		else if (rec != null)
		{
			String test = rec.getType();
			//if we got an unrecognized variable type - throw Exception
			if ( !test.equals("var") )
				if ( !test.equals("int") )
					throw new IOException ("Error!!!");

			return (Integer.parseInt(rec.getMemAddress()));
		}
        else
            throw new IOException ("Error!!!");
	}

	//-------------------------------------------------------------------------------------------
	// Function: getRegisterNum
	// Description: This function calculates the value of the register nunmber of the variables
    // 				that is currently used in an assignment.
	//-------------------------------------------------------------------------------------------
	public int getRegisterNum(LinkedList<Token> ToDo) throws IOException
	{
		int memory=0;
		int memory1=0;
		int registerNum=0;

		String newLine = System.getProperty("line.separator");

		int reg0=0;
        Token token=ToDo.getFirst();
        if (token.getToken_num()==29)
        {
        	ToDo.poll();
            int parreg1=getRegisterNum(ToDo);
            ToDo.poll();
            int oper =ToDo.poll().getToken_num();
            ToDo.poll();
            int parreg2=getRegisterNum(ToDo);
            ToDo.poll();
            switch (oper)
            {
            	//-----------
            	// multiply
            	//-----------
            	case (1):
            	{
                   	registerNum=getCurrentRegister();
                   	m_stringBuilder.append("MUL"+"\t"+registerNum+","+parreg1+","+parreg2+newLine);
                   	break;
            	}
                //-----------
                // divide
                //-----------
            	case (3):
            	{
                   registerNum=getCurrentRegister();
                   m_stringBuilder.append("DIV"+"\t"+registerNum+","+parreg1+","+parreg2+newLine);
                   break;
            	}
            	//-----------
                // add
                //-----------
            	case (6):
            	{
            		registerNum=getCurrentRegister();
            		m_stringBuilder.append("ADD"+"\t"+registerNum+","+parreg1+","+parreg2+newLine);
            		break;
            	}
                //-----------
                // subtract
                //-----------
                case (7):
                {
                   registerNum=getCurrentRegister();
                   m_stringBuilder.append("SUB"+"\t"+registerNum+","+parreg1+","+parreg2+newLine);
                   break;
                }
            }
        }

        else
        {
       if ((memory=getMemAddress(ToDo))==-1)
       {
           int num=m_integerValue;
           token=ToDo.poll();
           int regularGetRegister=getCurrentRegister();
           m_stringBuilder.append("ADDI"+"\t"+regularGetRegister+",0,"+num+newLine);
           int tokenNum = token.getToken_num();
           switch (tokenNum)
           {
           		//-----------
       			// multiply
       			//-----------
           		case (1)://mult
           		{
                   memory1=getMemAddress(ToDo);
                   reg0=getCurrentRegister();

                   m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory1+newLine);
                   registerNum=getCurrentRegister();
                   m_stringBuilder.append("MUL"+"\t"+registerNum+","+regularGetRegister+","+reg0+newLine);
                   break;
           		}
           		//-----------
       			// divide
       			//-----------
           		case (3):
           		{
                   memory1=getMemAddress(ToDo);
                   reg0=getCurrentRegister();
                   m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory1+newLine);
                   registerNum=getCurrentRegister();
                   m_stringBuilder.append("DIV"+"\t"+registerNum+","+regularGetRegister+","+reg0+newLine);
                   break;
           		}
           		//-----------
       			// add
       			//-----------
           		case (6):
           		{
                   memory1=getMemAddress(ToDo);
                   reg0=getCurrentRegister();
                   m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory1+newLine);
                   registerNum=getCurrentRegister();
                   m_stringBuilder.append("ADD"+"\t"+registerNum+","+regularGetRegister+","+reg0+newLine);
                   break;
           		}
           		//-----------
       			// subtract
       			//-----------
               case (7):
               {
                   memory1=getMemAddress(ToDo);
                   reg0=getCurrentRegister();
                   m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory1+newLine);
                   registerNum=getCurrentRegister();
                   m_stringBuilder.append("SUB"+"\t"+registerNum+","+regularGetRegister+","+reg0+newLine);
                   break;
               }

           }
       }
       else
       {
           int mem4=0;
           int operation=ToDo.poll().getToken_num();
           if ((mem4=getMemAddress(ToDo))==-1)//numeric case
           {
        	   int num=m_integerValue;

        	   switch (operation)
        	   {
        	   		//-----------
      				// multiply
      				//-----------
        	   		case (1):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory+newLine);
        	   			registerNum=getCurrentRegister();
        	   			m_stringBuilder.append("MULI"+"\t"+registerNum+","+reg0+","+num+newLine);
        	   			break;
        	   		}
        	   		//-----------
           			// divide
           			//-----------
        	   		case (3):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory+newLine);
        	   			registerNum=getCurrentRegister();
        	   			m_stringBuilder.append("DIVI"+"\t"+registerNum+","+reg0+","+num+newLine);
        	   			break;
        	   		}
        	   		//-----------
           			// add
           			//-----------
        	   		case (6):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory+newLine);
        	   			registerNum=getCurrentRegister();
        	   			m_stringBuilder.append("ADDI"+"\t"+registerNum+","+reg0+","+num+newLine);
        	   			break;
        	   		}
        	   		//-----------
           			// subtract
           			//-----------
        	   		case (7):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory+newLine);
        	   			registerNum=getCurrentRegister();
        	   			m_stringBuilder.append("SUBI"+"\t"+registerNum+","+reg0+","+num+newLine);
        	   			break;
        	   		}

        	   }//end of switch
           }
           else
           {
        	   int reg1=0;

        	   switch (operation)
        	   {
        	   		//-----------
      				// multiply
      				//-----------
        	   		case (1):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			reg1=getCurrentRegister();
        	   			m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory+newLine);
        	   			m_stringBuilder.append("LDW"+"\t"+reg1+",0,"+mem4+newLine);
        	   			registerNum=getCurrentRegister();
        	   			m_stringBuilder.append("MUL"+"\t"+registerNum+","+reg0+","+reg1+newLine);
        	   			break;
        	   		}
        	   		//-----------
           			// divide
           			//-----------
        	   		case (3):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			reg1=getCurrentRegister();
        	   			m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory+newLine);
        	   			m_stringBuilder.append("LDW"+"\t"+reg1+",0,"+mem4+newLine);
        	   			registerNum=getCurrentRegister();
        	   			m_stringBuilder.append("DIV"+"\t"+registerNum+","+reg0+","+reg1+newLine);
        	   			break;
        	   		}
        	   		//-----------
           			// add
           			//-----------
        	   		case (6):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			reg1=getCurrentRegister();
        	   			m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory+newLine);
        	   			m_stringBuilder.append("LDW"+"\t"+reg1+",0,"+mem4+newLine);
        	   			registerNum=getCurrentRegister();
        	   			m_stringBuilder.append("ADD"+"\t"+registerNum+","+reg0+","+reg1+newLine);
        	   			break;
        	   		}
        	   		//-----------
           			// subtract
           			//-----------
        	   		case (7):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			reg1=getCurrentRegister();
        	   			m_stringBuilder.append("LDW"+"\t"+reg0+",0,"+memory+newLine);
        	   			m_stringBuilder.append("LDW"+"\t"+reg1+",0,"+mem4+newLine);
        	   			registerNum=getCurrentRegister();
        	   			m_stringBuilder.append("SUB"+"\t"+registerNum+","+reg0+","+reg1+newLine);
        	   			break;
        	   		}

        	   }//end of switch
           }// end of else

         }
        }

        return registerNum;


	}


	//---------------------------------------------
	// MAIN
	// Create an instance of ex4_parser and run it
	//---------------------------------------------
	public static void main (String args[])
	{
		try
		{
			ex4_parser myEx4 = new ex4_parser();
			myEx4.GO();

		}
		catch (IOException e)
		{
			System.out.println("Error!!! "+e.getMessage());
			System.exit(1);
		}
	}
}
