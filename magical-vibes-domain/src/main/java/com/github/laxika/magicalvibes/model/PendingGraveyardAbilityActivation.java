package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A graveyard-activated ability that has paid its mana / exile costs and is now suspended waiting for
 * the player to choose which card to discard for its "Discard a card" activation cost
 * (e.g. Sunscourge Champion's Eternalize—{2}{W}{W}, Discard a card). The graveyard analogue of
 * {@link PendingAbilityActivation}: since the source card has already left the graveyard for exile,
 * the resolved {@code card} and {@code ability} are held directly rather than by graveyard index.
 */
public record PendingGraveyardAbilityActivation(UUID playerId, Card card, ActivatedAbility ability,
                                                int xValue, UUID targetId) {
}
