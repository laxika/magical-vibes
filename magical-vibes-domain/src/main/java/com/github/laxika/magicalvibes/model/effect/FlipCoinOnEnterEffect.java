package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Entry replacement that flips a coin and gives the entering permanent one of two fixed
 * characteristic states.
 */
public record FlipCoinOnEnterEffect(
        int headsPower,
        int headsToughness,
        Set<Keyword> headsKeywords,
        int tailsPower,
        int tailsToughness,
        Set<Keyword> tailsKeywords
) implements ReplacementEffect {
}
