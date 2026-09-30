package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution-time effect that copies every spell and every other activated or triggered ability
 * controlled by the effect's controller on the stack.
 */
public record CopyControllerAllSpellsAndAbilitiesEffect() implements CardEffect {
}
