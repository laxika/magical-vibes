package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileIfHadCounterElseReturnWithCounterEffect;

@CardRegistration(set = "VIS", collectorNumber = "76")
public class BogardanPhoenix extends Card {

    public BogardanPhoenix() {
        // Flying is auto-loaded from Scryfall keywords.
        // When this creature dies, exile it if it had a death counter on it. Otherwise, return it
        // to the battlefield under your control and put a death counter on it.
        addEffect(EffectSlot.ON_DEATH, new ExileIfHadCounterElseReturnWithCounterEffect(CounterType.DEATH));
    }
}
