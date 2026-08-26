package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "MKM", collectorNumber = "221")
public class NoMoreLies extends Card {

    public NoMoreLies() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3, false, true));
    }
}
