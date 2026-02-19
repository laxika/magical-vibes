package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesEffect;

@CardRegistration(set = "10E", collectorNumber = "222")
public class Pyroclasm extends Card {

    public Pyroclasm() {
        addEffect(EffectSlot.SPELL, new DealDamageToAllCreaturesEffect(2));
    }
}
