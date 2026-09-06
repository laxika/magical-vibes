package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * "Mill {@code count} cards. If at least one card matching {@code filter} is milled this way,
 * [{@code thenEffect}], otherwise [{@code elseEffect}]" (Liliana, Untouched by Death's +1).
 * The controller does the milling.
 *
 * <p>Only cards that actually reached the graveyard count — a replacement effect that diverts a
 * milled card elsewhere means it was not "milled this way". Wrap several follow-ups in a
 * {@link SequenceEffect}; the steps resolve in order against the same stack entry, so they must be
 * synchronous (no player-input pauses).</p>
 *
 * @param elseEffect optional synchronous follow-up when no matching card was milled
 */
public record MillControllerThenIfMilledEffect(DynamicAmount count, CardPredicate filter, CardEffect thenEffect,
                                               CardEffect elseEffect, boolean requireAllCardsMilled,
                                               boolean thenEffectTargets)
        implements CardEffect {

    /** Compatibility constructor for a dynamic mill count without an "otherwise" effect. */
    public MillControllerThenIfMilledEffect(DynamicAmount count, CardPredicate filter, CardEffect thenEffect) {
        this(count, filter, thenEffect, null, false, false);
    }

    /** Convenience constructor for a fixed mill count. */
    public MillControllerThenIfMilledEffect(int count, CardPredicate filter, CardEffect thenEffect) {
        this(new Fixed(count), filter, thenEffect, null, false, false);
    }

    /** Convenience constructor for a fixed mill count with an "otherwise" effect. */
    public MillControllerThenIfMilledEffect(int count, CardPredicate filter, CardEffect thenEffect,
                                            CardEffect elseEffect) {
        this(new Fixed(count), filter, thenEffect, elseEffect, false, false);
    }

    public static MillControllerThenIfMilledEffect whenAllCardsMilled(int count, CardEffect thenEffect) {
        return new MillControllerThenIfMilledEffect(
                new Fixed(count), new CardTruePredicate(), thenEffect, null, true, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return thenEffectTargets ? TargetSpec.NONE : thenEffect.targetSpec();
    }
}
