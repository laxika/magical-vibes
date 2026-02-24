package com.github.laxika.magicalvibes.model.effect;

/**
 * Put a -1/-1 counter on target creature.
 */
public record PutMinusOneMinusOneCounterOnTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
