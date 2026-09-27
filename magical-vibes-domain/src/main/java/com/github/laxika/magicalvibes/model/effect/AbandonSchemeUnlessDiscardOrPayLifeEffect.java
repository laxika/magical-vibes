package com.github.laxika.magicalvibes.model.effect;

/**
 * During an ongoing scheme's upkeep trigger, its controller must discard a card or pay life,
 * otherwise the scheme is abandoned.
 */
public record AbandonSchemeUnlessDiscardOrPayLifeEffect(int lifeCost) implements CardEffect {
}
