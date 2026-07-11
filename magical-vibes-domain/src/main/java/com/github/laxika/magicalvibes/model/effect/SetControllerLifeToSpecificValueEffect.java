package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the effect's controller's life total to a specific value.
 * E.g. Form of the Dragon: "At the beginning of each end step, your life total becomes 5."
 *
 * @param targetLifeTotal the life total to set for the controller
 */
public record SetControllerLifeToSpecificValueEffect(int targetLifeTotal) implements CardEffect {
}
