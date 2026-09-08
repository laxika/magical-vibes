package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for playing a card that was exiled by the source permanent and is still tracked
 * with that permanent.
 */
public record PlayedCardExiledWithSourceTriggerEffect() implements CardEffect {
}
