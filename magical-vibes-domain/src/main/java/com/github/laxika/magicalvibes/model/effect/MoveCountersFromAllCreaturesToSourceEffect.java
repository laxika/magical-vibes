package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Moves all counters of one kind from every creature onto the source permanent.
 *
 * <p>The source permanent is read from the stack entry, so this is an untargeted effect. The
 * counter total is collected at resolution, and the source itself is included when it is a
 * creature.</p>
 *
 * @param counterType the kind of counter to move
 */
public record MoveCountersFromAllCreaturesToSourceEffect(CounterType counterType) implements CardEffect {
}
