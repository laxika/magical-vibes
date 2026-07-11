package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect;

@CardRegistration(set = "POR", collectorNumber = "55")
public class Flux extends Card {

    public Flux() {
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
