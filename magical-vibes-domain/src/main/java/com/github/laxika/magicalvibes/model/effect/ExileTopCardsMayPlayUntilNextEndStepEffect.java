package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top {@code count} cards of the controller's library and lets that player play them
 * until their next end step. The amount is evaluated as the effect resolves ("that many", e.g. the
 * counters the creature that died had: {@code CountersOnTriggeringPermanent}).
 */
public record ExileTopCardsMayPlayUntilNextEndStepEffect(DynamicAmount count) implements CardEffect {

    public ExileTopCardsMayPlayUntilNextEndStepEffect(int count) {
        this(new Fixed(count));
    }
}
