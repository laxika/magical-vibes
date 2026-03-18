package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "76")
public class SkaabGoliath extends Card {

    public SkaabGoliath() {
        addEffect(EffectSlot.SPELL, new ExileNCardsFromGraveyardCost(2, CardType.CREATURE));
    }
}
