package ast_withaview;

import java.util.ArrayList;
import java.util.List;

public class Classe {

	private String Name;
	private int methodCount;
	private List<Classe> callerList;
	
	Classe(String Name)
	{
		this.setName(Name);
		this.setMethodCount(0);
		this.setCallerList(new ArrayList<Classe>());
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
	
	public List<Classe> getCallerList() {
		return callerList;
	}

	public void setCallerList(List<Classe> callerList) {
		this.callerList = callerList;
	}

	public String toString(){
		return "Class name is: " + this.getName()+ "\n" +
			   "Method count is: " + this.getMethodCount() + "\n" +
			   "Caller list: " +this.getCallerList();
		
	}
}

