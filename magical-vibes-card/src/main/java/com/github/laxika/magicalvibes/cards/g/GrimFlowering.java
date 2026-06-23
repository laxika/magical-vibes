package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardsPerCreatureCardInGraveyardEffect;

@CardRegistration(set = "DKA", collectorNumber = "117")
public class GrimFlowering extends Card {

    public GrimFlowering() {
        addEffect(EffectSlot.SPELL, new DrawCardsPerCreatureCardInGraveyardEffect(1));
    }
}
