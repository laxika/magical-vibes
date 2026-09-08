package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, the controller may play cards from the top of their library,
 * paying life equal to a spell's mana value instead of its mana cost.
 */
public record MayPlayCardsFromTopOfLibraryForLifeUntilEndOfTurnEffect() implements CardEffect {
}
