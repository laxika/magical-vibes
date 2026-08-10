package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player chooses matching permanents to keep until they control at most {@code count}, then
 * sacrifices the rest. Choices are made in active-player order and the sacrifices happen together.
 */
public record EachPlayerSacrificesDownToCountEffect(int count, PermanentPredicate filter)
        implements CardEffect {
}
