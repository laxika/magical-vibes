package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;

/**
 * Declares a target for a spell or ability and associates effects with it.
 * <p>
 * Card constructors use the target-first builder pattern:
 * <pre>
 * target(filter).addEffect(EffectSlot.SPELL, effect);
 * </pre>
 * Each {@code target()} call declares a new target slot. Effects added to that
 * slot are resolved using the permanent chosen for that target position.
 *
 * @see Card#target(TargetFilter)
 * @see Card#target(TargetFilter, int, int)
 */
@Getter
public class SpellTarget {

    /** Effective maximum for an "any number of targets" group; large enough to never constrain play. */
    public static final int ANY_NUMBER_OF_TARGETS = 1_000_000;

    private final Card card;
    private final TargetFilter filter;
    private final TargetFilter kickedFilter;
    private final int minTargets;
    private final int maxTargets;
    private final int kickedMinTargets;
    private final int kickedMaxTargets;
    private final int giftPromisedMinTargets;
    private final int index;
    private final DynamicAmount dynamicMinTargets;
    private final DynamicAmount dynamicMaxTargets;
    /**
     * When true the number of targets scales with the spell's X: the effective max
     * is {@code min(xValue, maxTargets)} and the effective min is {@code min(xValue, minTargets)}.
     * Used by "Destroy X target nonblack creatures"-style spells (Dregs of Sorrow), where
     * {@code maxTargets} acts only as a sanity cap.
     */
    private final boolean xScaled;
    /**
     * Identifies the mode a modal spell's target group belongs to, or {@code -1} when the group is not
     * scoped. Groups that share a non-negative scope must hold different objects (one mode's "another
     * target"), even when {@link Card#isAllowSharedTargets()} lets other groups share objects with them.
     */
    private int distinctScope = -1;

    SpellTarget(Card card, TargetFilter filter, int minTargets, int maxTargets, int index) {
        this(card, filter, filter, minTargets, maxTargets, minTargets, maxTargets, index, false, null, null,
                minTargets);
    }

    SpellTarget(Card card, TargetFilter filter, int minTargets, int maxTargets, int index, boolean xScaled) {
        this(card, filter, filter, minTargets, maxTargets, minTargets, maxTargets, index, xScaled, null, null,
                minTargets);
    }

    SpellTarget(Card card, TargetFilter filter, int minTargets, int maxTargets, int index,
                boolean xScaled, DynamicAmount dynamicMaxTargets) {
        this(card, filter, filter, minTargets, maxTargets, minTargets, maxTargets, index, xScaled, null,
                dynamicMaxTargets, minTargets);
    }

    SpellTarget(Card card, TargetFilter filter, int minTargets, int maxTargets, int index,
                boolean xScaled, DynamicAmount dynamicMinTargets, DynamicAmount dynamicMaxTargets) {
        this(card, filter, filter, minTargets, maxTargets, minTargets, maxTargets, index, xScaled,
                dynamicMinTargets, dynamicMaxTargets, minTargets);
    }

    SpellTarget(Card card, TargetFilter filter, int minTargets, int maxTargets,
                int kickedMinTargets, int kickedMaxTargets, int index,
                boolean xScaled, DynamicAmount dynamicMinTargets, DynamicAmount dynamicMaxTargets) {
        this(card, filter, filter, minTargets, maxTargets, kickedMinTargets, kickedMaxTargets, index,
                xScaled, dynamicMinTargets, dynamicMaxTargets, minTargets);
    }

    SpellTarget(Card card, TargetFilter filter, TargetFilter kickedFilter, int minTargets, int maxTargets,
                int kickedMinTargets, int kickedMaxTargets, int index,
                boolean xScaled, DynamicAmount dynamicMinTargets, DynamicAmount dynamicMaxTargets) {
        this(card, filter, kickedFilter, minTargets, maxTargets, kickedMinTargets, kickedMaxTargets, index,
                xScaled, dynamicMinTargets, dynamicMaxTargets, minTargets);
    }

    SpellTarget(Card card, TargetFilter filter, int minTargets, int maxTargets,
                int kickedMinTargets, int kickedMaxTargets, int index,
                boolean xScaled, DynamicAmount dynamicMinTargets, DynamicAmount dynamicMaxTargets,
                int giftPromisedMinTargets) {
        this(card, filter, filter, minTargets, maxTargets, kickedMinTargets, kickedMaxTargets, index,
                xScaled, dynamicMinTargets, dynamicMaxTargets, giftPromisedMinTargets);
    }

    SpellTarget(Card card, TargetFilter filter, TargetFilter kickedFilter, int minTargets, int maxTargets,
                int kickedMinTargets, int kickedMaxTargets, int index,
                boolean xScaled, DynamicAmount dynamicMinTargets, DynamicAmount dynamicMaxTargets,
                int giftPromisedMinTargets) {
        this.card = card;
        this.filter = filter;
        this.kickedFilter = kickedFilter;
        this.minTargets = minTargets;
        this.maxTargets = unboundedIfAnyNumber(maxTargets);
        this.kickedMinTargets = kickedMinTargets;
        this.kickedMaxTargets = unboundedIfAnyNumber(kickedMaxTargets);
        this.giftPromisedMinTargets = giftPromisedMinTargets;
        this.index = index;
        this.xScaled = xScaled;
        this.dynamicMinTargets = dynamicMinTargets;
        this.dynamicMaxTargets = dynamicMaxTargets;
    }

    /**
     * Cards written "any number of target ..." declare a maximum of 99 as a stand-in for no limit; a
     * declared maximum of at least 99 is lifted to {@link #ANY_NUMBER_OF_TARGETS} so that more than
     * 99 targets are legal.
     */
    private static int unboundedIfAnyNumber(int declaredMaximum) {
        return declaredMaximum >= 99 ? ANY_NUMBER_OF_TARGETS : declaredMaximum;
    }

    /**
     * Whether this group needs exactly X targets ({@link Card#targetExactlyX}): an X-scaled group whose
     * declared minimum equals its declared maximum, including a maximum lifted to
     * {@link #ANY_NUMBER_OF_TARGETS}.
     */
    public boolean isExactlyXScaled() {
        return xScaled && minTargets > 0 && unboundedIfAnyNumber(minTargets) == maxTargets;
    }

    /** Marks this group as part of the mode {@code scope}, whose targets must all be different objects. */
    public SpellTarget inDistinctScope(int scope) {
        this.distinctScope = scope;
        return this;
    }

    public TargetFilter getFilter(boolean kicked) {
        return kicked ? kickedFilter : filter;
    }

    /**
     * Adds an effect to this target slot. The effect is registered on the Card's
     * effect list and mapped to this target's index for resolution.
     */
    public SpellTarget addEffect(EffectSlot slot, CardEffect effect) {
        card.addEffect(slot, effect);
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }

    /**
     * Binds an effect that is already registered through another target slot to this slot as well,
     * so one effect instance resolves against the targets of several groups at once (Don and Leo
     * exiles its artifact and creature targets together before returning them).
     */
    public SpellTarget bindEffect(CardEffect effect) {
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }

    /**
     * Adds an effect with a specific trigger mode to this target slot.
     */
    public SpellTarget addEffect(EffectSlot slot, CardEffect effect, TriggerMode triggerMode) {
        card.addEffect(slot, effect, triggerMode);
        card.registerEffectTargetIndex(effect, this.index);
        return this;
    }
}
