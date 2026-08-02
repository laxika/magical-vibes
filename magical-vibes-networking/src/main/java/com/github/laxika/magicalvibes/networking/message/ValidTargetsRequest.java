package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.UUID;

/**
 * @param graveyardCardIndex index into the requesting player's graveyard when the targets are being
 *                           chosen for a graveyard activated ability (Soul of Shandalar); mutually
 *                           exclusive with {@code cardIndex} / {@code permanentIndex}
 */
public record ValidTargetsRequest(
        Integer cardIndex,
        Integer permanentIndex,
        Integer abilityIndex,
        List<UUID> alreadySelectedIds,
        Integer xValue,
        Boolean kicked,
        Integer graveyardCardIndex
) {
}
