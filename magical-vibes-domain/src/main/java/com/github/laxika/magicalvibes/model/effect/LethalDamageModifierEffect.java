package com.github.laxika.magicalvibes.model.effect;

/** Capability for a static effect that changes how lethal damage is determined. */
public interface LethalDamageModifierEffect extends CardEffect {

    default boolean usesPowerForLethalDamage() {
        return false;
    }
}
