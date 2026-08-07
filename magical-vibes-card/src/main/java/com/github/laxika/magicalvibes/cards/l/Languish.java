package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "ORI", collectorNumber = "105")
public class Languish extends Card {

    public Languish() {
        // All creatures get -4/-4 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-4, -4));
    }
}
