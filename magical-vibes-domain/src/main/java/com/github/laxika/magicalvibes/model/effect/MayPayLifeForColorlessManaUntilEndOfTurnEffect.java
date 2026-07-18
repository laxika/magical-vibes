package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, any time the controller could activate a mana ability, they may pay 1 life to add
 * {@code {C}} (e.g. Channel). Marks the controller in
 * {@code GameData.mayPayLifeForColorlessManaUntilEndOfTurn}; the life-for-mana special action deducts 1
 * life and adds one colorless mana to the controller's pool. Cleared at end of turn.
 */
public record MayPayLifeForColorlessManaUntilEndOfTurnEffect() implements CardEffect {
}
