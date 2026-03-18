package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ISD", collectorNumber = "70")
public class MurderOfCrows extends Card {

    public MurderOfCrows() {
        // Whenever another creature dies, you may draw a card. If you do, discard a card.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MayEffect(
                new DrawAndDiscardCardEffect(), "Draw a card and discard a card?"
        ));
    }
}
