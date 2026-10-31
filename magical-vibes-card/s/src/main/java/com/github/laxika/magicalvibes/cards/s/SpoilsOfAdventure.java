package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "ZNR", collectorNumber = "237")
public class SpoilsOfAdventure extends Card {

    public SpoilsOfAdventure() {
        addStaticEffect(new ReduceOwnCastCostEffect(new PartySize()));
        addSpellEffect(new GainLifeEffect(3));
        addSpellEffect(new DrawCardEffect(3));
    }
}
