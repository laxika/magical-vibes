package com.github.laxika.magicalvibes.model.effect;

/** Each player exiles up to {@code count} cards from the top of their library and may play those
 * cards until the effect controller's next turn. */
public record EachPlayerExilesTopCardsMayPlayUntilNextTurnEffect(int count) implements CardEffect {
}
