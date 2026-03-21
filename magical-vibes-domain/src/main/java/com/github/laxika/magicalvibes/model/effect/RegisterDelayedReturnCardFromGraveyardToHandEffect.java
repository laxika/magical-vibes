package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * When resolved, registers a delayed trigger that fires at the beginning of the next end step,
 * returning the specified card from its owner's graveyard to their hand (if it is still there).
 *
 * <p>Used by Tiana, Ship's Caretaker: "Whenever an Aura or Equipment you control is put into
 * a graveyard from the battlefield, you may return that card to its owner's hand at the
 * beginning of the next end step."
 *
 * @param cardId the UUID of the card to return from the graveyard at the next end step
 */
public record RegisterDelayedReturnCardFromGraveyardToHandEffect(UUID cardId) implements CardEffect {
}
