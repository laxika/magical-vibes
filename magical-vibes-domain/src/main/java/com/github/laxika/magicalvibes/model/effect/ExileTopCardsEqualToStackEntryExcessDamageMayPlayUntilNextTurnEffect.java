package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile cards from the top of your library equal to the excess damage stored on the resolving stack
 * entry. Grants play permission for those cards until the end of your next turn.
 */
public record ExileTopCardsEqualToStackEntryExcessDamageMayPlayUntilNextTurnEffect() implements CardEffect {
}
