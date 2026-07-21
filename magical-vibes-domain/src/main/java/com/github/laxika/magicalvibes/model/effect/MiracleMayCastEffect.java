package com.github.laxika.magicalvibes.model.effect;

/**
 * Body of the miracle triggered ability (CR 702.94a): "When you reveal this card this way,
 * you may cast it by paying [cost] rather than its mana cost." Resolution queues a may-cast
 * prompt that pays the card's {@code MiracleCast} cost and casts from hand (ignoring
 * type-based timing).
 */
public record MiracleMayCastEffect() implements CardEffect {
}
