package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THB", collectorNumber = "54")
public class MemoryDrain extends Card {

    public MemoryDrain() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ScryEffect(2));
    }
}
