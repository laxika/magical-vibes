package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SOS", collectorNumber = "129")
public class SeizeTheSpoils extends Card {

    public SeizeTheSpoils() {
        // As an additional cost to cast this spell, discard a card.
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        // Draw two cards and create a Treasure token.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(1));
    }
}
