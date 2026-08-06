package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "GTC", collectorNumber = "179")
public class MortusStrider extends Card {

    public MortusStrider() {
        // When this creature dies, return it to its owner's hand.
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
