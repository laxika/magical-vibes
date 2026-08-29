package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * ETB choice (Argothian Wurm): each player may sacrifice a land; if they do, put the source on
 * top of its owner's library. Players still receive their choices after an earlier player accepts.
 *
 * @param remainingPlayerIds players still to receive the choice
 * @param abilityControllerId controller of the triggered ability
 * @param sourcePermanentId the permanent to put on top when a land is sacrificed
 */
public record AnyPlayerMaySacrificeLandPutSourceOnTopEffect(
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId,
        UUID sourcePermanentId
) implements CardEffect {

    public AnyPlayerMaySacrificeLandPutSourceOnTopEffect() {
        this(null, null, null);
    }
}
