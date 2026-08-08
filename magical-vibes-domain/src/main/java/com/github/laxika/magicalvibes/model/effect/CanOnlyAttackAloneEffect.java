package com.github.laxika.magicalvibes.model.effect;

/**
 * Static self effect: this creature can only attack alone.
 * <p>
 * The creature can be declared as an attacker only if it is the sole attacker that combat.
 * Enforced at attacker declaration time in {@code CombatAttackService} — the self-scoped
 * counterpart of {@link EnchantedCreatureCanOnlyAttackAloneEffect} and the mirror of
 * {@link CantAttackOrBlockAloneEffect}. Used by Master of Cruelties.
 */
public record CanOnlyAttackAloneEffect() implements CardEffect {
}
