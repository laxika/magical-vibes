package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top N cards of your library. For each of those cards, put that card
 * into your hand unless any opponent pays X life. Then exile the rest.
 * <p>
 * Used by Sword-Point Diplomacy (count=3, lifeCost=3).
 *
 * @param count    number of cards to reveal from the top of the library
 * @param lifeCost life an opponent must pay per card to prevent it from going to hand
 */
public record RevealTopCardsOpponentPaysLifeOrToHandEffect(
        int count,
        int lifeCost
) implements CardEffect {
}
