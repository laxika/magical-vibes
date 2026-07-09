package com.github.laxika.magicalvibes.model.effect;

/**
 * ETB effect: the controller exiles the top N cards of their own library,
 * tracked as "exiled with" the source permanent (e.g. Colfenor's Plans).
 *
 * <p>Unlike {@link EachPlayerExilesTopCardsToSourceEffect} (which affects every player),
 * this only exiles the controller's library. Pair with
 * {@link AllowCastFromCardsExiledWithSourceEffect} to let the controller play those cards.
 */
public record ExileTopCardsToSourceEffect(int count) implements CardEffect {
}
