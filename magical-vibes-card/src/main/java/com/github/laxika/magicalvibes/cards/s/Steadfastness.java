package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "31")
public class Steadfastness extends Card {

    public Steadfastness() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(0, 3));
    }
}
