package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If a source would deal damage to a planeswalker you control, prevent N of that
 * damage." (Djeru, With Eyes Open)
 *
 * <p>Keyed on the controller of the damaged planeswalker: up to {@code amount} is prevented from each
 * source that would deal damage to a planeswalker that player controls, combat and noncombat.
 * Multiple copies stack. Applied via
 * {@code DamagePreventionService.applyPlaneswalkerFixedPerSourceDamagePrevention}, hooked in the
 * planeswalker branches of {@code DamageSupport.dealCreatureDamage} /
 * {@code DamageSupport.resolveAnyTargetDamage} (noncombat) and
 * {@code CombatDamageService.accumulatePlayerDamage} (combat, per attacker).
 */
public record PreventFixedDamageToPlaneswalkersYouControlEffect(int amount)
        implements PlaneswalkerDamagePreventionEffect {
}
