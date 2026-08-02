package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile all cards from your hand face down. If you do, put all other cards you own exiled with this
 * permanent into your hand." (Duplicity's upkeep trigger — wrap in {@link MayEffect} for the "you
 * may").
 *
 * <p>The whole hand is exiled face down and tracked with the source permanent
 * ({@code GameData.exiledCards} / {@code sourcePermanentId}); "all other cards" means everything the
 * controller owns that was already exiled with the source before this resolution, so the two piles
 * swap rather than merge. An empty hand still counts as having exiled all cards from it, so the
 * return half happens either way.
 *
 * <p>Companion to {@link ExileTopCardsToSourceEffect}, which seeds the exiled pile, and to
 * {@link ExileCardFromHandFaceDownWithSourceEffect} / {@link PutCardExiledWithSourceIntoHandEffect},
 * which move one card at a time.
 */
public record ExileHandFaceDownThenReturnCardsExiledWithSourceEffect() implements CardEffect {
}
