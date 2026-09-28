package com.github.laxika.magicalvibes.model.effect;

/**
 * If the source's power is at most {@code maxPower}, doubles its +1/+1 counters; otherwise it
 * removes all but one of those counters and its controller gains one life for each counter removed.
 * The power check and both branches resolve atomically against the same source state.
 */
public record DoublePlusOneCountersOrKeepOneAndGainLifeEffect(int maxPower)
        implements CardEffect, DoublingEffect {
}
