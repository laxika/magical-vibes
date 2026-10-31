package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "5DN", collectorNumber = "27")
@CardRegistration(set = "DD2", collectorNumber = "28")
@CardRegistration(set = "JVC", collectorNumber = "28")
@CardRegistration(set = "IMA", collectorNumber = "46")
public class Condescend extends Card {

    public Condescend() {
        addSpellEffect(new CounterUnlessPaysEffect(0, true, false));
        addSpellEffect(new ScryEffect(2));
    }
}
