package com.github.laxika.magicalvibes.model.filter;

/** Matches a creature that was attacking alone or was the only creature blocking. */
public record PermanentIsAttackingOrBlockingAlonePredicate() implements PermanentPredicate {
}
