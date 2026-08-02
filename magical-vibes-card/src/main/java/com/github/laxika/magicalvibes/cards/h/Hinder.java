package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;

@CardRegistration(set = "CHK", collectorNumber = "65")
public class Hinder extends Card {

    public Hinder() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.LIBRARY_TOP_OR_BOTTOM));
    }
}
