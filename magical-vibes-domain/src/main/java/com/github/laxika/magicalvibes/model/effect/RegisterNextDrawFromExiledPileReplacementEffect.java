package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot, turn-scoped delayed replacement of the controller's next draw: "instead put
 * the top card of the exiled pile into its owner's hand." The pile is the set of cards exiled with
 * the source permanent (see {@link SearchLibraryForCardsToExileFaceDownPileEffect}).
 *
 * <p>Stored per drawing player in {@code GameData.pendingNextDrawFromExiledPile} as a queue of
 * source permanent ids — repeated activations each replace one later draw — and consumed in
 * {@code DrawService.resolveDrawCard}. Cleared at end-of-turn cleanup ("this turn"). Used by
 * Mangara's Tome.
 */
public record RegisterNextDrawFromExiledPileReplacementEffect() implements CardEffect {
}
