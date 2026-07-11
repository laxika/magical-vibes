package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. The controller may play that card
 * without paying its mana cost.
 *
 * <p>When {@code exileIfNotPlayed} is {@code true}, a card that isn't (or can't be) played is
 * exiled (Djinn of Wishes). When {@code false}, such a card simply stays on top of the library
 * (Leaf-Crowned Elder's Kinship reveal, where the card has already been revealed).</p>
 */
public record RevealTopCardMayPlayFreeOrExileEffect(boolean exileIfNotPlayed) implements CardEffect {
}
