package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If a source would deal damage to a player, you may prevent X of that damage,
 * where X is the number of Clerics you control." (Battletide Alchemist)
 *
 * <p>Modeled per damage event on the controller of the permanent carrying this effect: up to
 * X = (Clerics that player controls) is prevented from each source that would deal damage to
 * them, where the count is re-evaluated at the moment damage would be dealt. Applies to both
 * combat and noncombat damage. Restricted to the controller because the "you may" choice would
 * never be used to prevent damage dealt to an opponent. Applied in
 * {@code DamageSupport.dealDamageToPlayer} (noncombat) and
 * {@code CombatDamageService.accumulatePlayerDamage} (combat, per attacker).
 */
public record PreventDamageToControllerPerClericEffect() implements CardEffect {
}
