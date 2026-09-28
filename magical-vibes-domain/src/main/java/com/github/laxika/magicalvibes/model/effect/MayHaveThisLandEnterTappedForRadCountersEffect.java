package com.github.laxika.magicalvibes.model.effect;

/** As this land enters, its controller may have it enter tapped and get rad counters. */
public record MayHaveThisLandEnterTappedForRadCountersEffect(int radCounterCount)
        implements ReplacementEffect {
}
