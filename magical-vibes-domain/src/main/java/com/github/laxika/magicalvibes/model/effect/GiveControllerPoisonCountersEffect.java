package com.github.laxika.magicalvibes.model.effect;

/**
 * Give the controller of this effect N poison counters.
 * Used by cards like Phyrexian Vatmother whose upkeep trigger poisons their own controller.
 */
public record GiveControllerPoisonCountersEffect(int amount) implements CardEffect {
}
