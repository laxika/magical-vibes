package com.github.laxika.magicalvibes.model.effect;

/**
 * Seeks a card that shares a card type with one of the discarded cards (Diviner of Fates). The discarded cards are the
 * stack entry's triggering cards, set by the discard collector.
 */
public record SeekCardSharingCardTypeWithDiscardedCardsEffect() implements CardEffect {
}
