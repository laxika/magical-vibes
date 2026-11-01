package com.github.laxika.magicalvibes.model.effect;

/**
 * Static as-enters ability (CR 614.1c) to indicate that this permanent
 * requires the controller to choose another creature they control as it enters
 * the battlefield ("As ~ enters, choose another creature you control.").
 * The chosen creature's ID is stored on the permanent via {@code chosenPermanentId}.
 */
public record ChooseAnotherCreatureOnEnterEffect() implements CardEffect {
}
