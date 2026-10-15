package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "DST", collectorNumber = "78")
public class Nourish extends Card {

    public Nourish() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(6));
    }
}
