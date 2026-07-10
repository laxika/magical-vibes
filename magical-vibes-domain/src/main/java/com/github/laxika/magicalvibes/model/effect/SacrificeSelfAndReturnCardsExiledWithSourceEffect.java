package com.github.laxika.magicalvibes.model.effect;

/**
 * End-step trigger effect for Colfenor's Urn: "if three or more cards have been exiled
 * with this artifact, sacrifice it. If you do, return those cards to the battlefield
 * under their owner's control."
 * <p>
 * The intervening-if ("N or more cards exiled with the source") is checked when the
 * ability would trigger and again as it resolves (CR 603.4). When met, the source is
 * sacrificed and every card exiled with it returns to the battlefield.
 *
 * @param minCount the minimum number of cards that must be exiled with the source
 */
public record SacrificeSelfAndReturnCardsExiledWithSourceEffect(int minCount) implements CardEffect {
}
