package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect stored in an {@code Emblem}'s staticEffects list (Tamiyo, the Moon Sage's emblem).
 * Indicates that whenever a card is put into the emblem controller's graveyard from anywhere, a
 * triggered ability fires that lets them return that card to their hand.
 */
public record ReturnCardPutIntoGraveyardToHandEffect() implements CardEffect {
}
