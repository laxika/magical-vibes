package com.github.laxika.magicalvibes.model.effect;

/**
 * Corpsejack Menace: "If one or more +1/+1 counters would be put on a creature you control, twice
 * that many +1/+1 counters are put on it instead."
 *
 * <p>A STATIC replacement-effect marker placed on the source permanent itself. Every site that puts
 * +1/+1 counters on a creature routes the count through
 * {@code GameQueryService.doublePlusOnePlusOneCounters}, which counts how many permanents carrying
 * this marker the affected creature's controller controls and multiplies the count by two for each
 * (so two copies multiply by four, three by eight, and so on). Counting the markers — rather than
 * granting a per-creature flag — lets multiple copies stack correctly without looping.
 */
public record DoublePlusOnePlusOneCountersEffect() implements PlusOnePlusOneCountersReplacementEffect {

    @Override
    public int replace(int count) {
        return count > 0 ? count * 2 : count;
    }
}
