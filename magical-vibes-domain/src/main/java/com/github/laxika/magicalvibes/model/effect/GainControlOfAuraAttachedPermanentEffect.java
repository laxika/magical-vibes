package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains control of the permanent captured by an Aura-attachment trigger for as long as that Aura
 * remains attached to it.
 *
 * <p>The triggering Aura is the stack entry's triggering permanent ("that Aura") and the enchanted
 * permanent is stored as its target context. The effect itself declares no target because the
 * ability that creates it is non-targeting.</p>
 */
public record GainControlOfAuraAttachedPermanentEffect() implements CardEffect {
}
