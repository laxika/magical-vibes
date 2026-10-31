package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "RNA", collectorNumber = "48")
@CardRegistration(set = "RVR", collectorNumber = "56")
public class Quench extends Card {

    public Quench() {
        addSpellEffect(new CounterUnlessPaysEffect(2));
    }
}
