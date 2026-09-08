package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TibaltTrickeryEffect;

@CardRegistration(set = "KHM", collectorNumber = "153")
public class TibaltsTrickery extends Card {

    public TibaltsTrickery() {
        addEffect(EffectSlot.SPELL, new TibaltTrickeryEffect());
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
