package com.github.laxika.magicalvibes.model.effect;

/**
 * Spirit of Resilience's graveyard-departure trigger: a +1/+1 counter on the source, then it may become a copy of an
 * artifact or creature card from among the cards that left the graveyard until end of turn. Those cards are the stack
 * entry's triggering cards, set by the leaves-the-graveyard collector.
 */
public record PutCounterThenMayBecomeCopyOfCardsEffect() implements CardEffect {
}
