import java.io.IOException;
import java.util.ArrayList;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser
{
    Variable lastVar;
    String lastOp;

    public SyntaxAnalyser(String fileName)
    {
        try
        {
            lex = new LexicalAnalyser(fileName);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void _statementPart_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("StatementPart");
        try
        {
            acceptTerminal(Token.beginSymbol);
            myGenerate.lowerScope();
            _statementList_();
            acceptTerminal(Token.endSymbol);
            myGenerate.raiseScope();
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<StatementPart> Error parsing", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("StatementPart");
    }

    public void _statementList_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("StatementList");
        try
        {
            _statement_();
            while (nextToken.symbol == Token.semicolonSymbol)
            {
                acceptTerminal(Token.semicolonSymbol);
                _statement_();
            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<StatementList> Error parsing", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("StatementList");
    }

    public void _statement_() throws IOException, CompilationException
    {
        //myGenerate.commenceNonterminal("Statement");
        try
        {
            switch (nextToken.symbol)
            {
                case Token.identifier:
                    myGenerate.commenceNonterminal("Statement");
                    _assignmentStatement_();
                    break;
                case Token.ifSymbol:
                    myGenerate.commenceNonterminal("Statement");
                    myGenerate.lowerScope();
                    _ifStatement_();
                    myGenerate.raiseScope();
                    break;
                case Token.whileSymbol:
                    myGenerate.commenceNonterminal("Statement");
                    myGenerate.lowerScope();
                    _whileStatement_();
                    myGenerate.raiseScope();
                    break;
                case Token.callSymbol:
                    myGenerate.commenceNonterminal("Statement");
                    _procedureStatement_();
                    break;
                case Token.doSymbol:
                    myGenerate.commenceNonterminal("Statement");
                    myGenerate.lowerScope();
                    _untilStatement_();
                    myGenerate.raiseScope();
                    break;
                case Token.forSymbol:
                    myGenerate.commenceNonterminal("Statement");
                    myGenerate.lowerScope();
                    _forStatement_();
                    myGenerate.raiseScope();
                    break;
                default:
                    throw new CompilationException("<Statement>: Expected valid statement FIRST token.", nextToken.lineNumber);
            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<Statement> Error parsing", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("Statement");
    }

    public void _assignmentStatement_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("AssignmentStatement");
        try
        {
            String ident = nextToken.text;
            Variable.Type type = Variable.Type.UNKNOWN;
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.becomesSymbol);
            switch (nextToken.symbol)
            {
                case Token.stringConstant:
                    type = Variable.Type.STRING;
                    acceptTerminal(Token.stringConstant);
                    break;
                case Token.numberConstant:
                    type = Variable.Type.NUMBER;
                case Token.identifier:
                case Token.leftParenthesis:
                    _expression_();
                    break;
                default:
                    myGenerate.reportError(nextToken, "<AssignmentStatement> Expected string constant or expression.");
            }
            if (!(type == Variable.Type.UNKNOWN))
            {
                if (myGenerate.getVariable(ident) == null)
                {
                    //myGenerate.addVariable(new Variable(ident, type));
                }
            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<AssignmentStatement> Error parsing", nextToken.lineNumber, ce);
        }

        myGenerate.finishNonterminal("AssignmentStatement");
    }

    public void _ifStatement_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("IfStatement");
        try
        {
            acceptTerminal(Token.ifSymbol);
            _condition_();
            acceptTerminal(Token.thenSymbol);
            _statementList_();
            _ifRemainder_();
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<IfStatement> Error parsing", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("IfStatement");
    }

    public void _ifRemainder_() throws IOException, CompilationException
    {
        switch (nextToken.symbol) //candidate for optimisation
        {
            case Token.endSymbol:
                acceptTerminal(Token.endSymbol);
                acceptTerminal(Token.ifSymbol);
                break;
            case Token.elseSymbol:
                acceptTerminal(Token.elseSymbol);
                _statementList_();
                acceptTerminal(Token.endSymbol);
                acceptTerminal(Token.ifSymbol);
                break;
            default:
                throw new CompilationException("<IfStatement> Expected \"else\" or \"end if\".", nextToken.lineNumber);
        }
    }

    public void _whileStatement_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("WhileStatement");
        try
        {
            acceptTerminal(Token.whileSymbol);
            _condition_();
            acceptTerminal(Token.loopSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol);
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<WhileStatement> Error parsing.", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("WhileStatement");
    }

    public void _procedureStatement_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("ProcedureStatement");
        try
        {
            acceptTerminal(Token.callSymbol);
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.leftParenthesis);
            _argumentList_();
            acceptTerminal(Token.rightParenthesis);
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<ProcedureStatement> Error parsing.", nextToken.lineNumber, ce);
        }

        myGenerate.finishNonterminal("ProcedureStatement");
    }

    public void _untilStatement_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("UntilStatement");
        try
        {
            acceptTerminal(Token.doSymbol);
            _statementList_();
            acceptTerminal(Token.untilSymbol);
            _condition_();
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<UntilStatement> Error parsing.", nextToken.lineNumber, ce);
        }

        myGenerate.finishNonterminal("UntilStatement");
    }

    public void _forStatement_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("ForStatement");
        try
        {
            acceptTerminal(Token.forSymbol);
            acceptTerminal(Token.leftParenthesis);
            _assignmentStatement_();
            acceptTerminal(Token.semicolonSymbol);
            _condition_();
            acceptTerminal(Token.semicolonSymbol);
            _assignmentStatement_();
            acceptTerminal(Token.rightParenthesis);
            acceptTerminal(Token.doSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol);
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<ForStatement> Error parsing.", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("ForStatement");
    }

    public void _argumentList_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("ArgumentList");
        try
        {
            acceptTerminal(Token.identifier);
            while (nextToken.symbol == Token.commaSymbol)
            {
                acceptTerminal(Token.commaSymbol);
                acceptTerminal(Token.identifier);
            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<ArgumentList> Error parsing.", nextToken.lineNumber, ce);
        }

        myGenerate.finishNonterminal("ArgumentList");
    }

    public void _condition_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("Condition");
        try
        {
            acceptTerminal(Token.identifier);
            _conditionalOperator_();
            switch (nextToken.symbol)
            {
                case Token.identifier:
                    acceptTerminal(Token.identifier);
                    break;
                case Token.numberConstant:
                    acceptTerminal(Token.numberConstant);
                    break;
                case Token.stringConstant:
                    acceptTerminal(Token.stringConstant);
                    break;
                default:
                    throw new CompilationException("<Condition> Expected identifier, number constant or string constant.", nextToken.lineNumber);

            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<Condition> Error parsing.", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("Condition");
    }

    public void _conditionalOperator_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("ConditionalOperator");
        try
        {
            switch (nextToken.symbol)
            {
                case Token.greaterThanSymbol:
                    acceptTerminal(Token.greaterThanSymbol);
                    break;
                case Token.greaterEqualSymbol:
                    acceptTerminal(Token.greaterEqualSymbol);
                    break;
                case Token.equalSymbol:
                    acceptTerminal(Token.equalSymbol);
                    break;
                case Token.notEqualSymbol:
                    acceptTerminal(Token.notEqualSymbol);
                    break;
                case Token.lessThanSymbol:
                    acceptTerminal(Token.lessThanSymbol);
                    break;
                case Token.lessEqualSymbol:
                    acceptTerminal(Token.lessEqualSymbol);
                    break;
                default:
                    throw new CompilationException("<ConditionalOperator> Expected conditional operator (>, >=, =, /=, <, <=).", nextToken.lineNumber);
            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<ConditionalOperator> Expected conditional operator (>, >=, =, /=, <, <=).", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("ConditionalOperator");
    }

    public void _expression_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("Expression");
        try
        {
            _term_();
            while (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol)
            {
                switch (nextToken.symbol)
                {
                    case Token.plusSymbol:
                        lastOp = "+";
                        acceptTerminal(Token.plusSymbol);
                        break;
                    case Token.minusSymbol:
                        lastOp = "-";
                        if (lastVar != null && lastVar.type == Variable.Type.STRING) //err if last "variable" is a String, cannot apply -
                        {
                            throw new CompilationException("<Expression> Cannot apply operator \"-\" to type String.", nextToken.lineNumber);
                        }
                        acceptTerminal(Token.minusSymbol);
                        break;
                    default:
                        throw new CompilationException("<Expression> Expected \"+\" or \"-\".", nextToken.lineNumber);
                }
                _term_();
            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<Expression> Expected conditional operator (>, >=, =, /=, <, <=).", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("Expression");
    }

    public void _term_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("Term");
        try
        {
            _factor_();
            while (nextToken.symbol == Token.timesSymbol || nextToken.symbol == Token.divideSymbol)
            {
                switch (nextToken.symbol)
                {
                    case Token.timesSymbol:
                        if (lastVar != null && lastVar.type == Variable.Type.STRING) //err if last "variable" is a String, cannot apply *
                        {
                            throw new CompilationException("<Term> Cannot apply operator \"*\" to type String.", nextToken.lineNumber);
                        }
                        acceptTerminal(Token.timesSymbol);
                        break;
                    case Token.divideSymbol:
                        if (lastVar != null &&lastVar.type == Variable.Type.STRING) //err if last "variable" is a String, cannot apply /
                        {
                            throw new CompilationException("<Term> Cannot apply operator \"\\\" to type String.", nextToken.lineNumber);
                        }
                        acceptTerminal(Token.divideSymbol);
                        break;
                    default:
                        throw new CompilationException("<Term> Expected \"*\" or \"\\\".", nextToken.lineNumber);
                }
                _term_();
            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<Term> Expected \"*\" or \"\\\".", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("Term");
    }

    public void _factor_() throws IOException, CompilationException
    {
        myGenerate.commenceNonterminal("Factor");
        try
        {
            switch (nextToken.symbol)
            {
                case Token.identifier:
                    acceptTerminal(Token.identifier);
                    break;
                case Token.numberConstant:
                    acceptTerminal(Token.numberConstant);
                    break;
                case Token.leftParenthesis:
                    acceptTerminal(Token.leftParenthesis);
                    _expression_();
                default:
                    throw new CompilationException("<Factor> Expected identifier, number constant or expression.", nextToken.lineNumber);
            }
        }
        catch (CompilationException ce)
        {
            throw new CompilationException("<Factor> Expected identifier, number constant or expression.", nextToken.lineNumber, ce);
        }
        myGenerate.finishNonterminal("Factor");
    }

    public void acceptTerminal(int symbol) throws IOException, CompilationException
    {

        if (nextToken.symbol == symbol)
        {
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
        }
        else
        {
            throw new CompilationException("<Terminal> Expected \"" + Token.getName(symbol) + "\"", nextToken.lineNumber);
        }
    }
}
