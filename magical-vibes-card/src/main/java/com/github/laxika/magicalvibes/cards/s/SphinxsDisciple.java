package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "BNG", collectorNumber = "51")
public class SphinxsDisciple extends Card {

    public SphinxsDisciple() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new DrawCardEffect(1));
    }
}
