package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "ULG", collectorNumber = "36")
public class Miscalculation extends Card {

    public Miscalculation() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
        addCycling("{2}");
    }
}
