package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** The token-creation event currently waiting for a replacement choice. */
public record PendingTokenCreationReplacement(
        UUID replacementPermanentId,
        int amount,
        int power,
        int toughness,
        boolean copyEnchantedPermanent
) {
}
