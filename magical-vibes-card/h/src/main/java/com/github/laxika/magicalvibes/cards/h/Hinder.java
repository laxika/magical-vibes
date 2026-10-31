package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;

@CardRegistration(set = "CHK", collectorNumber = "65")
@CardRegistration(set = "2XM", collectorNumber = "54")
public class Hinder extends Card {

    public Hinder() {
        addSpellEffect(new CounterSpellEffect(CounteredSpellDestination.LIBRARY_TOP_OR_BOTTOM));
    }
}
