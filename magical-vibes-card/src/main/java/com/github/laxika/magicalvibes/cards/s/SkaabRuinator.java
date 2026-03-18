package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "77")
public class SkaabRuinator extends Card {

    public SkaabRuinator() {
        addEffect(EffectSlot.SPELL, new ExileNCardsFromGraveyardCost(3, CardType.CREATURE));
        addCastingOption(new GraveyardCast());
    }
}
