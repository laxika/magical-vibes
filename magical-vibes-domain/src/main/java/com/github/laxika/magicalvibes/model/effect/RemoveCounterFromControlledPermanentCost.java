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
 * <p>Used both as the payable side of a {@link ForcedCostOrElseEffect} and as an activated-ability
 * cost. When used for an activated ability, the controller chooses one of their counter-bearing
 * permanents through the standard permanent-choice cost flow.
 */
public record RemoveCounterFromControlledPermanentCost() implements CostEffect {
}
