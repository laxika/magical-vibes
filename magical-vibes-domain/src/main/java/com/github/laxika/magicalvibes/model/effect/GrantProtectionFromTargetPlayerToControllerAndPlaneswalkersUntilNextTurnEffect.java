package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Grants the controller and planeswalkers they control protection from the target player until the controller's next
 * turn.
 *
 * @param playerId the player protected from; {@code null} on the card's effect, whose target is chosen as it is
 *                 cast. The floating effect the resolution makes on the controller carries it.
 */
public record GrantProtectionFromTargetPlayerToControllerAndPlaneswalkersUntilNextTurnEffect(UUID playerId)
        implements CardEffect {

    public GrantProtectionFromTargetPlayerToControllerAndPlaneswalkersUntilNextTurnEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
