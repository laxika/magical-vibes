package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura effect: the enchanted creature can't be tapped to pay a Vehicle's crew cost.
 * The crew-cost handler checks this effect while building and validating its legal choices.
 */
public record EnchantedCreatureCantCrewEffect() implements CardEffect {
}
