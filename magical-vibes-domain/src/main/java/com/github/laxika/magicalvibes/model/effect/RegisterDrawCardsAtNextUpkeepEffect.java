package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * When resolved, registers a delayed trigger so the resolving controller draws {@code count} cards
 * at the beginning of the next turn's upkeep. Used for "Draw a card at the beginning of the next
 * turn's upkeep" (e.g. Blessed Wine). Drained in {@code StepTriggerService.handleUpkeepTriggers}.
 *
 * @param count the number of cards drawn at the next upkeep
 */
public record RegisterDrawCardsAtNextUpkeepEffect(int count) implements CardDrawingEffect {

    public RegisterDrawCardsAtNextUpkeepEffect() {
        this(1);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(count);
    }
}
