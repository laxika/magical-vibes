package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ONS", collectorNumber = "76")
public class Complicate extends Card {

    public Complicate() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3));

        addCycling("{2}{U}");
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                new CounterUnlessPaysEffect(1), "Counter target spell unless its controller pays {1}?"));
    }
}
