package io.github.matteas.nontrivial.parser;

import java.util.Set;
import java.util.Optional;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Validated version of {@link Syntax} that can be safely used for parsing
 * via a {@link Parser}.
 *
 * To validate a syntax, call {@link Syntax@validate()}. A syntax is valid
 * if it is an ll(1) grammar.
 */
public abstract class ValidSyntax<V extends Value<V>, K extends TokenKind> {
    /**
     * Also known in literature as the "FIRST" set.
     * This is the {@link Set} of {@link TokenKind} that starts
     * a token sequence which this syntax recognizes.
     */
    public final Set<K> acceptableKinds;
    
    /**
     * Also known in literature as the "nullability".
     */
    public final Optional<V> canComplete;
    
    /**
     * Also known in literature as the "productivity".
     */
    public final boolean canAcceptSomeTokenSequence;
    
    public final Set<ShouldNotFollowEntry<V, K>> shouldNotFollow;

    protected ValidSyntax(
        Set<K> acceptableKinds,
        Optional<V> canComplete,
        boolean canAcceptSomeTokenSequence,
        Set<ShouldNotFollowEntry<V, K>> shouldNotFollow
    ) {
        this.acceptableKinds = Set.copyOf(acceptableKinds);
        this.canComplete = canComplete;
        this.canAcceptSomeTokenSequence = canAcceptSomeTokenSequence;
        this.shouldNotFollow = Set.copyOf(shouldNotFollow);
    }

    public final boolean accepts(K kind) {
        return acceptableKinds.contains(kind);
    }

    /**
     * Also known in literature as "pierce"
     */
    public abstract Focus<V, K> focus(K kind, Focus.Context<V, K> context);

    public static class Success<V extends Value<V>, K extends TokenKind> extends ValidSyntax<V, K> {
        public final V value;

        /**
         * Create a validated version of {@link Syntax.Success} syntax node.
         * Assumes node is already validated.
         * This constructor is package private so that the only way
         * to obtain a ValidSyntax node is by {@link Syntax#validate()}
         */
        Success(V value) {
            super(
                Collections.emptySet(),
                Optional.of(value),
                true,
                Collections.emptySet()
            );
            
            this.value = value;
        }
        
        @Override
        public Focus<V, K> focus(K kind, Focus.Context<V, K> context) {
            throw new UnsupportedOperationException(
                "Focussing torwards a node that can accept any non-empty "
                + "token should never reach a Success node, since the success "
                + "node only accepts the empty token sequence."
            );
        }
    }

    public static class Element<V extends Value<V>, K extends TokenKind> extends ValidSyntax<V, K> {
        public final K kind;
        
        /**
         * Create a validated version of {@link Syntax.Element} syntax node.
         * Assumes node is already validated.
         * This constructor is package private so that the only way
         * to obtain a ValidSyntax node is by {@link Syntax#validate()}
         */
        Element(K kind) {
            super(
                Set.of(kind),
                Optional.empty(),
                true,
                Collections.emptySet()
            );
            
            this.kind = kind;
        }
        
        @Override
        public Focus<V, K> focus(K kind, Focus.Context<V, K> context) {
            assert this.kind == kind;
            return new Focus<>(this, context);
        }
    }
    
    public static class Disjunction<V extends Value<V>, K extends TokenKind> extends ValidSyntax<V, K> {
        public final ValidSyntax<V, K> left;
        public final ValidSyntax<V, K> right;

        /**
         * Create a validated version of {@link Syntax.Disjunction} syntax node.
         * Assumes node is already validated.
         * This constructor is package private so that the only way
         * to obtain a ValidSyntax node is by {@link Syntax#validate()}
         */
        Disjunction(
            ValidSyntax<V, K> left,
            ValidSyntax<V, K> right,
            Set<K> acceptableKinds,
            Optional<V> canComplete,
            boolean canAcceptSomeTokenSequence,
            Set<ShouldNotFollowEntry<V, K>> shouldNotFollow
        ) {
            super(
                acceptableKinds,
                canComplete,
                canAcceptSomeTokenSequence,
                shouldNotFollow
            );
            
            this.left = left;
            this.right = right;
        }
        
        @Override
        public Focus<V, K> focus(K kind, Focus.Context<V, K> context) {
            if (left.accepts(kind)) {
                return left.focus(kind, context);
            }
            return right.focus(kind, context);
        }
    }
    
    public static class Sequence<V extends Value<V>, K extends TokenKind> extends ValidSyntax<V, K> {
        public final ValidSyntax<V, K> left;
        public final ValidSyntax<V, K> right;

        /**
         * Create a validated version of {@link Syntax.Sequence} syntax node.
         * Assumes node is already validated.
         * This constructor is package private so that the only way
         * to obtain a ValidSyntax node is by {@link Syntax#validate()}
         */
        Sequence(
            ValidSyntax<V, K> left,
            ValidSyntax<V, K> right,
            Set<K> acceptableKinds,
            Optional<V> canComplete,
            boolean canAcceptSomeTokenSequence,
            Set<ShouldNotFollowEntry<V, K>> shouldNotFollow
        ) {
            super(
                acceptableKinds,
                canComplete,
                canAcceptSomeTokenSequence,
                shouldNotFollow
            );
            
            this.left = left;
            this.right = right;
        }

        @Override
        public Focus<V, K> focus(K kind, Focus.Context<V, K> context) {
            final Supplier<Focus<V, K>> leftFocus = () ->
                left.focus(kind, new Focus.Context.FollowBy<>(right, context));
            
            return left.canComplete
                .map(v -> {
                    if (left.accepts(kind)) {
                        return leftFocus.get();
                    }
                    return right.focus(kind, new Focus.Context.Prepend<>(v, context));
                })
                .orElseGet(leftFocus);
        }
    }
    
    public static class Transform<V extends Value<V>, K extends TokenKind> extends ValidSyntax<V, K> {
        public final UnaryOperator<V> transformation;
        public final ValidSyntax<V, K> syntax;

        /**
         * Create a validated version of {@link Syntax.Transform} syntax node.
         * Assumes node is already validated.
         * This constructor is package private so that the only way
         * to obtain a ValidSyntax node is by {@link Syntax#validate()}
         */
        Transform(
            UnaryOperator<V> transformation,
            ValidSyntax<V, K> syntax,
            Optional<V> canComplete
        ) {
            super(
                syntax.acceptableKinds,
                canComplete,
                syntax.canAcceptSomeTokenSequence,
                syntax.shouldNotFollow
            );
            this.transformation = transformation;
            this.syntax = syntax;
        }
        
        @Override
        public Focus<V, K> focus(K kind, Focus.Context<V, K> context) {
            return syntax.focus(kind, new Focus.Context.Apply<>(transformation, context));
        }
    }
}
