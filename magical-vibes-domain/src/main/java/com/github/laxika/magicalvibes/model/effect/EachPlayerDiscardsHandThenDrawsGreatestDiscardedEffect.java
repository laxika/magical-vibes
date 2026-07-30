package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards their hand, then draws cards equal to the greatest number of cards a player
 * discarded this way (Windfall, Jace's Archivist). All discards happen first in APNAP order, so the
 * shared draw count is the largest hand discarded; every player then draws that many in APNAP order.
 * Differs from {@link EachPlayerDiscardsHandThenDrawsThatManyEffect}, where each player draws only
 * as many cards as they personally discarded.
 */
public record EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect() implements CardEffect {
}
