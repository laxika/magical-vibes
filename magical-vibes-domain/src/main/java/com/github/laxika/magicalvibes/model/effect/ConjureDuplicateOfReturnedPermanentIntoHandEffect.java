package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures a duplicate of the permanent returned by the preceding bounce effect into the
 * resolving player's hand. The duplicate also carries the static permission to spend mana as
 * though it were mana of any color to cast that card.
 */
public record ConjureDuplicateOfReturnedPermanentIntoHandEffect() implements CardEffect {
}
