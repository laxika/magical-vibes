package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "64")
public class StymiedHopes extends Card {

    public StymiedHopes() {
        addSpellEffect(new CounterUnlessPaysEffect(1));
        addSpellEffect(new ScryEffect(1));
    }
}
