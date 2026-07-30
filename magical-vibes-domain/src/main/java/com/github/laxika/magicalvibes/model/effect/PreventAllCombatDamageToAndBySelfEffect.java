package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all combat damage that would be dealt to and dealt by this creature." (e.g. Fog Bank)
 * <p>
 * Always-on marker read off the source permanent's own {@code EffectSlot.STATIC} effects. The
 * damage-taken side is checked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield};
 * the damage-dealt side in
 * {@code GameQueryService.isPreventedFromDealingDamage(GameData, Permanent, boolean)}. Noncombat
 * damage in either direction is unaffected.
 */
public record PreventAllCombatDamageToAndBySelfEffect() implements CardEffect {
}
