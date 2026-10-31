package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DGM", collectorNumber = "14")
public class Mindstatic extends Card {

    public Mindstatic() {
        addSpellEffect(new CounterUnlessPaysEffect(6));
    }
}
