package com.github.laxika.magicalvibes.model.effect;

/**
 * Put one or more -1/-1 counters on target creature.
 *
 * @param count number of -1/-1 counters to place (default 1)
 */
public record PutMinusOneMinusOneCounterOnTargetCreatureEffect(int count) implements CardEffect {

    public PutMinusOneMinusOneCounterOnTargetCreatureEffect() {
        this(1);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
