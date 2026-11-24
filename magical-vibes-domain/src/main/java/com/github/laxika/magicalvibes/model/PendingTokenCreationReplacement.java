package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * The token-creation event currently waiting for a replacement choice: Esix's copies of a chosen creature
 * ({@code copyChosenCreature}), else copies of the permanent {@code replacementPermanentId} is attached to.
 */
public record PendingTokenCreationReplacement(
        UUID replacementPermanentId,
        int amount,
        int power,
        int toughness,
        boolean copyChosenCreature
) {
}
