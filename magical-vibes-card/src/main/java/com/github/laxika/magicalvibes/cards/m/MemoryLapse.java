package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "103")
@CardRegistration(set = "7ED", collectorNumber = "88")
@CardRegistration(set = "6ED", collectorNumber = "81")
public class MemoryLapse extends Card {

    public MemoryLapse() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.LIBRARY_TOP));
    }
}
