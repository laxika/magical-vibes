package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Conjures a non-token duplicate of a permanent whose id was captured by a preceding non-targeting
 * choice, onto its controller's battlefield. {@code power} and {@code toughness}, when present, set
 * the duplicate's base power and toughness on the new card ("the duplicate perpetually has base
 * power and toughness 1/1"); {@code null} keeps the copied values.
 */
public record ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(
        UUID permanentId,
        Integer power,
        Integer toughness
) implements CardEffect {
}
