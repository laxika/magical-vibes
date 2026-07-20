package com.github.laxika.magicalvibes.model.effect;

/**
 * Vizier of Remedies: "If one or more -1/-1 counters would be put on a creature you control, that
 * many -1/-1 counters minus one are put on it instead."
 *
 * <p>A STATIC replacement-effect marker placed on the source permanent itself (not granted to
 * creatures, unlike {@link CantHaveMinusOneMinusOneCountersEffect}). Every site that puts -1/-1
 * counters on a creature routes the count through
 * {@code GameQueryService.reduceMinusOneMinusOneCounters}, which counts how many permanents carrying
 * this marker the affected creature's controller controls and subtracts one per marker (floored at
 * zero). Counting the markers — rather than granting a per-creature flag — lets multiple copies stack
 * correctly (two Viziers reduce by two).
 */
public record ReduceMinusOneMinusOneCountersEffect() implements CardEffect {
}
