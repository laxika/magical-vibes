package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * The token-creation event currently waiting for a replacement choice: Esix's copies of a chosen creature
 * ({@code copyChosenCreature}), else copies of the permanent {@code replacementPermanentId} is attached to.
 * {@code creatorId} is the player who would create the tokens and was offered the replacement.
 */
public record PendingTokenCreationReplacement(
        UUID replacementPermanentId,
        int amount,
        int power,
        int toughness,
        boolean copyChosenCreature,
        UUID creatorId
) {
}
