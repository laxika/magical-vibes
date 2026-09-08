package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses any player, then that player chooses a creature they control to return
 * to its owner's hand. This is non-targeting and is intended to be wrapped in {@link MayEffect}
 * when the player choice is optional.
 */
public record ChoosePlayerThenReturnCreatureToHandEffect() implements CardEffect {
}
