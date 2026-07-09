package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If noncombat damage would be dealt to you, prevent that damage.
 * You gain life equal to the damage prevented this way." (e.g. Purity)
 *
 * <p>Applied per noncombat damage event in {@code DamageSupport.dealDamageToPlayer} on the
 * controller of the permanent carrying this effect. Combat damage is unaffected.
 */
public record PreventNoncombatDamageToControllerAndGainLifeEffect() implements CardEffect {
}
