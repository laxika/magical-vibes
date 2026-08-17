package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player other than the resolving stack entry's controller may draw up to {@code max} cards.
 */
public record EachOtherPlayerMayDrawUpToNCardsEffect(int max) implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(max);
    }
}
