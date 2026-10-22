package com.github.laxika.magicalvibes.model.effect;

/**
 * Static self-effect: the source gains the watched keywords found on cards exiled with it.
 *
 * @param includesProtection whether the printed protection abilities of those cards are gained too
 *                           (Wretched Bonemass), which Urborg Scavengers' watched list does not include
 */
public record GainKeywordsOfCardsExiledWithSourceEffect(boolean includesProtection) implements CardEffect {
}
