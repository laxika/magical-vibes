package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * A graveyard-activated ability suspended while the player chooses cards for an activation cost.
 * This covers both exact-N graveyard-exile costs and discard costs such as Sunscourge Champion's
 * Eternalize—{2}{W}{W}, Discard a card and Haunted Dead's Discard two cards.
 * The graveyard analogue of {@link PendingAbilityActivation}: since the source card may already have
 * left the graveyard for exile, the resolved {@code card} and {@code ability} are held directly rather
 * than by graveyard index.
 *
 * @param remainingDiscards how many discard choices are still owed (decrements after each pick)
 * @param discardCostRequiredName the name every remaining discard must match for a same-name discard
 *                                cost, fixed by the first card chosen; {@code null} otherwise
 * @param graveyardTargetIds graveyard targets chosen before the cost interaction was suspended
 */
public record PendingGraveyardAbilityActivation(UUID playerId, Card card, ActivatedAbility ability,
                                                int xValue, UUID targetId, int remainingDiscards,
                                                String discardCostRequiredName,
                                                List<UUID> graveyardTargetIds) {

    public PendingGraveyardAbilityActivation {
        graveyardTargetIds = graveyardTargetIds != null ? List.copyOf(graveyardTargetIds) : null;
    }

    public PendingGraveyardAbilityActivation(UUID playerId, Card card, ActivatedAbility ability,
                                             int xValue, UUID targetId, int remainingDiscards) {
        this(playerId, card, ability, xValue, targetId, remainingDiscards, null, null);
    }

    public PendingGraveyardAbilityActivation(UUID playerId, Card card, ActivatedAbility ability,
                                             int xValue, UUID targetId, int remainingDiscards,
                                             String discardCostRequiredName) {
        this(playerId, card, ability, xValue, targetId, remainingDiscards,
                discardCostRequiredName, null);
    }
}
