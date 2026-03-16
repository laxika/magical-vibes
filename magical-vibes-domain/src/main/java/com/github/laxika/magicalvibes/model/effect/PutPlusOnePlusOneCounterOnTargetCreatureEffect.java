package com.github.laxika.magicalvibes.model.effect;

/**
 * Put one or more +1/+1 counters on target creature.
 *
 * @param count number of +1/+1 counters to place
 */
public record PutPlusOnePlusOneCounterOnTargetCreatureEffect(int count) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
