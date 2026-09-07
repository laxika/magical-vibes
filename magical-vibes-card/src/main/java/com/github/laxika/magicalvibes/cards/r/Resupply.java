package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "DTK", collectorNumber = "32")
public class Resupply extends Card {

    public Resupply() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(6));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
