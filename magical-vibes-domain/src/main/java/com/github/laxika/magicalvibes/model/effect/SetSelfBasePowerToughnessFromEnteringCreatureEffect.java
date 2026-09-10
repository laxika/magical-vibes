package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the source permanent's base power and toughness to the current power and toughness of
 * the creature that caused the enter trigger until end of turn.
 */
public record SetSelfBasePowerToughnessFromEnteringCreatureEffect() implements CardEffect {

    @Override
    public boolean usesEnteringPermanentReference() {
        return true;
    }
}
