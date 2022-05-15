package io.github.matteas.trivial.ast;

import io.github.matteas.trivial.combinator.Combinator;
import io.github.matteas.trivial.combinator.EvalError;
import java.util.Map;

public class AstIdentifier implements Ast {
    final String identifier;
    
    public AstIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    @Override
    public Combinator eval(Map<String, Combinator> scope) throws EvalError {
        // System.out.println("Id[" + identifier + "].eval");
        try {
            return scope.resolve(identifier);
        } catch (EvalError error) {
            throw new EvalError("Error evaluating identifier" + identifier, error);
        }
    }
    
    @Override
    public String toString() {
        return "Id('" + identifier + "')";
    }
}
