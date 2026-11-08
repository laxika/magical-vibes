package com.github.laxika.magicalvibes.model.effect;

/**
 * "When this becomes untapped, return that exiled card" (Tawnos's Coffin): returns the cards the source's linked exile
 * tracked as it became untapped, the stack entry's triggering cards, through their source-linked exile-return entries.
 */
public record ReturnCardsExiledWithSourceOnUntapEffect() implements CardEffect {
}
