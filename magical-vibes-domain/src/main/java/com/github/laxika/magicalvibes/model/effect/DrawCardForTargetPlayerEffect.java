package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Draws cards for the player stored in the stack entry's targetId field
 * (a targeted player, the active player whose draw/upkeep step triggered the ability, or the
 * damaged player when an {@link EventValue}-based instance is used by a combat-damage trigger).
 *
 * @param amount                 number of cards to draw
 * @param requireSourceUntapped  if true, the source permanent (via sourcePermanentId)
 *                               must still be untapped at resolution time (intervening-if)
 * @param targetsPlayer          whether this effect itself establishes a player target
 * @param targetGroup            activated-ability target group to draw for, or {@code -1} for
 *                               the entry's normal target resolution
 * @param opponentDrawStepOnly   whether an {@code Triggers.drawStep(ActivePlayer.ANY)} instance skips the
 *                               source controller's draw step
 * @param triggeringCardOwner    whether the player is instead the owner of the card a "whenever you play a card" event
 *                               was about ("its owner draws a card", Apple of Eden, Isu Relic), read from the entry as
 *                               the ability resolves ({@link #ofTriggeringCardOwner(int)})
 */
public record DrawCardForTargetPlayerEffect(DynamicAmount amount, boolean requireSourceUntapped,
                                            boolean targetsPlayer, int targetGroup,
                                            boolean opponentDrawStepOnly, boolean triggeringCardOwner)
        implements CardEffect, OpponentDrawStepOnlyEffect {

    public DrawCardForTargetPlayerEffect(DynamicAmount amount, boolean requireSourceUntapped,
                                         boolean targetsPlayer, int targetGroup, boolean opponentDrawStepOnly) {
        this(amount, requireSourceUntapped, targetsPlayer, targetGroup, opponentDrawStepOnly, false);
    }

    public DrawCardForTargetPlayerEffect(int amount) {
        this(new Fixed(amount), false, false, -1, false);
    }

    public DrawCardForTargetPlayerEffect(int amount, boolean requireSourceUntapped) {
        this(new Fixed(amount), requireSourceUntapped, false, -1, false);
    }

    public DrawCardForTargetPlayerEffect(int amount, boolean requireSourceUntapped, boolean targetsPlayer) {
        this(new Fixed(amount), requireSourceUntapped, targetsPlayer, -1, false);
    }

    public DrawCardForTargetPlayerEffect(DynamicAmount amount, boolean requireSourceUntapped, boolean targetsPlayer) {
        this(amount, requireSourceUntapped, targetsPlayer, -1, false);
    }

    public static DrawCardForTargetPlayerEffect forTargetGroup(int amount, int targetGroup) {
        return forTargetGroup(new Fixed(amount), targetGroup);
    }

    public static DrawCardForTargetPlayerEffect forTargetGroup(DynamicAmount amount, int targetGroup) {
        return new DrawCardForTargetPlayerEffect(amount, false, true, targetGroup, false);
    }

    /** "That card's owner draws [amount]", of the card the trigger event was about; it chooses no target. */
    public static DrawCardForTargetPlayerEffect ofTriggeringCardOwner(int amount) {
        return new DrawCardForTargetPlayerEffect(new Fixed(amount), false, false, -1, false, true);
    }

    public static DrawCardForTargetPlayerEffect forOpponentDrawStep(int amount) {
        return new DrawCardForTargetPlayerEffect(new Fixed(amount), false, false, -1, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
