package com.github.laxika.magicalvibes.model.effect;

/**
 * Additional cost to cast a spell: return a creature you control to its owner's hand
 * (e.g. Familiar's Ruse). Placed in the {@code SPELL} slot. The creature to return is
 * supplied via {@code PlayCardRequest.sacrificePermanentId} and paid in
 * {@code SpellCastingService}. The spell is unplayable if you control no creature.
 */
public record ReturnCreatureToHandCost() implements CostEffect {
}
