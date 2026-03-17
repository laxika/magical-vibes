package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that have the same name as the source permanent.
 * Used by Evil Twin's activated ability: "Destroy target creature with the same name as this creature."
 */
public record PermanentHasSameNameAsSourcePredicate() implements PermanentPredicate {
}
