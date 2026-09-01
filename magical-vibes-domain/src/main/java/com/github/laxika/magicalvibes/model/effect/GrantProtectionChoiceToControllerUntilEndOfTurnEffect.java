package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, prompts the controller to choose a color, then grants that player protection
 * from the chosen color until end of turn.
 */
public record GrantProtectionChoiceToControllerUntilEndOfTurnEffect() implements CardEffect {
}
