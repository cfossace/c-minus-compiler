import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


public class ex4_parser
{
	private int m_currentRegister;
    private ex2_interpret interpreter;
	private ex3_symbol m_symbolTable;
	private BufferedWriter m_writer;
	private StringBuilder m_stringBuilder;
	private int m_integerValue;



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

		}
		catch (IOException e){System.out.println("Error creating a writer to the file prog.txt! "+e.getMessage());}
	}

	private int getCurrentRegister()
	{
        m_currentRegister++;
		m_currentRegister = m_currentRegister%32;
        if (m_currentRegister==0)
            m_currentRegister++;
        return m_currentRegister;
	}

	public void GO() throws IOException
	{
		LinkedList<Token> ToDo = m_symbolTable.getRowsToDo();		//getting the actions to be performed
		Token currentToken = null;
		// sb = new StringBuilder();
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
				currentToken = ToDo.poll();
				if (!currentToken.getToken().equals(">>"))
					throw new IOException ("Wrong cin command! " +currentToken.getToken());
				currentToken = ToDo.poll();
				if ( !m_symbolTable.containsVariable(currentToken.getToken()))
					throw new IOException("No such Variable to read into! "+currentToken.getToken());

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
				currentToken = ToDo.poll();
				if (!currentToken.getToken().equals("<<"))
					throw new IOException ("Wrong cout command! " +currentToken.getToken());
				currentToken = ToDo.getFirst();
				if ( !m_symbolTable.containsVariable(currentToken.getToken()))
					throw new IOException("No such Variable to print! "+currentToken.getToken());
				int registerNum = getRegisterNum(ToDo);
				m_stringBuilder.append("WRD"+"\t"+"0,0,"+registerNum+newLine);
                ToDo.poll();
			}
			//------------------------
			// if we got an identifier
			//------------------------
			else
			{
				ToDo.addFirst(currentToken);
                int memAddress=getMemAddress(ToDo);
                ToDo.poll();
                int registerNum = getRegisterNum(ToDo);
                m_stringBuilder.append("STW"+"\t"+registerNum+",0,"+memAddress+newLine);
                ToDo.poll();
			}

			if (!ToDo.isEmpty())
				currentToken = ToDo.poll();
		}
		m_writer.write(m_stringBuilder.toString());
		m_writer.flush();
		interpreter = new ex2_interpret();
		interpreter.readCommands();
        m_writer.close();
        this.WriteOutput();
	}

    private void WriteOutput()
    {
        try
        {
            String newLine = System.getProperty("line.separator");
            BufferedWriter w = new BufferedWriter(new FileWriter("output.txt"));
            BufferedReader r = new BufferedReader(new FileReader("prog.txt"));
            String str;
            while ((str = r.readLine())!=null)
            {
                w.write(str+newLine);
            }
            w.newLine();
            w.flush();
            r.close();
            r = new BufferedReader(new FileReader("interpreter.txt"));
            while ((str = r.readLine())!=null)
            {
                w.write(str+newLine);
            }
            w.newLine();
            w.flush();
            for (int i = 0;i<interpreter.GetAns().size();++i)
                w.write("output "+(i+1)+": "+interpreter.GetAns().get(i)+newLine);
            w.close();
            r.close();
        }catch(IOException e){System.out.println(e.getMessage());}
    }

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
		//String structInstanceName = token.getToken();
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
                String ZZ_adress = m_symbolTable.GetSymbolAddress(name); /**CHECK**/
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
			//if (  (!(record.getType().equals("var")) ) || (!(record.getType().equals("int"))) )
			if ( !test.equals("var") )
				if ( !test.equals("int") )
					throw new IOException ("Error!!!");

			return (Integer.parseInt(rec.getMemAddress()));
		}
        else
            throw new IOException ("Error!!!");
	}

	public int getRegisterNum(LinkedList<Token> ToDo) throws IOException
	{
		int memory=0;
		int memory1=0;
		int registerNum=0;

		String newLine = System.getProperty("line.separator");
		String commandName=new String();

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
            		commandName="";
                   	registerNum=getCurrentRegister();
                   	commandName="MUL"+"\t"+registerNum+","+parreg1+","+parreg2;
                   	m_stringBuilder.append(commandName+newLine);
                   	break;
            	}
                //-----------
                // divide
                //-----------
            	case (3):
            	{
                   commandName="";
                   registerNum=getCurrentRegister();
                   commandName="DIV"+"\t"+registerNum+","+parreg1+","+parreg2;
                   m_stringBuilder.append(commandName+newLine);
                   break;
            	}
            	//-----------
                // add
                //-----------
            	case (6):
            	{
            		commandName="";
            		registerNum=getCurrentRegister();
            		commandName="ADD"+"\t"+registerNum+","+parreg1+","+parreg2;
            		m_stringBuilder.append(commandName+newLine);
            		break;
            	}
                //-----------
                // subtract
                //-----------
                case (7):
                {
                	commandName="";
                   registerNum=getCurrentRegister();
                   commandName="SUB"+"\t"+registerNum+","+parreg1+","+parreg2;
                   m_stringBuilder.append(commandName+newLine);
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
           int regMaster=getCurrentRegister();
           commandName="ADDI"+"\t"+regMaster+",0,"+num;
           m_stringBuilder.append(commandName+newLine);
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
                   commandName="LDW"+"\t"+reg0+",0,"+memory1;

                   m_stringBuilder.append(commandName+newLine);
                   registerNum=getCurrentRegister();
                   commandName="MUL"+"\t"+registerNum+","+regMaster+","+reg0;
                   m_stringBuilder.append(commandName+newLine);
                   break;
           		}
           		//-----------
       			// divide
       			//-----------
           		case (3):
           		{
                   memory1=getMemAddress(ToDo);
                   reg0=getCurrentRegister();
                   commandName="LDW"+"\t"+reg0+",0,"+memory1;

                  m_stringBuilder.append(commandName+newLine);
                   commandName="";
                   registerNum=getCurrentRegister();
                   commandName="DIV"+"\t"+registerNum+","+regMaster+","+reg0;
                   m_stringBuilder.append(commandName+newLine);
                   break;
           		}
           		//-----------
       			// add
       			//-----------
           		case (6):
           		{
                   memory1=getMemAddress(ToDo);
                   reg0=getCurrentRegister();
                   commandName="LDW"+"\t"+reg0+",0,"+memory1;

                   m_stringBuilder.append(commandName+newLine);
                   commandName="";
                   registerNum=getCurrentRegister();
                   commandName="ADD"+"\t"+registerNum+","+regMaster+","+reg0;
                   m_stringBuilder.append(commandName+newLine);
                   break;
           		}
           		//-----------
       			// subtract
       			//-----------
               case (7):
               {
                   memory1=getMemAddress(ToDo);
                   reg0=getCurrentRegister();
                   commandName="LDW"+"\t"+reg0+",0,"+memory1;

                   m_stringBuilder.append(commandName+newLine);
                   commandName="";
                   registerNum=getCurrentRegister();
                   commandName="SUB"+"\t"+registerNum+","+regMaster+","+reg0;
                   m_stringBuilder.append(commandName+newLine);
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
        	   		case (1):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			commandName="LDW"+"\t"+reg0+",0,"+memory;

        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="";
        	   			registerNum=getCurrentRegister();
        	   			commandName="MULI"+"\t"+registerNum+","+reg0+","+num;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			break;
        	   		}
        	   		case (3):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			commandName="LDW"+"\t"+reg0+",0,"+memory;

        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="";
        	   			registerNum=getCurrentRegister();
        	   			commandName="DIVI"+"\t"+registerNum+","+reg0+","+num;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			break;
        	   		}
        	   		case (6):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			commandName="LDW"+"\t"+reg0+",0,"+memory;

        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="";
        	   			registerNum=getCurrentRegister();
        	   			commandName="ADDI"+"\t"+registerNum+","+reg0+","+num;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			break;
        	   		}
        	   		case (7):
        	   		{
        	   			reg0=getCurrentRegister();
        	   			commandName="LDW"+"\t"+reg0+",0,"+memory;

        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="";
        	   			registerNum=getCurrentRegister();
        	   			commandName="SUBI"+"\t"+registerNum+","+reg0+","+num;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			break;
        	   		}

        	   }//end of switch
           }
           else
           {
        	   int reg1=0;

        	   switch (operation)
        	   {
        	   		case (1)://mult
        	   		{
        	   			//     mem2=getMemAddress(tokens);
        	   			reg0=getCurrentRegister();
        	   			commandName="LDW"+"\t"+reg0+",0,"+memory;
        	   			reg1=getCurrentRegister();
        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="LDW"+"\t"+reg1+",0,"+mem4;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="";
        	   			registerNum=getCurrentRegister();
        	   			commandName="MUL"+"\t"+registerNum+","+reg0+","+reg1;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			break;
        	   		}
        	   		case (3)://div
        	   		{
        	   			reg0=getCurrentRegister();
        	   			commandName="LDW"+"\t"+reg0+",0,"+memory;
        	   			reg1=getCurrentRegister();
        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="LDW"+"\t"+reg1+",0,"+mem4;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="";
        	   			registerNum=getCurrentRegister();
        	   			commandName="DIV"+"\t"+registerNum+","+reg0+","+reg1;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			break;
        	   		}
        	   		case (6)://plus
        	   		{
        	   			reg0=getCurrentRegister();
        	   			commandName="LDW"+"\t"+reg0+",0,"+memory;
        	   			reg1=getCurrentRegister();
        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="LDW"+"\t"+reg1+",0,"+mem4;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="";
        	   			registerNum=getCurrentRegister();
        	   			commandName="ADD"+"\t"+registerNum+","+reg0+","+reg1;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			break;
        	   		}
        	   		case (7)://minus
        	   		{
        	   			reg0=getCurrentRegister();
        	   			commandName="LDW"+"\t"+reg0+",0,"+memory;
        	   			reg1=getCurrentRegister();
        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="LDW"+"\t"+reg1+",0,"+mem4;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			commandName="";
        	   			registerNum=getCurrentRegister();
        	   			commandName="SUB"+"\t"+registerNum+","+reg0+","+reg1;
        	   			m_stringBuilder.append(commandName+newLine);
        	   			break;
        	   		}

        	   }//end of switch
           }// end of else

         }
        }

        return registerNum;


	}



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
