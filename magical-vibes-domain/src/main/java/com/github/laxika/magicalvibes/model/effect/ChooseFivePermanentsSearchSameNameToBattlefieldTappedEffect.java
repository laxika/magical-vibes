package com.github.laxika.magicalvibes.model.effect;

/**
 * Choose five permanents you control. For each of those permanents, you may search your library
 * for a card with the same name as that permanent. Put those cards onto the battlefield tapped,
 * then shuffle.
 *
 * <p>The controller chooses up to five different permanents they control (fewer if they control
 * fewer) via a
 * {@link com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.ChooseFivePermanentsSearchSameNameToBattlefieldTapped}
 * multi-permanent choice; the completion queues one optional single-name library search per chosen
 * permanent, each putting a found card onto the battlefield tapped, then shuffles. Used by Clarion
 * Ultimatum.
 */
public record ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffect() implements CardEffect {
}
