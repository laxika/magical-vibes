package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks a spell that should be returned from graveyard to hand if a card of the required type
 * is discarded during the current discard interaction.
 *
 * @param card         the spell card to potentially return
 * @param controllerId the owner/controller of the spell
 * @param requiredType the card type that triggers the return
 */
public record PendingReturnToHandOnDiscardType(Card card, UUID controllerId, CardType requiredType) {
}
