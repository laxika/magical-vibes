package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts a creature card from the controller's hand onto the battlefield when it shares a creature
 * type with every permanent retained from the ability's tracked permanent cost choices.
 */
public record PutCreatureFromHandSharingChosenCostPermanentsEffect() implements CardEffect {
}
