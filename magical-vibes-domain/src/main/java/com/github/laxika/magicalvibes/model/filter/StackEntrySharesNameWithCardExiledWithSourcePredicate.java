package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell on the stack whose card name equals the name of a card exiled with the source
 * permanent. Evaluation requires the source permanent, so this predicate matches nothing when it
 * is evaluated without a source context.
 */
public record StackEntrySharesNameWithCardExiledWithSourcePredicate() implements StackEntryPredicate {
}
