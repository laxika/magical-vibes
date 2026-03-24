package com.github.laxika.magicalvibes.model.effect;

/**
 * Draw N cards, then discard M cards at random. If the discarded cards share at least one card type,
 * put the specified number of +1/+1 counters on the source permanent.
 * <p>
 * Used by Rowdy Crew (XLN): "When Rowdy Crew enters the battlefield, draw three cards, then discard
 * two cards at random. If two cards that share a card type are discarded this way, put two +1/+1
 * counters on Rowdy Crew."
 *
 * @param drawAmount    number of cards to draw
 * @param discardAmount number of cards to discard at random
 * @param counterAmount number of +1/+1 counters to put on source if discarded cards share a type
 */
public record DrawAndRandomDiscardWithSharedTypeCountersEffect(
        int drawAmount,
        int discardAmount,
        int counterAmount
) implements CardEffect {
}
