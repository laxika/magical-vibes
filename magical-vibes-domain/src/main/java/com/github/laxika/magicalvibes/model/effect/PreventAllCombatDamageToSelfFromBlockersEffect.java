package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all combat damage that would be dealt to this creature by creatures
 * blocking it." (e.g. Armored Transport)
 * <p>
 * Always-on marker read off the source permanent's own {@code EffectSlot.STATIC} effects in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#isCombatDamageFromBlockerPrevented}.
 * Narrower than {@link PreventAllCombatDamageToSelfEffect}: only damage from creatures that are
 * blocking this creature is prevented, so combat damage this creature takes while blocking (or
 * from any other source) still applies, as does all noncombat damage.
 */
public record PreventAllCombatDamageToSelfFromBlockersEffect() implements CardEffect {
}
