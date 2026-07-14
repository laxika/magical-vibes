package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndPutOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "88")
@CardRegistration(set = "6ED", collectorNumber = "81")
public class MemoryLapse extends Card {

    public MemoryLapse() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndPutOnTopOfLibraryEffect());
    }
}
