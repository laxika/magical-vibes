package com.github.laxika.magicalvibes.model.effect;

/** Puts the same number of each counter kind on the creature that caused the enter trigger. */
public record PutCountersOnEnteringCreatureEqualToSourceCountersEffect() implements CardEffect {

    @Override
    public boolean usesEnteringPermanentReference() {
        return true;
    }
}
