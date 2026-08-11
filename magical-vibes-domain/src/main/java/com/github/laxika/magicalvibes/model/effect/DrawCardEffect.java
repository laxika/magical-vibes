package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CardsDiscardedByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;

/**
 * Controller draws {@code amount} cards, one at a time (so draw-replacement effects and
 * "whenever you draw" triggers see each individual draw).
 */
public record DrawCardEffect(DynamicAmount amount, boolean onlyIfSacrificed) implements ManaAbilityCardDrawingEffect {

    public DrawCardEffect(DynamicAmount amount) {
        this(amount, false);
    }

    public DrawCardEffect() {
        this(1);
    }

    public DrawCardEffect(int amount) {
        this(new Fixed(amount));
    }

    public static DrawCardEffect sacrificeOnly(int amount) {
        return new DrawCardEffect(new Fixed(amount), true);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return onlyIfSacrificed;
    }

    @Override
    public TargetSpec targetSpec() {
        // Only target-relative amounts require a player target on the stack entry (e.g. Dream
        // Salvage draws equal to the number of cards target opponent discarded this turn; Tamiyo,
        // the Moon Sage draws for each tapped creature target player controls).
        return isTargetRelative() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    private boolean isTargetRelative() {
        if (amount instanceof CardsDiscardedByTargetPlayerThisTurn) {
            return true;
        }
        return amount instanceof PermanentCount count && count.scope() == CountScope.TARGET_PLAYER;
    }
}
