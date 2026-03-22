package com.github.laxika.magicalvibes.model.effect;

/**
 * Put one or more +1/+1 counters on each creature you control.
 *
 * @param count the number of +1/+1 counters to put on each creature (default 1)
 */
public record PutPlusOnePlusOneCounterOnEachOwnCreatureEffect(int count) implements CardEffect {

    /** Convenience constructor — puts exactly one +1/+1 counter on each creature you control. */
    public PutPlusOnePlusOneCounterOnEachOwnCreatureEffect() {
        this(1);
    }
}
