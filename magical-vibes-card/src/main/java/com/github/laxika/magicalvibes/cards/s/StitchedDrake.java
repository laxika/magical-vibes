package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "80")
public class StitchedDrake extends Card {

    public StitchedDrake() {
        addEffect(EffectSlot.SPELL, new ExileCardFromGraveyardCost(CardType.CREATURE));
    }
}
