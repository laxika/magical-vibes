package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles every card from the controller's hand face down and returns those cards at the next end step, then draws
 * {@code thenDraw} cards ("return those cards to your hand, then draw a card", Ignorant Bliss).
 *
 * @param thenDraw the cards the controller draws after the return, as part of the same delayed ability; 0 for none
 */
public record ExileControllerHandFaceDownAndReturnAtNextEndStepEffect(int thenDraw) implements CardEffect {

    public ExileControllerHandFaceDownAndReturnAtNextEndStepEffect() {
        this(0);
    }
}
