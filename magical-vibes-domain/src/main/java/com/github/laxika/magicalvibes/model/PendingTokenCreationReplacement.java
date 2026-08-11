package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * The token-creation event currently waiting for a Mirrormind Crown choice.
 */
public record PendingTokenCreationReplacement(
        UUID crownPermanentId,
        int amount,
        int power,
        int toughness
) {
}
