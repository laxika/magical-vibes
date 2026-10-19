package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player sacrifices matching permanents until they control at most {@code count}. Choices are made in
 * active-player order and the sacrifices happen together.
 *
 * <p>When {@code choosesKept} is false the player chooses which excess permanents to sacrifice; when true the
 * player chooses the {@code count} permanents to keep (e.g. "choose a creature ... sacrifice the rest") and
 * the rest are sacrificed.
 */
public record EachPlayerSacrificesDownToCountEffect(int count, PermanentPredicate filter, boolean choosesKept)
        implements CardEffect {

    public EachPlayerSacrificesDownToCountEffect(int count, PermanentPredicate filter) {
        this(count, filter, false);
    }
}
