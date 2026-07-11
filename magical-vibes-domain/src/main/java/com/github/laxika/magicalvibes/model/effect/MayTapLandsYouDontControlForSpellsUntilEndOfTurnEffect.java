package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, the controller may tap lands they don't control for mana. Mana produced this way
 * may only be spent to cast spells (e.g. Piracy). Marks the controller in
 * {@code GameData.mayTapLandsForSpellsUntilEndOfTurn}; the foreign-land tap action routes the produced
 * mana into the spell-only bucket of the controller's mana pool.
 */
public record MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect() implements CardEffect {
}
