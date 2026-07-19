package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. If it's a land card, put it onto the
 * battlefield. Otherwise, put it into the controller's hand.
 *
 * <p>Used by Skyward Eye Prophets ({@code {T}} activated ability).
 */
public record RevealTopCardLandToBattlefieldElseToHandEffect() implements CardEffect {
}
