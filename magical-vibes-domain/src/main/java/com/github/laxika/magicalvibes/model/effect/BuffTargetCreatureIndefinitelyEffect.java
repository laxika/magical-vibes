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
 *
 * <p>The same record is also stamped, with an {@code UNTIL_YOUR_NEXT_TURN} duration on the
 * floating effect rather than {@code PERMANENT}, by {@code BoostTargetCreatureEffectHandler} for
 * "gets +X/+Y until your next turn" pumps: the duration lives on the floating effect, so both
 * durations share this one sublayer-7c read path. It is never named on a card for that case.
 */
public record BuffTargetCreatureIndefinitelyEffect(int powerBoost, int toughnessBoost,
                                                   Set<Keyword> keywords) implements CardEffect {

    public BuffTargetCreatureIndefinitelyEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, Set.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
