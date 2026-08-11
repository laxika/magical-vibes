package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent chooses a creature they control and puts {@code count} -1/-1 counters on it.
 */
public record EachOpponentBlightsEffect(int count) implements CardEffect {
}
