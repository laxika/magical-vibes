package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. The controller may play that card
 * without paying its mana cost. If they don't (or can't), exile it.
 * Used by Djinn of Wishes.
 */
public record RevealTopCardMayPlayFreeOrExileEffect() implements CardEffect {
}
