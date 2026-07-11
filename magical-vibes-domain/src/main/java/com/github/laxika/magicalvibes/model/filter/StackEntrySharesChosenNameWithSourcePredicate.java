package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell on the stack whose card name equals the chosen name recorded on the source
 * permanent (set via a "choose a card name" ETB effect). Used for "counter target spell with
 * the chosen name" (Declaration of Naught). Evaluation requires the source permanent, so this
 * predicate matches nothing when evaluated without a source context.
 */
public record StackEntrySharesChosenNameWithSourcePredicate() implements StackEntryPredicate {
}
