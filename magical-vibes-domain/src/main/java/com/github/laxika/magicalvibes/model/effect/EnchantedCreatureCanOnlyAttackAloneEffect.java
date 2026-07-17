package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura effect: the enchanted creature can only attack alone.
 * <p>
 * The enchanted creature can be declared as an attacker only if it is the
 * sole attacker that combat. If any other creature is also declared as an
 * attacker, the enchanted creature can't attack. Enforced at attacker
 * declaration time in {@code CombatAttackService}. Used by Errantry.
 */
public record EnchantedCreatureCanOnlyAttackAloneEffect() implements CardEffect {
}
