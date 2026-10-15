package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "ZNR", collectorNumber = "251")
public class SeaGateColossus extends Card {

    public SeaGateColossus() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new PartySize()));
    }
}
