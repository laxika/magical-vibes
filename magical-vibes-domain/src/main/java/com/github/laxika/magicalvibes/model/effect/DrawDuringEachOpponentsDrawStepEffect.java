package com.github.laxika.magicalvibes.model.effect;

/**
 * Static ability "You draw [amount] card(s) during each opponent's draw step" (Teferi, Who Slows the Sunset's emblem).
 * It is no triggered ability: the controller draws right after the active player's draw-step draw (CR 504.1), with no
 * player getting priority in between (the card's ruling), as the draw step's turn-based action happens.
 * {@code StepTriggerService.handleDrawStep} reads it from the emblems in the command zone (CR 114.4); the draw happens
 * only while there is a draw step, so a skipped draw step draws nothing.
 *
 * @param amount how many cards the controller draws
 */
public record DrawDuringEachOpponentsDrawStepEffect(int amount) implements CardEffect {

    public DrawDuringEachOpponentsDrawStepEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive: " + amount);
        }
    }
}
