package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "65")
public class MakeshiftMauler extends Card {

    public MakeshiftMauler() {
        addEffect(EffectSlot.SPELL, new ExileCardFromGraveyardCost(CardType.CREATURE));
    }
}
