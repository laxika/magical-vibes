package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Conditional wrapper for aura effects: checks whether the enchanted creature has a specific
 * subtype. If it does, the {@code ifMatch} effect is active; otherwise, the {@code ifNotMatch}
 * effect is active.
 * <p>
 * Both inner effects can be any existing {@link CardEffect} — e.g., a
 * {@code StaticBoostEffect} for the match branch and an
 * {@code EnchantedCreatureCantAttackOrBlockEffect} for the non-match branch.
 * <p>
 * The static effect system delegates to the active inner effect's handler for bonuses.
 * The combat system unwraps this conditional when checking for lockdown effects like
 * "can't attack or block".
 */
public record EnchantedCreatureSubtypeConditionalEffect(
        CardSubtype subtype,
        CardEffect ifMatch,
        CardEffect ifNotMatch
) implements CardEffect {
}
