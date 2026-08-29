package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * "Mill {@code count} cards. If at least one card matching {@code filter} is milled this way,
 * [{@code thenEffect}]" (Liliana, Untouched by Death's +1). The controller does the milling.
 *
 * <p>Only cards that actually reached the graveyard count — a replacement effect that diverts a
 * milled card elsewhere means it was not "milled this way". Wrap several follow-ups in a
 * {@link SequenceEffect}; the steps resolve in order against the same stack entry, so they must be
 * synchronous (no player-input pauses).</p>
 *
 * <p>{@link #whenAllCardsMilled(int, CardEffect)} is the targeted reflexive variant: it queues the
 * follow-up only when all requested cards were milled, and chooses its target after the mill.</p>
 */
public record MillControllerThenIfMilledEffect(DynamicAmount count, CardPredicate filter, CardEffect thenEffect,
                                               boolean requireAllCardsMilled, boolean thenEffectTargets)
        implements CardEffect {

    public MillControllerThenIfMilledEffect(DynamicAmount count, CardPredicate filter, CardEffect thenEffect) {
        this(count, filter, thenEffect, false, false);
    }

    /** Convenience constructor for a fixed mill count. */
    public MillControllerThenIfMilledEffect(int count, CardPredicate filter, CardEffect thenEffect) {
        this(new Fixed(count), filter, thenEffect);
    }

    /** Mill exactly {@code count} cards, then choose targets for the reflexive follow-up. */
    public static MillControllerThenIfMilledEffect whenAllCardsMilled(int count, CardEffect thenEffect) {
        return new MillControllerThenIfMilledEffect(
                new Fixed(count), new CardTruePredicate(), thenEffect, true, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return thenEffectTargets ? TargetSpec.NONE : thenEffect.targetSpec();
    }
}
