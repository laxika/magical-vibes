package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all combat damage that would be dealt to this creature." (e.g. Seraph of the Sword)
 * <p>
 * Always-on marker read off the source permanent's own {@code EffectSlot.STATIC} effects in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 * Noncombat damage is unaffected, and unlike
 * {@link PreventAllCombatDamageToAndBySelfEffect} the creature still deals its own combat damage.
 */
public record PreventAllCombatDamageToSelfEffect() implements CardEffect {
}
