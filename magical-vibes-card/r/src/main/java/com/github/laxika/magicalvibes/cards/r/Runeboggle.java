package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GPT", collectorNumber = "33")
public class Runeboggle extends Card {

    public Runeboggle() {
        addSpellEffect(new CounterUnlessPaysEffect(1));
        addSpellEffect(new DrawCardEffect());
    }
}
