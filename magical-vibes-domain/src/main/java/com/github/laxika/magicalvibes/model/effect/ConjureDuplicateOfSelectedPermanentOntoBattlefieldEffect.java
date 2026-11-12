package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures a non-token duplicate of the permanent an earlier step of the resolution picked (the first of the
 * resolving entry's picks), onto its controller's battlefield. {@code power} and {@code toughness}, when present, set
 * the duplicate's base power and toughness on the new card ("the duplicate perpetually has base
 * power and toughness 1/1"); {@code null} keeps the copied values.
 */
public record ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(
        Integer power,
        Integer toughness
) implements CardEffect {
}
