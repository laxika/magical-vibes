package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A player discards cards at random. The amount is a {@link DynamicAmount}, so the same effect
 * covers a fixed count ("discards a card at random") or an X value ({@code XValue} — Mind Shatter).
 *
 * @param amount           number of cards to discard at random
 * @param causedByOpponent when {@code true} the targeted player (stack entry's targetId) discards;
 *                         when {@code false} the controller discards (self-mill/discard, e.g. rummaging)
 */
public record TargetPlayerRandomDiscardEffect(DynamicAmount amount, boolean causedByOpponent) implements CardEffect {

    public TargetPlayerRandomDiscardEffect() {
        this(new Fixed(1), true);
    }

    public TargetPlayerRandomDiscardEffect(int amount) {
        this(new Fixed(amount), false);
    }

    public TargetPlayerRandomDiscardEffect(DynamicAmount amount) {
        this(amount, false);
    }

    @Override
    public boolean canTargetPlayer() {
        return causedByOpponent;
    }
}
