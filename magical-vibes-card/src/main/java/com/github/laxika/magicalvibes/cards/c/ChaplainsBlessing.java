package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SOI", collectorNumber = "10")
public class ChaplainsBlessing extends Card {

    public ChaplainsBlessing() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(5));
    }
}
