package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If a source would deal damage to you, prevent N of that damage." (Urza's Armor)
 *
 * <p>When {@code creatureSourcesOnly} is set the restriction narrows to creature sources only,
 * covering "If a creature would deal damage to you, prevent N of that damage." (Orbs of Warding)
 *
 * <p>Modeled per damage event on the controller of the permanent carrying this effect: up to
 * {@code amount} is prevented from each source that would deal damage to them. Applies to both
 * combat and noncombat damage. Applied in {@code DamageSupport.dealDamageToPlayer} (noncombat)
 * and {@code CombatDamageService.accumulatePlayerDamage} (combat, per attacker) via
 * {@code DamagePreventionService.applyControllerFixedPerSourceDamagePrevention}.
 */
public record PreventFixedDamagePerSourceToControllerEffect(int amount, boolean creatureSourcesOnly) implements CardEffect {

    /** "If a source would deal damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromAnySource(int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, false);
    }

    /** "If a creature would deal damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromCreatures(int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, true);
    }
}
