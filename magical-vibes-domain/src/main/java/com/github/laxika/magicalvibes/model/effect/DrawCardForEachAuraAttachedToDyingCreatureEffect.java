package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "whenever an enchanted creature dies, draw a card for each Aura you controlled that
 * was attached to it" (Hateful Eidolon).
 *
 * <p>Placed on {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_ANY_CREATURE_DIES}. The
 * death trigger collector counts the qualifying Auras at the time of death and resolves into a
 * regular {@link DrawCardEffect}.
 */
public record DrawCardForEachAuraAttachedToDyingCreatureEffect() implements CardEffect {
}
