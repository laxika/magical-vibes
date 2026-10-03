package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Attack trigger for Eumidian Wastewaker: the controller and defending player each discard a card
 * or sacrifice a permanent, then the controller draws for each land put into a graveyard this way.
 */
public record EumidianWastewakerEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new EventValue();
    }
}
