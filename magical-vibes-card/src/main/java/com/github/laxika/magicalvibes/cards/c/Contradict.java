package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DTK", collectorNumber = "49")
public class Contradict extends Card {

    public Contradict() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
