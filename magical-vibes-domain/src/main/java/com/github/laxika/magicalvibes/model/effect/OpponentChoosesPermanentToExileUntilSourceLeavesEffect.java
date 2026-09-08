package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * An opponent chooses a permanent controlled by the effect controller to exile until the source
 * permanent leaves the battlefield.
 */
public record OpponentChoosesPermanentToExileUntilSourceLeavesEffect(PermanentPredicate filter)
        implements CardEffect {
}
