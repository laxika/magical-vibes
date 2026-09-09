package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a non-targeting attack trigger effect that acts on the other creature attacking in
 * the same combat. The combat trigger collector stores that creature as fixed event context.
 */
public interface OtherAttackingCreatureReferenceEffect extends CardEffect {
}
