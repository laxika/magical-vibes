package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an attacking permanent whose attack target is the same as the source permanent's attack
 * target.
 */
public record PermanentIsAttackingSameTargetAsSourcePredicate() implements PermanentPredicate {
}
