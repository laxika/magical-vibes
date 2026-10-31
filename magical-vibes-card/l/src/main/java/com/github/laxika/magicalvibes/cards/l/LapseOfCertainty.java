package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "9")
public class LapseOfCertainty extends Card {

    public LapseOfCertainty() {
        addSpellEffect(new CounterSpellEffect(CounteredSpellDestination.LIBRARY_TOP));
    }
}
