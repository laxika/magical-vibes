package com.github.laxika.magicalvibes.model.effect;

/**
 * Lets the controller put a creature card with mana value exactly X from their hand onto the
 * battlefield, where X is the activated ability's chosen value.
 */
public record PutCreatureFromHandWithManaValueXEffect() implements CardEffect {
}
