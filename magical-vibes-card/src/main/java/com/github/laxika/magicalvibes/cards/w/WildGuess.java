package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "M13", collectorNumber = "157")
@CardRegistration(set = "M14", collectorNumber = "161")
public class WildGuess extends Card {

    public WildGuess() {
        // As an additional cost to cast this spell, discard a card.
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
