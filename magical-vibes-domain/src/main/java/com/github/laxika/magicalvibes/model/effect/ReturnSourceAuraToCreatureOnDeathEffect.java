package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered ability on an Aura: when its enchanted creature dies, return the Aura from its
 * controller's graveyard to the battlefield attached to a creature chosen by that controller.
 */
public record ReturnSourceAuraToCreatureOnDeathEffect() implements CardEffect {
}
