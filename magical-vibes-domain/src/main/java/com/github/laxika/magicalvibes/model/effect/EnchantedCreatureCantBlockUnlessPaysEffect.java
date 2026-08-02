package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura effect: the enchanted creature can't block unless its controller pays the given
 * amount of generic mana as an additional cost to declare it as a blocker.
 */
public record EnchantedCreatureCantBlockUnlessPaysEffect(int amount) implements CardEffect {
}
