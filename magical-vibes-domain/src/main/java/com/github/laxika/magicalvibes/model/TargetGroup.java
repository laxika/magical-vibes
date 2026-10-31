package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * One target group of a card: what it may target and how many targets it takes. A card declares its
 * groups in order with {@link Card#target(TargetFilter)} and the other {@code target…} builders, and
 * {@link #getIndex()} is the group's position in that order. The effects bound to a group resolve
 * against the objects chosen for it. A group is an immutable value; changing one replaces it.
 *
 * @see Card#getTargetGroups()
 * @see SpellAbility#targets()
 */
@Getter
@EqualsAndHashCode
@ToString
public final class TargetGroup {

    /** Effective maximum for an "any number of targets" group; large enough to never constrain play. */
    public static final int ANY_NUMBER_OF_TARGETS = 1_000_000;

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
    private final int distinctScope;

    TargetGroup(TargetFilter filter, TargetFilter kickedFilter, int minTargets, int maxTargets,
                int kickedMinTargets, int kickedMaxTargets, int index,
                boolean xScaled, DynamicAmount dynamicMinTargets, DynamicAmount dynamicMaxTargets,
                int giftPromisedMinTargets) {
        this(filter, kickedFilter, minTargets, unboundedIfAnyNumber(maxTargets), kickedMinTargets,
                unboundedIfAnyNumber(kickedMaxTargets), giftPromisedMinTargets, index, dynamicMinTargets,
                dynamicMaxTargets, xScaled, -1);
    }

    private TargetGroup(TargetFilter filter, TargetFilter kickedFilter, int minTargets, int maxTargets,
                        int kickedMinTargets, int kickedMaxTargets, int giftPromisedMinTargets, int index,
                        DynamicAmount dynamicMinTargets, DynamicAmount dynamicMaxTargets, boolean xScaled,
                        int distinctScope) {
        this.filter = filter;
        this.kickedFilter = kickedFilter;
        this.minTargets = minTargets;
        this.maxTargets = maxTargets;
        this.kickedMinTargets = kickedMinTargets;
        this.kickedMaxTargets = kickedMaxTargets;
        this.giftPromisedMinTargets = giftPromisedMinTargets;
        this.index = index;
        this.dynamicMinTargets = dynamicMinTargets;
        this.dynamicMaxTargets = dynamicMaxTargets;
        this.xScaled = xScaled;
        this.distinctScope = distinctScope;
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

    public TargetFilter getFilter(boolean kicked) {
        return kicked ? kickedFilter : filter;
    }

    /** This group as part of the mode {@code scope}, whose targets must all be different objects. */
    TargetGroup inDistinctScope(int scope) {
        return new TargetGroup(filter, kickedFilter, minTargets, maxTargets, kickedMinTargets, kickedMaxTargets,
                giftPromisedMinTargets, index, dynamicMinTargets, dynamicMaxTargets, xScaled, scope);
    }

    /** This group at position {@code newIndex} of another card's groups, without a mode scope. */
    TargetGroup atIndex(int newIndex) {
        return new TargetGroup(filter, kickedFilter, minTargets, maxTargets, kickedMinTargets, kickedMaxTargets,
                giftPromisedMinTargets, newIndex, dynamicMinTargets, dynamicMaxTargets, xScaled, -1);
    }
}
