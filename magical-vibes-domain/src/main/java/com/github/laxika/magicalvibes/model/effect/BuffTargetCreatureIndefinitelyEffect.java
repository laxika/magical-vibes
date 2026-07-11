package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * "Target creature gets +X/+Y and gains [keywords]. This effect lasts indefinitely." (Riding the
 * Dilu Horse). Unlike {@link BoostTargetCreatureEffect} + {@link GrantKeywordEffect}, which wear
 * off at end of turn, this is a continuous effect with no duration (CR 611.2b): the handler
 * records it as a {@code PERMANENT} floating continuous effect on the target. The additive P/T
 * boost applies in sublayer 7c and the keywords in layer 6, read straight off the floating
 * effect by {@code GameQueryService.assembleStaticBonus} for the affected permanent.
 */
public record BuffTargetCreatureIndefinitelyEffect(int powerBoost, int toughnessBoost,
                                                   Set<Keyword> keywords) implements CardEffect {

    public BuffTargetCreatureIndefinitelyEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, Set.of());
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
