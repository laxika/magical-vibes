package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses any number of creatures; the chosen creatures must block this combat if
 * able. The choice is non-targeted and may include creatures controlled by either player.
 */
public record ChooseCreaturesToBlockThisTurnIfAbleEffect() implements CardEffect {
}
