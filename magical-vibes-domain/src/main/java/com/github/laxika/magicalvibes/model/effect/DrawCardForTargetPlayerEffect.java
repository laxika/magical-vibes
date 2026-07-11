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
 */
public record DrawCardForTargetPlayerEffect(DynamicAmount amount, boolean requireSourceUntapped, boolean targetsPlayer) implements CardEffect {

    public DrawCardForTargetPlayerEffect(int amount) {
        this(new Fixed(amount), false, false);
    }

    public DrawCardForTargetPlayerEffect(int amount, boolean requireSourceUntapped) {
        this(new Fixed(amount), requireSourceUntapped, false);
    }

    public DrawCardForTargetPlayerEffect(int amount, boolean requireSourceUntapped, boolean targetsPlayer) {
        this(new Fixed(amount), requireSourceUntapped, targetsPlayer);
    }

    @Override
    public boolean canTargetPlayer() {
        return targetsPlayer;
    }
}
