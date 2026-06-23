package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, prompts the controller to choose a color, then grants that controller
 * <em>and each permanent that controller controls</em> protection from the chosen color
 * until end of turn. A single color is chosen and applied to all of them.
 * <p>
 * Used by the fateful hour mode of cards such as Faith's Shield
 * ("you and each permanent you control gain protection from the color of your choice").
 */
public record GrantProtectionChoiceToControllerAndPermanentsUntilEndOfTurnEffect() implements CardEffect {
}
