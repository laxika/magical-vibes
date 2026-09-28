package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an activated or triggered ability whose source currently has no colors. Abilities from
 * command-zone planar objects are also colorless sources; entries with no source do not match.
 */
public record StackEntrySourceIsColorlessPredicate() implements StackEntryPredicate {
}
