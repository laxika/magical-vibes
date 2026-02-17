package com.github.laxika.magicalvibes.model.effect;

/**
 * At the beginning of your upkeep, sacrifice a creature other than this creature.
 * If you can't, this creature deals {@code damage} damage to you.
 */
public record SacrificeOtherCreatureOrDamageEffect(int damage) implements CardEffect {
}
