package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.UUID;

/**
 * @param graveyardCardIndex index into the requesting player's graveyard when the targets are being
 *                           chosen for a graveyard activated ability (Soul of Shandalar); mutually
 *                           exclusive with {@code cardIndex} / {@code permanentIndex}
 * @param handOwnerId        the player whose hand {@code cardIndex} indexes when it isn't the requester's own
 *                           (Sen Triplets), or {@code null}
 */
public record ValidTargetsRequest(
        Integer cardIndex,
        Integer permanentIndex,
        Integer abilityIndex,
        List<UUID> alreadySelectedIds,
        Integer xValue,
        Boolean kicked,
        Integer graveyardCardIndex, UUID planarObjectId, UUID commandCardId, UUID handOwnerId
) {
    public ValidTargetsRequest(Integer cardIndex, Integer permanentIndex, Integer abilityIndex,
            List<UUID> alreadySelectedIds, Integer xValue, Boolean kicked, Integer graveyardCardIndex, UUID planarObjectId,
            UUID commandCardId) {
        this(cardIndex, permanentIndex, abilityIndex, alreadySelectedIds, xValue, kicked, graveyardCardIndex, planarObjectId, commandCardId, null);
    }
    public ValidTargetsRequest(Integer cardIndex, Integer permanentIndex, Integer abilityIndex,
            List<UUID> alreadySelectedIds, Integer xValue, Boolean kicked, Integer graveyardCardIndex, UUID planarObjectId) {
        this(cardIndex, permanentIndex, abilityIndex, alreadySelectedIds, xValue, kicked, graveyardCardIndex, planarObjectId, null);
    }
    public ValidTargetsRequest(Integer cardIndex, Integer permanentIndex, Integer abilityIndex,
            List<UUID> alreadySelectedIds, Integer xValue, Boolean kicked, Integer graveyardCardIndex) {
        this(cardIndex, permanentIndex, abilityIndex, alreadySelectedIds, xValue, kicked, graveyardCardIndex, null);
    }
}
