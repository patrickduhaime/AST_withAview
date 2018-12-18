package ast_withaview;

public class Classe {

	private String Name;
	private int methodCount;
	
	Classe(String Name)
	{
		this.setName(Name);
		this.setMethodCount(0);
		
	}

	public int getMethodCount() {
		return methodCount;
	}

	public void setMethodCount(int methodCount) {
		this.methodCount = methodCount;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}
	
	public String toString(){
		return "Class name is: " + this.getName()+ "\n" +
			   "Method count is: " + this.getMethodCount();
	}
}

