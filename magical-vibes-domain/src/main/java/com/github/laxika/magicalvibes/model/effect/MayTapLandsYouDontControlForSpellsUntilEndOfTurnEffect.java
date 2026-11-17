package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, the controller may tap lands they don't control for mana. Mana produced this way
 * may only be spent to cast spells (e.g. Piracy). A floating effect on the controller that lasts until end of turn;
 * the foreign-land tap action routes the produced
 * mana into the spell-only bucket of the controller's mana pool.
 */
public record MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect() implements CardEffect {
}
