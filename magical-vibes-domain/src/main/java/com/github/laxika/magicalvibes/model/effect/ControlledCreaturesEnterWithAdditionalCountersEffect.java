package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Static ability that functions while the source permanent is on the battlefield:
 * each other creature with the specified {@code subtype} controlled by the source's
 * controller enters the battlefield with additional +1/+1 counters. The amount may be
 * fixed or evaluated dynamically from the source and battlefield state.
 * <p>
 * This is a replacement effect (MTG Rule 614.1c) — it modifies how the creature
 * enters the battlefield rather than triggering after the fact. "Other" is handled
 * implicitly: the source is already on the battlefield while the affected creature is
 * still entering, and a creature entering simultaneously with the source does not see
 * the source's replacement effect (CR 614.12).
 * <p>
 * Used by cards like Sage of Fables ("Each other Wizard creature you control enters
 * with an additional +1/+1 counter on it.") and Giada, Font of Hope (where the
 * amount is the number of Angels already controlled).
 * <p>
 * A {@code null} {@code subtype} means "the creature type chosen as the source entered"
 * — the effect then reads the source permanent's {@code chosenSubtype} and applies to
 * nothing while no type has been chosen. Pair it with a
 * {@link ChooseSubtypeOnEnterEffect} in {@code ON_ENTER_BATTLEFIELD} (Metallic Mimic).
 */
public record ControlledCreaturesEnterWithAdditionalCountersEffect(
        CardSubtype subtype,
        DynamicAmount count,
        boolean allCreatures
) implements CardEffect {

    public ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype subtype, DynamicAmount count) {
        this(subtype, count, false);
    }

    public ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype subtype, int count) {
        this(subtype, new Fixed(count), false);
    }

    /**
     * "Each other creature you control of the chosen type enters with {@code count}
     * additional +1/+1 counters on it."
     */
    public static ControlledCreaturesEnterWithAdditionalCountersEffect ofChosenSubtype(int count) {
        return new ControlledCreaturesEnterWithAdditionalCountersEffect(null, count);
    }

    public static ControlledCreaturesEnterWithAdditionalCountersEffect forAllCreatures(DynamicAmount count) {
        return new ControlledCreaturesEnterWithAdditionalCountersEffect(null, count, true);
    }
}
