package com.github.laxika.magicalvibes.model.effect;

/**
 * Uses up, as it resolves, the boon whose ability this is: one made by {@link CreateBoonEffect#usedAsItResolves}, of
 * the resolving ability's controller and source card.
 */
public record ConsumeBoonEffect() implements CardEffect {
}
