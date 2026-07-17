package com.github.laxika.magicalvibes.model.amount;

/**
 * The current life total of the targeted player (the player carried on the stack entry's target
 * channel — for combat-damage triggers this is the player that was dealt damage). The player-facing
 * sibling of {@link ControllerLifeTotal}; evaluates to 0 without a legal target player.
 */
public record TargetPlayerLifeTotal() implements DynamicAmount {
}
