package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a haunted permanent: one a card in exile is haunting ("target haunted creature", "for as long as it remains
 * haunted", Kaya, Ghost Haunter's emblems). It reads {@code GameData.hauntingCardToPermanentId}.
 */
public record PermanentIsHauntedPredicate() implements PermanentPredicate {
}
