package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Cost that requires the player to return N permanents matching a filter from
 * the battlefield to their owner's hand. Used by cards like Multani, Yavimaya's
 * Avatar ("{1}{G}, Return two lands you control to their owner's hand: ...").
 */
public record ReturnMultiplePermanentsToHandCost(int count, PermanentPredicate filter) implements CostEffect {
}
