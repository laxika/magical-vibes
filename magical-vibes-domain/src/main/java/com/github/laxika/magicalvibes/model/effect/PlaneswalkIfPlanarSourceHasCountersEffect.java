package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Planeswalks if the source planar object has at least the specified number of counters. */
public record PlaneswalkIfPlanarSourceHasCountersEffect(CounterType counterType, int threshold)
        implements CardEffect {
}
