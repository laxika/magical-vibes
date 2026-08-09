package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, when the controller taps a land for mana, that land produces one mana of a
 * color of the controller's choice instead of its normal type and amount. The choice is made for
 * each land tap.
 */
public record LandManaProducesOneChosenColorUntilEndOfTurnEffect() implements CardEffect {
}
