import java.io.BufferedWriter;
import java.io.IOException;


public class Record
{
	private String name;
	private String type;
	private String definition;
	private String memAddress;
	private String next;

	public Record()
	{
		this.name = null;
		this.type = null;
		this.definition = null;
		this.memAddress = null;
		this.next = null;
	}

	public String getName()
	{
		return name;
	}
	public void setName(String name)
{
		this.name = name;
	}
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		this.type = type;
	}
	public String getDefinition()
	{
		return definition;
	}
	public void setDefinition(String definition)
	{
		this.definition = definition;
	}
	public String getMemAddress()
	{
		return memAddress;
	}
	public void setMemAddress(String memAddress)
	{
		this.memAddress = memAddress;
	}
	public String getNext()
	{
		return next;
	}
	public void setNext(String next)
	{
		this.next = next;
	}

	//-----------------------------------------------------------------------------------------
	// Function:
	// Description:
	//-----------------------------------------------------------------------------------------
	public void printRecord(BufferedWriter writer, int recordNum)
	{
		try
		{
			writer.write("*********************** Record number : "+recordNum+" *****************************");
			writer.newLine();

			if (this.name != null)
			{
				if (this.name.contains("struct"))
					writer.write("struct");
				else
					writer.write(this.name);
				writer.newLine();
			}
			if (this.type != null)
			{
				writer.write(this.type);
				writer.newLine();
			}

			if (this.definition != null)
			{
				writer.write(this.definition+'\n');
				writer.newLine();
			}

			if (this.memAddress != null)
			{
				writer.write(this.memAddress+'\n');
				writer.newLine();
			}
			if (this.next != null)
			{
				writer.write(this.next+'\n'+'\n');
				writer.newLine();
			}

			writer.flush();
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

}
