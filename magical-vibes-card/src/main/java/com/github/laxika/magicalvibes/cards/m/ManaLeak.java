package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "62")
public class ManaLeak extends Card {

    public ManaLeak() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3));
    }
}
