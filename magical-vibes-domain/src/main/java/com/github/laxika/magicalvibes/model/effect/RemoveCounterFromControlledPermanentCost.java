package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that removes a single counter of any kind from any permanent the payer controls
 * ("unless you remove a counter from a permanent you control" — Chisei, Heart of Oceans).
 *
 * <p>Unlike {@link RemoveCounterFromControlledCreatureCost} this is not restricted to creatures
 * and not restricted to one counter type: any permanent carrying at least one counter is a legal
 * choice. When the chosen permanent carries several kinds of counters, the first kind present is
 * removed (same convention as
 * {@link MoveCounterFromTargetCreatureToTargetCreatureEffect}'s "a counter" mode).
 *
 * <p>Currently only used as the payable side of a {@link ForcedCostOrElseEffect}.
 */
public record RemoveCounterFromControlledPermanentCost() implements CostEffect {
}
