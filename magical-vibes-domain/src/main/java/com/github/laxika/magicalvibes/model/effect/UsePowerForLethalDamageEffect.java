package com.github.laxika.magicalvibes.model.effect;

/** Uses a creature's effective power instead of its toughness to determine lethal damage. */
public record UsePowerForLethalDamageEffect() implements LethalDamageModifierEffect {

    @Override
    public boolean usesPowerForLethalDamage() {
        return true;
    }
}
