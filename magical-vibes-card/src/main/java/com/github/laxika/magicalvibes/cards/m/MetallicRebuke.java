package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "AER", collectorNumber = "39")
public class MetallicRebuke extends Card {

    public MetallicRebuke() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3));
    }
}
