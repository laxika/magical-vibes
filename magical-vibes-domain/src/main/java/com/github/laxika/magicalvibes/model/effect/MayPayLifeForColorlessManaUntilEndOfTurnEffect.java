package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, any time the controller could activate a mana ability, they may pay 1 life to add
 * {@code {C}} (e.g. Channel). A floating effect on the controller that
 * lasts until end of turn; the life-for-mana special action deducts 1
 * life and adds one colorless mana to the controller's pool.
 */
public record MayPayLifeForColorlessManaUntilEndOfTurnEffect() implements CardEffect {
}
