package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "Whenever this creature deals damage to a creature, exile that creature".
 * Registered in {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE}
 * and self-scoped: it only fires when the permanent holding it is the damage source.
 *
 * <p>Expanded at trigger-collection time into an {@link ExileTargetPermanentEffect} entry whose
 * non-targeting target is the damaged creature, so it is never resolved directly.
 */
public record ExileDamagedCreatureEffect() implements CardEffect {
}
