package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the controller's library until a nonland card is found, then grants the
 * controller permission to cast that card until end of turn at its normal cost.
 */
public record ExileTopUntilNonlandMayCastThisTurnEffect() implements CardEffect {
}
