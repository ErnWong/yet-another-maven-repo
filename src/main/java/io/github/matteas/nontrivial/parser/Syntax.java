package io.github.matteas.nontrivial.parser;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Syntax<V extends Value, K extends TokenKind> {
    /**
     * Also known in literature as the "FIRST" set.
     * This is the {@link Set} of {@link TokenKind} that starts
     * a token sequence which this syntax recognizes.
     */
    protected final InductiveProperty<Set<K>> acceptableKinds;
    
    /**
     * Also known in literature as the "nullability".
     */
    protected final InductiveProperty<Optional<V>> canAcceptEmptyTokenSequence;
    
    /**
     * Also known in literature as the "productivity".
     */
    protected final InductiveProperty<Boolean> canAcceptSomeTokenSequence;
    
    protected final InductiveProperty<Set<ShouldNotFollowEntry<V, K>>> shouldNotFollow;
    protected final InductiveProperty<Set<Conflict>> conflicts;

    protected Syntax(
        InductiveProperty<Set<K>> acceptableKinds,
        InductiveProperty<Optional<V>> canAcceptEmptyTokenSequence,
        InductiveProperty<Boolean> canAcceptSomeTokenSequence,
        InductiveProperty<Set<ShouldNotFollowEntry<V, K>>> shouldNotFollow,
        InductiveProperty<Set<Conflict>> conflicts
    ) {
        this.acceptableKinds = acceptableKinds;
        this.canAcceptEmptyTokenSequence = canAcceptEmptyTokenSequence;
        this.canAcceptSomeTokenSequence = canAcceptSomeTokenSequence;
        this.shouldNotFollow = shouldNotFollow;
        this.conflicts = conflicts;
    }

    public static class ShouldNotFollowEntry<V extends Value, K extends TokenKind> {
        public final Disjunction<V, K> source;
        public final Set<K> disallowedKinds;

        public ShouldNotFollowEntry(Disjunction<V, K> source, Set<K> disallowedKinds) {
            this.source = source;
            this.disallowedKinds = disallowedKinds;
        }
    }

    public interface Conflict {}

    /**
     * Also known in literature as Nullability/Nullability conflict.
     */
    public class BothAcceptsEmptySequenceConflict implements Conflict {
        public final Disjunction<V, K> source;
        public BothAcceptsEmptySequenceConflict(Disjunction<V, K> source) {
            this.source = source;
        }
    }
    
    /**
     * Also known in literature as First/First conflict.
     */
    public class BothAcceptsSameFirstTokenKindConflict implements Conflict {
        public final Disjunction<V, K> source;
        public final Set<K> ambiguities;
        public BothAcceptsSameFirstTokenKindConflict(Disjunction<V, K> source, Set<K> ambiguities) {
            this.source = source;
            this.ambiguities = ambiguities;
        }
    }
    
    /**
     * Also known in literature as First/Follow conflict.
     */
    public class FollowConflict implements Conflict {
        public final Disjunction<V, K> source;
        public final Sequence<V, K> root;
        public final Set<K> ambiguities;
        public FollowConflict(Disjunction<V, K> source, Sequence<V, K> root, Set<K> ambiguities) {
            this.source = source;
            this.root = root;
            this.ambiguities = ambiguities;
        }
    }

    protected abstract void realize();
    protected abstract ValidSyntax toValidSyntaxUnchecked();

    public final ValidationResult validate() {
        realize();
        if (conflicts.get().isEmpty()) {
            return new ValidationResult.Ok(toValidSyntaxUnchecked());
        }
        return new ValidationResult.Error(this);
    }

    public static class Success<V extends Value, K extends TokenKind> extends Syntax<V, K> {
        public final V value;
        
        public Success(V value) {
            super(
                new InductiveProperty.Constant<>(Collections.emptySet()),
                new InductiveProperty.Constant<>(Optional.of(value)),
                new InductiveProperty.Constant<>(true),
                new InductiveProperty.Constant<>(Collections.emptySet()),
                new InductiveProperty.Constant<>(Collections.emptySet())
            );
            
            this.value = value;
        }
        
        @Override
        protected void realize() {
            // Nothing to do.
        }
        
        @Override
        protected ValidSyntax toValidSyntaxUnchecked() {
            return new ValidSyntax.Success(value);
        }
    }

    public static class Element<V extends Value, K extends TokenKind> extends Syntax<V, K> {
        public final K kind;
        
        public Element(K kind) {
            super(
                new InductiveProperty.Constant<>(Set.of(kind)),
                new InductiveProperty.Constant<>(Optional.empty()),
                new InductiveProperty.Constant<>(true),
                new InductiveProperty.Constant<>(Collections.emptySet()),
                new InductiveProperty.Constant<>(Collections.emptySet())
            );
            
            this.kind = kind;
        }
        
        @Override
        protected void realize() {
            // Nothing to do.
        }
        
        @Override
        protected ValidSyntax toValidSyntaxUnchecked() {
            return new ValidSyntax.Element(kind);
        }
    }
    
    public static class Disjunction<V extends Value, K extends TokenKind> extends Syntax<V, K> {
        public final Syntax<V, K> left;
        public final Syntax<V, K> right;

        public Disjunction(Syntax<V, K> left, Syntax<V, K> right) {
            super(
                new InductiveProperty.Rule<>(
                    List.of(left.acceptableKinds, right.acceptableKinds),
                    () -> Stream.concat(
                        left.acceptableKinds.get().stream(),
                        right.acceptableKinds.get().stream()
                    ).collect(Collectors.toSet())
                ),
                new InductiveProperty.Rule<>(
                    List.of(left.canAcceptEmptyTokenSequence, right.canAcceptEmptyTokenSequence),
                    () -> left.canAcceptEmptyTokenSequence.get()
                        .or(() -> right.canAcceptEmptyTokenSequence.get())
                ),
                new InductiveProperty.Rule<>(
                    List.of(left.canAcceptSomeTokenSequence, right.canAcceptSomeTokenSequence),
                    () -> left.canAcceptSomeTokenSequence.get()
                        || right.canAcceptSomeTokenSequence.get()
                ),
                new InductiveProperty.Rule<>(
                    List.of(
                        left.acceptableKinds,
                        left.canAcceptEmptyTokenSequence,
                        right.acceptableKinds,
                        right.canAcceptEmptyTokenSequence,
                    ),
                    () -> {
                        final Set<ShouldNotFollowEntry<V, K>> entries = Stream.concat(
                            left.shouldNotFollow.get().stream(),
                            right.shouldNotFollow.get().stream()
                        ).collect(Collectors.toCollection(HashSet::new));
                        
                        if (left.canAcceptEmptyTokenSequence.get().isPresent()) {
                            entries.add(new ShouldNotFollowEntry<>(this, right.acceptableKinds.get()));
                        }
                        if (right.canAcceptEmptyTokenSequence.get().isPresent()) {
                            entries.add(new ShouldNotFollowEntry<>(this, left.acceptableKinds.get()));
                        }
                        
                        return entries;
                    }
                ),
                new InductiveProperty.Rule<>(
                    List.of(
                        left.canAcceptEmptyTokenSequence,
                        left.acceptableKinds,
                        left.shouldNotFollow,
                        left.conflicts,
                        right.canAcceptEmptyTokenSequence,
                        right.acceptableKinds,
                        right.shouldNotFollow,
                        right.conflicts
                    ),
                    () -> {
                        final Set<Conflict> conflicts = Stream.concat(
                            left.conflicts.get().stream(),
                            right.conflicts.get().stream()
                        ).collect(Collectors.toCollection(HashSet::new));
                        
                        if (left.canAcceptEmptyTokenSequence.get().isPresent()
                                && right.canAcceptEmptyTokenSequence.get().isPresent()) {
                            conflicts.add(new BothAcceptsEmptySequenceConflict(this));
                        }
    
                        final Set<K> firstFirstAmbiguities = new HashSet<>(left.acceptableKinds.get());
                        firstFirstAmbiguities.retainAll(right.acceptableKinds.get());
                        if (!firstFirstAmbiguities.isEmpty()) {
                            conflicts.add(new BothAcceptsSameFirstTokenKindConflict(this, firstFirstAmbiguities));
                        }
                        
                        return conflicts;
                    }
                )
            );
            
            this.left = left;
            this.right = right;
        }
        
        @Override
        protected void realize() {
            left.realize();
            right.realize();
        }
        
        @Override
        protected ValidSyntax toValidSyntaxUnchecked() {
            return new ValidSyntax.Disjunction(
                left.toValidSyntaxUnchecked(),
                right.toValidSyntaxUnchecked(),
                acceptableKinds.get(),
                canAcceptEmptyTokenSequence.get(),
                canAcceptSomeTokenSequence.get(),
                shouldNotFollow.get()
            );
        }
    }
    
    public static class Sequence<V extends Value, K extends TokenKind> extends Syntax<V, K> {
        public final Syntax<V, K> left;
        public final Syntax<V, K> right;
        
        public Sequence(Syntax<V, K> left, Syntax<V, K> right) {
            super(
                new InductiveProperty.Rule<>(
                    List.of(
                        left.acceptableKinds,
                        left.canAcceptEmptyTokenSequence,
                        right.canAcceptSomeTokenSequence,
                        right.acceptableKinds
                    ),
                    () -> {
                        final Set<K> kinds = new HashSet<>();
                        if (right.canAcceptSomeTokenSequence.get()) {
                            kinds.addAll(left.acceptableKinds.get());
                        }
                        if (left.canAcceptEmptyTokenSequence.get().isPresent()) {
                            kinds.addAll(right.acceptableKinds.get());
                        }
                        return kinds;
                    }
                ),
                new InductiveProperty.Rule<>(
                    List.of(left.canAcceptEmptyTokenSequence, right.canAcceptEmptyTokenSequence),
                    () -> left.canAcceptEmptyTokenSequence.get().flatMap(
                        leftValue -> right.canAcceptEmptyTokenSequence.get().map(
                            rightValue -> rightValue.prepend(leftValue)
                        )
                    )
                ),
                new InductiveProperty.Rule<>(
                    List.of(left.canAcceptSomeTokenSequence, right.canAcceptSomeTokenSequence),
                    () -> left.canAcceptSomeTokenSequence.get()
                        && right.canAcceptSomeTokenSequence.get()
                ),
                new InductiveProperty.Rule<>(
                    List.of(
                        left.shouldNotFollow,
                        left.canAcceptSomeTokenSequence,
                        right.canAcceptEmptyTokenSequence,
                        right.shouldNotFollow,
                    ),
                    () -> {
                        final Set<ShouldNotFollowEntry<V, K>> entries = new HashSet<>();
                        if (right.canAcceptEmptyTokenSequence.get().isPresent()) {
                            entries.addAll(left.shouldNotFollow.get());
                        }
                        if (left.canAcceptSomeTokenSequence.get()) {
                            entries.addAll(right.shouldNotFollow.get());
                        }
                        return entries;
                    }
                ),
                new InductiveProperty.Rule<Set<Conflict>>(
                    List.of(
                        left.shouldNotFollow,
                        left.conflicts,
                        right.acceptableKinds,
                        right.conflicts
                    ),
                    () -> {
                        final Set<Conflict> conflicts = Stream.concat(
                            left.conflicts.get().stream(),
                            right.conflicts.get().stream()
                        ).collect(Collectors.toCollection(HashSet::new));
    
                        for (final var shouldNotFollowEntry : left.shouldNotFollow.get()) {
                            final Set<K> followAmbiguities = new HashSet<>(shouldNotFollowEntry.disallowedKinds);
                            followAmbiguities.retainAll(right.acceptableKinds.get());
                            if (!followAmbiguities.isEmpty()) {
                                conflicts.add(new FollowConflict(shouldNotFollowEntry.source, this, followAmbiguities));
                            }
                        }
                        
                        return conflicts;
                    }
                )
            );
            
            this.left = left;
            this.right = right;
        }
        
        @Override
        protected void realize() {
            left.realize();
            right.realize();
        }
        
        @Override
        protected ValidSyntax toValidSyntaxUnchecked() {
            return new ValidSyntax.Disjunction(
                left.toValidSyntaxUnchecked(),
                right.toValidSyntaxUnchecked(),
                acceptableKinds.get(),
                canAcceptEmptyTokenSequence.get(),
                canAcceptSomeTokenSequence.get(),
                shouldNotFollow.get()
            );
        }
    }
    
    public static class Transform<V extends Value, K extends TokenKind> implements Syntax<V, K> {
        public final UnaryOperator<V> transformation;
        public final Syntax<V, K> syntax;

        public Transform(UnaryOperator<V> transformation, Syntax<V, K> syntax) {
            super(
                syntax.acceptableKinds,
                new InductiveProperty.Rule<>(
                    List.of(syntax.canAcceptEmptyTokenSequence),
                    () -> syntax.canAcceptEmptyTokenSequence.get().map(transformation)
                ),
                syntax.canAcceptSomeTokenSequence,
                syntax.shouldNotFollow,
                syntax.conflicts
            );
            this.transformation = transformation;
            this.syntax = syntax;
        }
        
        @Override
        protected void realize() {
            syntax.realize();
        }
        
        @Override
        protected ValidSyntax toValidSyntaxUnchecked() {
            return new ValidSyntax.Disjunction(
                transformation,
                syntax.toValidSyntaxUnchecked(),
                canAcceptEmptyTokenSequence.get()
            );
        }
    }

    /**
     * Used to create recursive syntaxes.
     */
    public static class Deferred<V extends Value, K extends TokenKind> extends Syntax<V, K> {
        private final Supplier<Syntax<V, K>> syntaxGetter;
        private Optional<Syntax<V, K>> realizedSyntax;
        
        private final InductiveProperty.Deferred<Set<K>> deferredAcceptableKinds
            = new InductiveProperty.Deferred<>(Collections.emptySet()),
        private final InductiveProperty.Deferred<Optional<V>> deferredCanAcceptEmptyTokenSequence
            = new InductiveProperty.Deferred<>(Optional.empty()),
        private final InductiveProperty.Deferred<Boolean> deferredCanAcceptSomeTokenSequence
            = new InductiveProperty.Deferred<>(false),
        private final InductiveProperty.Deferred<Set<ShouldNotFollowEntry>> deferredShouldNotFollow
            = new InductiveProperty.Deferred<>(Collections.emptySet()),
        private final InductiveProperty.Deferred<Set<Conflict> deferredConflicts
            = new InductiveProperty.Deferred<>(Collections.emptySet())

        public Deferred(Supplier<Syntax<V, K>> syntaxGetter) {
            super(
                deferredAcceptableKinds,
                deferredCanAcceptEmptyTokenSequence,
                deferredCanAcceptSomeTokenSequence,
                deferredShouldNotFollow,
                deferredConflicts
            );
            this.syntaxGetter = syntaxGetter;
        }

        @Override
        protected void realize() {
            realizedSyntax = Optional.of(syntaxGetter.get());
            deferredAcceptableKinds.realize(realizedSyntax.acceptableKinds);
            deferredCanAcceptEmptyTokenSequence.realize(realizedSyntax.canAcceptEmptyTokenSequence);
            deferredCanAcceptSomeTokenSequence.realize(realizedSyntax.canAcceptSomeTokenSequence);
            deferredShouldNotFollow.realize(realizedSyntax.shouldNotFollow);
            deferredConflicts.realize(realizedSyntax.conflicts);
        }
        
        @Override
        protected ValidSyntax toValidSyntaxUnchecked() {
            return realizedSyntax.get().toValidSyntaxUnchecked();
        }

        // Do we still need this??
        // @Override
        // public Focus.Context focus(K kind, Focus.Context context) {
        //     return realizedSyntax.get().focus(kind, context);
        // }
    }
}