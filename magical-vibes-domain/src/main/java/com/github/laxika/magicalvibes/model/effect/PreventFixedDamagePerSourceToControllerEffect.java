package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If a source would deal damage to you, prevent N of that damage." (Urza's Armor)
 *
 * <p>Modeled per damage event on the controller of the permanent carrying this effect: up to
 * {@code amount} is prevented from each source that would deal damage to them. Applies to both
 * combat and noncombat damage. Applied in {@code DamageSupport.dealDamageToPlayer} (noncombat)
 * and {@code CombatDamageService.accumulatePlayerDamage} (combat, per attacker) via
 * {@code DamagePreventionService.applyControllerFixedPerSourceDamagePrevention}.
 */
public record PreventFixedDamagePerSourceToControllerEffect(int amount) implements CardEffect {
}
