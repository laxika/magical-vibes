package com.github.laxika.magicalvibes.model.effect;

/**
 * As the source creature enters, its controller may exile up to X creature cards from their
 * graveyard and it enters with a fixed number of +1/+1 counters for each card exiled this way.
 */
public record ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect(int countersPerCard)
        implements ReplacementEffect {
}
