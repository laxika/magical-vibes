package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "76")
public class ShipwreckLooter extends Card {

    public ShipwreckLooter() {
        // Raid — When this creature enters, if you attacked this turn,
        // you may draw a card. If you do, discard a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Raid(), 
                new MayEffect(new DrawAndDiscardCardEffect(), "Draw a card and discard a card?")));
    }
}
