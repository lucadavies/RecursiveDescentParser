import java.util.ArrayList;
import java.util.Stack;

public class Generate extends AbstractGenerate
{
    private int indent = 0;
    private Stack<ArrayList<Variable>> scopeVars = new Stack<>();
    private ArrayList<Variable> allVars = new ArrayList<>();

    public void commenceNonterminal(String name)
    {
        indent();
        super.commenceNonterminal(name);
        indent++;
    }

    public void finishNonterminal(String name)
    {
        indent--;
        indent();
        super.finishNonterminal(name);
    }

    public void addVariable(Variable v)
    {
        indent();
        super.addVariable(v);
        scopeVars.peek().add(v);
        allVars.add(v);
    }

    public void removeVariable(Variable v)
    {
        indent();
        super.removeVariable(v);
        allVars.remove(v);
    }

    public Variable getVariable(String identifier)
    {
        Variable var = null;
        for (Variable v : allVars)
        {
            if (v.identifier.equals(identifier))
            {
                var = v;
            }
        }
        return var;
    }

    public void lowerScope()
    {
        scopeVars.push(new ArrayList<>());
    }

    public void raiseScope()
    {
        for (Variable v : scopeVars.peek())
        {
            removeVariable(v);
        }
        scopeVars.pop();
    }

    private void indent()
    {
        for (int i = 0; i < indent; i++)
        {
            System.out.print("    ");
        }
    }

    public void insertTerminal(Token token)
    {
        indent();
        super.insertTerminal(token);
    }

    public void reportSuccess()
    {
        indent();
        super.reportSuccess();
    }

    public void reportError(Token t, String msg) throws CompilationException
    {
        indent();
        System.out.println("rggERROR " + msg + " got \"" + t.text + "\".");
        throw new CompilationException(msg + " got \"" + t.text + "\".", t.lineNumber);
    }
}
