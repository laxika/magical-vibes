package com.github.laxika.magicalvibes.model.filter;

/**
 * A {@link StackEntryPredicate} that matches any spell or ability on the stack unconditionally.
 * The stack-entry counterpart of {@link PermanentTruePredicate} / {@link CardTruePredicate}, so
 * "any spell on the stack" is spelled as a predicate rather than as a {@code null} inner
 * predicate — {@code null} means "matches nothing" for permanents and "matches everything" for
 * cards, and a target predicate must not depend on which of those a reader assumes.
 */
public record StackEntryTruePredicate() implements StackEntryPredicate {
}
