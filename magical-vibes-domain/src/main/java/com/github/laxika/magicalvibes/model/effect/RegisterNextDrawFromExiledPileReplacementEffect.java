package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Registers a one-shot replacement effect (CR 614.1a) of the controller's next draw this turn: "instead put
 * the top card of the exiled pile into its owner's hand." The pile is the set of cards exiled with
 * the source permanent (see {@link SearchLibraryForCardsToExileFaceDownPileEffect}).
 *
 * <p>Each activation resolves into a floating effect on the drawing player whose payload is this record with the
 * pile's source permanent, so repeated activations each replace one later draw. It ends at cleanup ("this turn") or
 * as it replaces a draw ({@code DrawService}). Used by Mangara's Tome.
 *
 * @param pileSourceId the permanent whose exiled pile it takes from; {@code null} on the card's own copy
 */
public record RegisterNextDrawFromExiledPileReplacementEffect(UUID pileSourceId) implements CardEffect {

    public RegisterNextDrawFromExiledPileReplacementEffect() {
        this(null);
    }
}
