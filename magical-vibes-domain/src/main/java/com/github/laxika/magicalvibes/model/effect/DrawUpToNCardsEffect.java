package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A player may draw up to {@code max} cards — they choose any number from 0 to {@code max} at
 * resolution. Used by Arcane Denial's delayed upkeep trigger ("Its controller may draw up to two
 * cards at the beginning of the next turn's upkeep") and by Fatal Lore's second mode ("That player
 * draws up to three cards"), where {@code recipient} is {@link DrawUpToRecipient#OPPONENT}.
 *
 * @param max       the largest number of cards that may be drawn
 * @param recipient who chooses the amount and draws
 */
public record DrawUpToNCardsEffect(int max, DrawUpToRecipient recipient) implements CardDrawingEffect {

    public DrawUpToNCardsEffect(int max) {
        this(max, DrawUpToRecipient.CONTROLLER);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(max);
    }
}
