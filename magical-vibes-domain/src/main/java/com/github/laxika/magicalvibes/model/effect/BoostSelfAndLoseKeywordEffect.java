package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * One-shot effect: the source permanent gets +X/+Y and loses the given keyword until end of turn.
 * Combines {@link BoostSelfEffect} and {@link RemoveKeywordEffect} (SELF scope) so it can be used
 * as a single wrapped reward (e.g. the win reward of {@link ClashEffect}). Used by Sentry Oak:
 * on winning a clash it gets +2/+0 and loses defender until end of turn.
 */
public record BoostSelfAndLoseKeywordEffect(int powerBoost, int toughnessBoost, Keyword keyword)
        implements CardEffect {

    @Override
    public boolean isSelfTargeting() {
        return true;
    }
}
