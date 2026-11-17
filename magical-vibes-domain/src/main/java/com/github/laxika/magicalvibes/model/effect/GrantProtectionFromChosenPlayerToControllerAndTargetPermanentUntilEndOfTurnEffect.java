package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Gives the ability's controller and its target permanent protection from the remembered player until end of turn.
 *
 * @param playerId the player protected from; {@code null} on the card's effect, which names no player until it
 *                 resolves. The floating effect the resolution makes on the controller carries it.
 */
public record GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffect(UUID playerId)
        implements CardEffect {

    public GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
