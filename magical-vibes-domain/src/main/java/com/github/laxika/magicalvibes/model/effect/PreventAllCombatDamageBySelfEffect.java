package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all combat damage this creature would deal."
 *
 * <p>This is a by-side-only counterpart to {@link PreventAllCombatDamageToSelfEffect}; combat
 * damage dealt to the source is unaffected. The marker is read by the combat damage source query.
 */
public record PreventAllCombatDamageBySelfEffect() implements CardEffect {
}
