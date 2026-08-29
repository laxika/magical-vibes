package com.github.laxika.magicalvibes.model.effect;

/**
 * Discards the controller's hand, draws that many cards, then offers one discarded artifact or
 * creature card at each of mana values one, two, and three to return to the battlefield.
 */
public record DiscardHandThenDrawAndReturnArtifactOrCreatureCardsEffect() implements CardEffect {
}
