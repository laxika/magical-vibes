package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "IKO", collectorNumber = "59")
public class Neutralize extends Card {

    public Neutralize() {
        addSpellEffect(new CounterSpellEffect());
        addCycling("{2}");
    }
}
