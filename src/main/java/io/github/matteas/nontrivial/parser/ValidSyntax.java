package io.github.matteas.nontrivial.parser;

import java.util.Set;
import java.util.Optional;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
    public final Optional<V> canAcceptEmptyTokenSequence;
    
    /**
     * Also known in literature as the "productivity".
     */
    public final boolean canAcceptSomeTokenSequence;
    
    public final Set<ShouldNotFollowEntry<V, K>> shouldNotFollow;

    protected ValidSyntax(
        Set<K> acceptableKinds,
        Optional<V> canAcceptEmptyTokenSequence,
        boolean canAcceptSomeTokenSequence,
        Set<ShouldNotFollowEntry<V, K>> shouldNotFollow
    ) {
        this.acceptableKinds = Set.copyOf(acceptableKinds);
        this.canAcceptEmptyTokenSequence = canAcceptEmptyTokenSequence;
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
        
        public Success(V value) {
            super(
                Collections.emptySet(),
                Optional.of(value),
                true,
                Collections.emptySet()
            );
            
            this.value = value;
        }
    }

    public static class Element<V extends Value<V>, K extends TokenKind> extends ValidSyntax<V, K> {
        public final K kind;
        
        public Element(K kind) {
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

        public Disjunction(
            ValidSyntax<V, K> left,
            ValidSyntax<V, K> right,
            Set<K> acceptableKinds,
            Optional<V> canAcceptEmptyTokenSequence,
            boolean canAcceptSomeTokenSequence,
            Set<ShouldNotFollowEntry<V, K>> shouldNotFollow
        ) {
            super(
                acceptableKinds,
                canAcceptEmptyTokenSequence,
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

        public Sequence(
            ValidSyntax<V, K> left,
            ValidSyntax<V, K> right,
            Set<K> acceptableKinds,
            Optional<V> canAcceptEmptyTokenSequence,
            boolean canAcceptSomeTokenSequence,
            Set<ShouldNotFollowEntry<V, K>> shouldNotFollow
        ) {
            super(
                acceptableKinds,
                canAcceptEmptyTokenSequence,
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
            
            return left.canAcceptEmptyTokenSequence
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

        public Transform(
            UnaryOperator<V> transformation,
            ValidSyntax<V, K> syntax,
            Optional<V> canAcceptEmptyTokenSequence
        ) {
            super(
                syntax.acceptableKinds,
                canAcceptEmptyTokenSequence,
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