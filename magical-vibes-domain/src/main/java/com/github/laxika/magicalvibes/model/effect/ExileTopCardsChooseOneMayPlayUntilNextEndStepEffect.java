package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top {@code count} cards of the controller's library, then has the controller choose
 * one of those cards to play until their next end step.
 */
public record ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(int count) implements CardEffect {
}
