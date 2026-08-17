package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Registers a delayed trigger that makes a player draw cards at the next end step. */
public record RegisterDrawCardsAtNextEndStepEffect(int count, EndStepDrawRecipient recipient)
        implements CardDrawingEffect {

    public RegisterDrawCardsAtNextEndStepEffect() {
        this(1, EndStepDrawRecipient.CONTROLLER);
    }

    public RegisterDrawCardsAtNextEndStepEffect(int count) {
        this(count, EndStepDrawRecipient.CONTROLLER);
    }

    public static RegisterDrawCardsAtNextEndStepEffect triggeringPlayer() {
        return new RegisterDrawCardsAtNextEndStepEffect(1, EndStepDrawRecipient.TRIGGERING_PLAYER);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(count);
    }
}
