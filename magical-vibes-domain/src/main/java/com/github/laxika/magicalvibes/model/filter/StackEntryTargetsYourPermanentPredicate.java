package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells that target at least one permanent controlled by the counterspell's controller.
 * Used by Turn Aside and similar cards ("Counter target spell that targets a permanent you control").
 */
public record StackEntryTargetsYourPermanentPredicate() implements StackEntryPredicate {
}
