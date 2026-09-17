package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents with at least the requested number of Auras attached to them. */
public record PermanentHasAtLeastAttachedAurasPredicate(int minimum) implements PermanentPredicate {
}
