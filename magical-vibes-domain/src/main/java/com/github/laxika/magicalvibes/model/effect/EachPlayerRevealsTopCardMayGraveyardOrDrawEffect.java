package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player reveals the top card of their library. The controller may put those cards into
 * their owners' graveyards; otherwise, each player draws a card.
 *
 * <p>The cards remain on top of their libraries while the controller makes the choice, so the
 * draw branch draws the cards that were revealed.
 */
public record EachPlayerRevealsTopCardMayGraveyardOrDrawEffect() implements CardEffect {
}
