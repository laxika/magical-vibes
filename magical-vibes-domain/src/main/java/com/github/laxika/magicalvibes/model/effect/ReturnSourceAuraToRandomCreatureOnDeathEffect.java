package com.github.laxika.magicalvibes.model.effect;

/**
 * When the enchanted creature dies, returns the source Aura from its owner's graveyard to the
 * battlefield attached to a creature it can legally enchant, chosen at random.
 */
public record ReturnSourceAuraToRandomCreatureOnDeathEffect() implements CardEffect {
}
