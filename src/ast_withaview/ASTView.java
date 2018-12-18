package ast_withaview;

import java.util.HashSet;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

public class ASTView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ast_withaview.ASTView";
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	private TableViewer viewer;
	private Action doubleClickAction;
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

/**
	 * The constructor.
	 */
	public ASTView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(new String[] { "Lancer l'analyse"});
		viewer.setLabelProvider(new ViewLabelProvider());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "AST_withAview.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void makeActions() {
		doubleClickAction = new Action() {
			public void run() {
				showMessage("L'analyse des projets importés va être lancé.");
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"AST View",
			message);
		
		Analyse();
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	

	private void Analyse() {
		
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IWorkspaceRoot root = workspace.getRoot();
	// Get all projects in the workspace
	IProject[] projects = root.getProjects();
	// Loop over all projects
	for (IProject project : projects) {
	    try {
	        if (project.isNatureEnabled(JDT_NATURE)) {
	            analyseMethods(project);
	        }
	    } catch (CoreException e) {
	        e.printStackTrace();
	    }
	}
	}
	

	    private void analyseMethods(IProject project) throws JavaModelException {
        IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
        for (IPackageFragment mypackage : packages) {
            if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                createAST(mypackage);
            }

        }
    }

		private void createAST(IPackageFragment mypackage) throws JavaModelException {
        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
        	printIMethods(unit);
	    }
	}

	    private void printIMethods(ICompilationUnit unit) throws JavaModelException {
	        IType[] allTypes = unit.getAllTypes();
	        for (IType type : allTypes) {
	            printIMethodDetails(type);
	        }
	    }
	    
	    private void printIMethodDetails(IType type) throws JavaModelException {
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {
            HashSet<IMethod> callers = getCallersOf(method);
                       for(IMethod caller : callers)
            {
               System.out.println("Methode: "+method.getElementName());
        	   System.out.println("Parent de la methode: "+method.getParent().getElementName());
               System.out.println("Appelant de la methode: "+caller.getElementName());
               System.out.println("Parent de l'appelant: "+caller.getParent().getElementName());
               System.out.println("La classe: " + caller.getParent().getElementName() + " a une reference vers: " + method.getParent().getElementName() + "\n");
            }
        }
    }
	    @SuppressWarnings("restriction")
	    public HashSet<IMethod> getCallersOf(IMethod m) {
	    	 
	    	 CallHierarchy callHierarchy = CallHierarchy.getDefault();
	    	 
	    	 IMember[] members = {m};
	    	 
	    	 MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
	    	  HashSet<IMethod> callers = new HashSet<IMethod>();
	    	  for (MethodWrapper mw : methodWrappers) {
	    	    MethodWrapper[] mw2 = mw.getCalls(new NullProgressMonitor());
	    	    HashSet<IMethod> temp = getIMethods(mw2);
	    	    callers.addAll(temp);    
	    	   }
	    	 
	    	return callers;
	    	}
	    	@SuppressWarnings("restriction")
	    	 HashSet<IMethod> getIMethods( MethodWrapper[] methodWrappers) {
	    	  HashSet<IMethod> c = new HashSet<IMethod>(); 
	    	  for (MethodWrapper m : methodWrappers) {
	    	   IMethod im = getIMethodFromMethodWrapper(m);
	    	   if (im != null) {
	    	    c.add(im);
	    	   }
	    	  }
	    	  return c;
	    	 }
	    	 
	    	 @SuppressWarnings("restriction")
			IMethod getIMethodFromMethodWrapper( MethodWrapper m) {
	    	  try {
			IMember im = m.getMember();
	    	   if (im.getElementType() == IJavaElement.METHOD) {
	    	    return (IMethod)m.getMember();
	    	   }
	    	  } catch (Exception e) {
	    	   e.printStackTrace();
	    	  }
	    	  return null;
	    	 }
}

