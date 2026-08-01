package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: "This creature can't be destroyed by lethal damage unless lethal damage dealt by a
 * single source is marked on it." (Ogre Enforcer).
 * <p>
 * Read off the permanent's own {@code EffectSlot.STATIC} effects in
 * {@code StateBasedActionService}. Requires per-source marked-damage tracking on
 * {@link com.github.laxika.magicalvibes.model.Permanent}. Zero-toughness SBAs and deathtouch from a
 * single source still destroy the creature; damage from multiple sources cannot be combined to kill
 * it. No layer handler — rules-modifying marker only.
 */
public record CantBeDestroyedByLethalDamageUnlessSingleSourceEffect() implements CardEffect {
}
