package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CardsDiscardedByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Controller draws {@code amount} cards, one at a time (so draw-replacement effects and
 * "whenever you draw" triggers see each individual draw).
 */
public record DrawCardEffect(DynamicAmount amount) implements CardDrawingEffect {

    public DrawCardEffect() {
        this(1);
    }

    public DrawCardEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }

    @Override
    public TargetSpec targetSpec() {
        // Only target-relative amounts require a player target on the stack entry (e.g. Dream
        // Salvage draws equal to the number of cards target opponent discarded this turn).
        return amount instanceof CardsDiscardedByTargetPlayerThisTurn
                ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }
}
