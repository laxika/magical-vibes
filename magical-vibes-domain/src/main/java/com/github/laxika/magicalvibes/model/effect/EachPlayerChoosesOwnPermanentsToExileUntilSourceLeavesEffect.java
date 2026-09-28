package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Each player chooses up to {@code maxCount} qualifying permanents they control to exile until the source leaves. */
public record EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffect(
        PermanentPredicate filter, int maxCount) implements CardEffect {
}
