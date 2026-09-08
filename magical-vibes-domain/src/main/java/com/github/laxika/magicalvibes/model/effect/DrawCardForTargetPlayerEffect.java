package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Draws cards for the player stored in the stack entry's targetId field
 * (a targeted player, or the active player whose draw/upkeep step triggered the ability).
 *
 * @param amount                 number of cards to draw
 * @param requireSourceUntapped  if true, the source permanent (via sourcePermanentId)
 *                               must still be untapped at resolution time (intervening-if)
 * @param targetsPlayer          whether this effect itself establishes a player target
 * @param targetGroup            activated-ability target group to draw for, or {@code -1} for
 *                               the entry's normal target resolution
 */
public record DrawCardForTargetPlayerEffect(DynamicAmount amount, boolean requireSourceUntapped,
                                            boolean targetsPlayer, int targetGroup) implements CardEffect {

    public DrawCardForTargetPlayerEffect(int amount) {
        this(new Fixed(amount), false, false, -1);
    }

    public DrawCardForTargetPlayerEffect(int amount, boolean requireSourceUntapped) {
        this(new Fixed(amount), requireSourceUntapped, false, -1);
    }

    public DrawCardForTargetPlayerEffect(int amount, boolean requireSourceUntapped, boolean targetsPlayer) {
        this(new Fixed(amount), requireSourceUntapped, targetsPlayer, -1);
    }

    public DrawCardForTargetPlayerEffect(DynamicAmount amount, boolean requireSourceUntapped, boolean targetsPlayer) {
        this(amount, requireSourceUntapped, targetsPlayer, -1);
    }

    public static DrawCardForTargetPlayerEffect forTargetGroup(int amount, int targetGroup) {
        return forTargetGroup(new Fixed(amount), targetGroup);
    }

    public static DrawCardForTargetPlayerEffect forTargetGroup(DynamicAmount amount, int targetGroup) {
        return new DrawCardForTargetPlayerEffect(amount, false, true, targetGroup);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
