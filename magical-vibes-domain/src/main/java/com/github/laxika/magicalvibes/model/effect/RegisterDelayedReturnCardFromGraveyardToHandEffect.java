package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * When resolved, registers a delayed trigger that fires at the beginning of the next end step,
 * returning the specified card from its owner's graveyard to their hand (if it is still there).
 *
 * <p>Used by Tiana, Ship's Caretaker: "Whenever an Aura or Equipment you control is put into
 * a graveyard from the battlefield, you may return that card to its owner's hand at the
 * beginning of the next end step." The collector bakes the dying card's id into {@code cardId}.
 *
 * <p>Also used by The Locust God on {@code ON_DEATH} with {@code cardId == null}: the handler
 * falls back to the stack-entry source card.
 *
 * @param cardId the UUID of the card to return, or {@code null} to use the resolving source card
 */
public record RegisterDelayedReturnCardFromGraveyardToHandEffect(UUID cardId) implements CardEffect {
}
